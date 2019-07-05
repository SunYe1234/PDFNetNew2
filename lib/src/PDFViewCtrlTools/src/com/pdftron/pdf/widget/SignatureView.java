package com.pdftron.pdf.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.pdftron.pdf.utils.AnalyticsHandlerAdapter;
import com.pdftron.pdf.utils.ImageMemoryCache;
import java.util.LinkedList;

public class SignatureView extends View {

    public interface SignatureViewListener {
        void onTouchStart(float x, float y);
        void onError();
    }

    private SignatureViewListener mSignatureViewListener;

    private Bitmap mBitmap;
    private Canvas mCanvas;
    private Path mPath;
    private Paint mBitmapPaint;
    private Paint mPencilPaint;

    private int mColor;
    private float mStrokeWidth;

    private LinkedList<PointF> mPathPoints;
    private LinkedList<LinkedList<PointF>> mSignaturePathPoints;

    private LinkedList<Path> mPaths;

    private float mLeft = 0.0f;
    private float mTop = 0.0f;
    private float mRight = 0.0f;
    private float mBottom = 0.0f;
    private boolean mIsFirstPoint = true;

    public SignatureView(Context context) {
        super(context);

        mPath = new Path();
        mPaths = new LinkedList<>();

        mPencilPaint = new Paint();
        mPencilPaint.setAntiAlias(true);
        mPencilPaint.setDither(true);
        mPencilPaint.setStyle(Paint.Style.STROKE);
        mPencilPaint.setStrokeCap(Paint.Cap.ROUND);
        mPencilPaint.setStrokeJoin(Paint.Join.ROUND);

        mBitmapPaint = new Paint(Paint.DITHER_FLAG);

        mPathPoints = new LinkedList<>();
        mSignaturePathPoints = new LinkedList<>();

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        setLayoutParams(layoutParams);
    }

    public void setSignatureViewListener(SignatureViewListener listener) {
        mSignatureViewListener = listener;
    }

    public void setup(int color, float thickness) {
        mColor = color;
        mStrokeWidth = thickness;
        mPencilPaint.setColor(mColor);
        mPencilPaint.setStrokeWidth(mStrokeWidth);

        mBitmapPaint.setColor(mColor);
        mBitmapPaint.setStrokeWidth(mStrokeWidth);
    }

    public void eraseSignature() {
        mPathPoints.clear();
        mSignaturePathPoints.clear();
        mIsFirstPoint = true;

        // Erase previous drawing on Canvas
        mCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
        mPath = new Path();
        mPaths.clear();
        invalidate();
    }

    public void updateStrokeThickness(float thickness) {
        mStrokeWidth = thickness;
        // update signature stroke thickness
        mBitmapPaint.setStrokeWidth(thickness);
        mPencilPaint.setStrokeWidth(thickness);

        mCanvas.drawColor(0, PorterDuff.Mode.CLEAR);

        invalidate();
    }

    public void updateStrokeColor(int color) {
        // update signature stroke color
        mColor = color;
        mBitmapPaint.setColor(color);
        mPencilPaint.setColor(color);

        invalidate();
    }

    public void clearResources() {
        ImageMemoryCache.getInstance().addBitmapToReusableSet(mBitmap);
        mBitmap = null;
    }

    public LinkedList<LinkedList<PointF>> getSignaturePaths() {
        return mSignaturePathPoints;
    }

    public RectF getBoundingBox() {
        return new RectF(mLeft, mTop, mRight, mBottom);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (canvas == null || mBitmap == null) {
            return;
        }

        for (Path p : mPaths) {
            canvas.drawPath(p, mPencilPaint);
        }
        canvas.drawPath(mPath, mPencilPaint);

        canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        try {
            mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ALPHA_8);
        } catch (Exception e) {
            AnalyticsHandlerAdapter.getInstance().sendException(e);
            if (mSignatureViewListener != null) {
                mSignatureViewListener.onError();
            }
            return;
        }
        mCanvas = new Canvas(mBitmap);
    }

    private float mX, mY;
    private static final float TOUCH_TOLERANCE = 1;

    private void touch_start(float x, float y) {
        mPath = new Path();
        mPath.moveTo(x, y);
        mX = x;
        mY = y;

        mPathPoints = new LinkedList<>();
        mPathPoints.add(new PointF(x, y));

        if (mSignatureViewListener != null) {
            mSignatureViewListener.onTouchStart(x, y);
        }

        // Initialize bounding values if it is the first touch
        if (mIsFirstPoint) {
            mLeft = x;
            mTop = y;
            mRight = x;
            mBottom = y;
            mIsFirstPoint = false;
        }
    }

    private void touch_move(float x, float y) {
        // TODO What if points are outside area?
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            //mPath.lineTo(x, y);
            mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
            mX = x;
            mY = y;
            mPathPoints.add(new PointF(x, y));

            mLeft = Math.min(x, mLeft);
            mTop = Math.max(y, mTop);
            mRight = Math.max(x, mRight);
            mBottom = Math.min(y, mBottom);
        }
    }

    private void touch_up() {
        mPath.lineTo(mX, mY);
        mPaths.add(mPath);
        // Commit the path to our off-screen
        mCanvas.drawPath(mPath, mPencilPaint);
        // Kill this so we don't double draw
        mPath = new Path();
        // Add current path to the list
        mSignaturePathPoints.add(mPathPoints);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touch_start(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                touch_move(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                touch_up();
                invalidate();
                break;
        }
        return true;
    }
}
