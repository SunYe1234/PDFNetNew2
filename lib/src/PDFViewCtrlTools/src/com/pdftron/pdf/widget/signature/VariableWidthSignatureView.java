package com.pdftron.pdf.widget.signature;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.pdftron.pdf.StrokeOutlineBuilder;
import com.pdftron.pdf.utils.PathPool;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

// Modified from https://github.com/simplifycom/ink-android/blob/master/ink/src/main/java/com/simplify/ink/InkView.java
@SuppressWarnings("RedundantThrows")
public class VariableWidthSignatureView extends View {

    private static final int DEFAULT_STROKE_COLOR = 0xFF000000;

    private Bitmap bitmap;
    private Canvas canvas;
    private Paint hdPaint;
    private ArrayList<InkListener> listeners = new ArrayList<>();

    private float strokeWidth;
    private StrokeOutlineBuilder currentOutline;
    private List<double[]> outlines = new ArrayList<>();

    private boolean isPressureSensitive = true;

    // Bounding box
    private float mLeft = 0.0f;
    private float mTop = 0.0f;
    private float mRight = 0.0f;
    private float mBottom = 0.0f;

    private CompositeDisposable mDisposable = new CompositeDisposable();
    private PublishSubject<InkEvent> mTouchEventSubject = PublishSubject.create();
    private Observable<double[]> mPointProcessor =
            mTouchEventSubject.serialize()
                    .observeOn(Schedulers.computation())
                    .map(new Function<InkEvent, double[]>() {
                        @Override
                        public double[] apply(InkEvent inkEvent) throws Exception {
                            throwIfOnMainThread();

                            // Add the point to StrokeOutlineBuilder
                            float x = inkEvent.x;
                            float y = inkEvent.y;
                            float pressure = inkEvent.pressure;

                            // On touch down create a new stroke, on touch move add to the
                            // existing stroke, and on touch up
                            switch (inkEvent.eventType) {
                                case ON_TOUCH_DOWN:
                                    currentOutline = new StrokeOutlineBuilder(strokeWidth);
                                    currentOutline.addPoint(x, y, pressure);
                                    return currentOutline.getLastSegmentOutline(2, StrokeOutlineBuilder.TipOptions.NO_SPECIAL_OPTIONS);
                                case ON_TOUCH_MOVE:
                                    currentOutline.addPoint(x, y, pressure);
                                    return currentOutline.getLastSegmentOutline(2, StrokeOutlineBuilder.TipOptions.NO_SPECIAL_OPTIONS);
                                case ON_TOUCH_UP:
                                    currentOutline.addPoint(x, y, pressure);
                                    outlines.add(currentOutline.getOutline());
                                    return currentOutline.getLastSegmentOutline(2, StrokeOutlineBuilder.TipOptions.HAS_END_TIP);
                            }
                            throw new RuntimeException("Missing check for event type");
                        }
                    });

    public VariableWidthSignatureView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VariableWidthSignatureView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {

        // init paint
        hdPaint = new Paint();
        hdPaint.setStrokeCap(Paint.Cap.ROUND); // this one is important
        hdPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        hdPaint.setStrokeWidth(0);
        hdPaint.setAntiAlias(true);

        // apply default settings
        setColor(DEFAULT_STROKE_COLOR);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        clear();
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        int action = e.getAction();
        // on down, initialize stroke point
        float x = e.getX();
        float y = e.getY();
        float pressure = isPressureSensitive ? e.getPressure() : 1.0f; // if not pressure sensitive, then just set pressure to 1

        if (action == MotionEvent.ACTION_DOWN) {
            // notify listeners
            for (InkListener listener : listeners) {
                listener.onInkStarted();
            }
            mTouchEventSubject.onNext(
                    new InkEvent(InkEventType.ON_TOUCH_DOWN,
                            x,
                            y,
                            pressure)
            );
        } else if (action == MotionEvent.ACTION_MOVE) {
            mTouchEventSubject.onNext(
                    new InkEvent(InkEventType.ON_TOUCH_MOVE,
                            x,
                            y,
                            pressure)
            );
        }
        if (action == MotionEvent.ACTION_UP) {
            mTouchEventSubject.onNext(
                    new InkEvent(InkEventType.ON_TOUCH_UP,
                            x,
                            y,
                            pressure)
            );
        }

        mLeft = Math.min(x, mLeft);
        mTop = Math.max(y, mTop);
        mRight = Math.max(x, mRight);
        mBottom = Math.min(y, mBottom);
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // simply paint the bitmap on the canvas
        canvas.drawBitmap(bitmap, 0, 0, null);
        super.onDraw(canvas);
    }

    /**
     * Adds a listener on the view
     *
     * @param listener The listener
     */
    public void addListener(InkListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    /**
     * Removes the listener from the view
     *
     * @param listener The listener
     */
    public void removeListener(InkListener listener) {
        listeners.remove(listener);
    }

    /**
     * Enables and disables pressure sensitive inking. By default this is enabled.
     *
     * @param isEnabled true if pressure sensitivity is enabled, false otherwise
     */
    public void setPressureSensitivity(boolean isEnabled) {
        isPressureSensitive = isEnabled;
    }

    /**
     * Sets the stroke color
     *
     * @param color The color value
     */
    public void setColor(int color) {
        hdPaint.setColor(color);

        // clear current drawn strokes
        if (bitmap != null) {
            bitmap.eraseColor(Color.TRANSPARENT);

            // draw stroke outlines with new color
            for (double[] outline : outlines) {
                mDisposable.add(
                        arrayToDrawablePath(Observable.just(outline).observeOn(Schedulers.computation()))
                                .subscribe(
                                        new Consumer<InkDrawInfo>() {
                                            @Override
                                            public void accept(InkDrawInfo drawInfo) throws Exception {
                                                throwIfOnMainThread();
                                                // Draw the outline
                                                canvas.drawPath(drawInfo.path, hdPaint);
                                                postInvalidate(drawInfo.left, drawInfo.top, drawInfo.right, drawInfo.bottom);
                                            }
                                        })
                );
            }
        }
    }

    /**
     * Clears the view
     */
    public void clear() {
        // init bitmap cache
        if (bitmap != null) {
            bitmap.recycle();
        }

        // init bitmap cache
        bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);

        canvas = new Canvas(bitmap);

        if (mDisposable != null) {
            mDisposable.clear();
        }

        outlines.clear();

        mDisposable.add(
                arrayToDrawablePath(mPointProcessor)
                        .subscribe(
                                new Consumer<InkDrawInfo>() {
                                    @Override
                                    public void accept(InkDrawInfo drawInfo) throws Exception {
                                        throwIfOnMainThread();
                                        // Draw the outline
                                        canvas.drawPath(drawInfo.path, hdPaint);
                                        postInvalidate(drawInfo.left, drawInfo.top, drawInfo.right, drawInfo.bottom);
                                    }
                                },
                                new Consumer<Throwable>() {
                                    @Override
                                    public void accept(Throwable throwable) throws Exception {
                                        throw new RuntimeException(throwable);
                                    }
                                })
        );

        invalidate();
    }

    // observable must run in background thread otherwise an exception will br thrown
    private Observable<InkDrawInfo> arrayToDrawablePath(Observable<double[]> observable) {
        return observable
                .filter(new Predicate<double[]>() {
                    @Override
                    public boolean test(double[] outline) throws Exception {
                        return outline.length >= 8;
                    }
                })
                .map(new Function<double[], InkDrawInfo>() {
                    @Override
                    public InkDrawInfo apply(double[] outline) throws Exception {
                        throwIfOnMainThread();
                        Path pathf = PathPool.getInstance().obtain();
                        pathf.setFillType(Path.FillType.WINDING);

                        double mLeft = outline[0];
                        double mTop = outline[1];
                        double mRight = outline[0];
                        double mBottom = outline[1];

                        pathf.moveTo((float) outline[0], (float) outline[1]);
                        for (int i = 2, cnt = outline.length; i < cnt; i += 6) {

                            // Curve will reside in convex hull of control points so
                            // determine boundary to draw
                            for (int k = 0; k <= 5; k += 2) {
                                mLeft = Math.min(outline[i + k], mLeft);
                                mTop = Math.min(outline[i + k + 1], mTop);
                                mRight = Math.max(outline[i + k], mRight);
                                mBottom = Math.max(outline[i + k + 1], mBottom);
                            }
                            pathf.cubicTo((float) outline[i], (float) outline[i + 1], (float) outline[i + 2],
                                    (float) outline[i + 3], (float) outline[i + 4], (float) outline[i + 5]);
                        }
                        int fudge = 2;
                        return new InkDrawInfo( // fudge the drawing box size by expanding a couple of pixels, just in case
                                (int) mLeft - fudge,
                                (int) mRight + fudge,
                                (int) mTop - fudge,
                                (int) mBottom + fudge,
                                pathf
                        );
                    }
                });
    }

    public void setStrokeWidth(float strokeWidth) {
        this.strokeWidth = strokeWidth;
    }

    public RectF getBoundingBox() {
        return new RectF(mLeft, mTop, mRight, mBottom);
    }

    public List<double[]> getStrokes() {
        return outlines;
    }

    /**
     * Listener for the ink view to notify clear events
     */
    public interface InkListener {
        /**
         * Callback method when the stroke has started.
         */
        void onInkStarted();
    }

    // Helper method to ensure we don't do any computation on the main thread
    private static void throwIfOnMainThread() {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            throw new IllegalStateException("Must not be invoked from the main thread.");
        }
    }
}
