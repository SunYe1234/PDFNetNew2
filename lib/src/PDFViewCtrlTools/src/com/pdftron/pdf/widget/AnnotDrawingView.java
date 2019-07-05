package com.pdftron.pdf.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

import com.pdftron.pdf.Annot;
import com.pdftron.pdf.CurvePainter;
import com.pdftron.pdf.PDFViewCtrl;
import com.pdftron.pdf.annots.Ink;
import com.pdftron.pdf.config.ToolConfig;
import com.pdftron.pdf.model.AnnotStyle;
import com.pdftron.pdf.model.InkItem;
import com.pdftron.pdf.model.RotateInfo;
import com.pdftron.pdf.model.RulerItem;
import com.pdftron.pdf.tools.FreehandCreate;
import com.pdftron.pdf.tools.RulerCreate;
import com.pdftron.pdf.tools.ToolManager;
import com.pdftron.pdf.utils.AnalyticsHandlerAdapter;
import com.pdftron.pdf.utils.DrawingUtils;
import com.pdftron.pdf.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;

import static com.pdftron.pdf.tools.AnnotEdit.e_lm;
import static com.pdftron.pdf.tools.AnnotEdit.e_lr;
import static com.pdftron.pdf.tools.AnnotEdit.e_ml;
import static com.pdftron.pdf.tools.AnnotEdit.e_ul;

public class AnnotDrawingView extends AppCompatImageView {

    private AnnotViewImpl mAnnoViewImpl;

    private RectF mOval = new RectF();
    private RectF mBBox = new RectF();
    private int mPageNum;

    private PointF mPt3 = new PointF(0, 0);
    private PointF mPt4 = new PointF(0, 0);
    private PointF mPt5 = new PointF(0, 0);
    private PointF mPt6 = new PointF(0, 0);

    private int mXOffset;
    private int mYOffset;

    private Path mOnDrawPath = new Path();
    private Path mRotatePath = new Path();

    private Rect mOffsetRect = new Rect();

    private String mIcon;

    private ArrayList<PointF> mVertices = new ArrayList<>();
    private PointF[] mPolyShapeCtrlPts;

    private ArrayList<InkItem> mInks = new ArrayList<>();
    private PointF mInkOffset;
    private float mInitialWidthScreen;
    private float mInitialHeightScreen;
    private float mScaleWidthScreen;
    private float mScaleHeightScreen;

    private boolean mInitRectSet;

    private float mRotDegree;
    private float mRotDegreeSave;
    private boolean mRotating;
    private boolean mRotated;
    private Integer mSnapDegree;

    private boolean mCanDraw;
    private Bitmap mAnnotBitmap;

    public AnnotDrawingView(Context context) {
        this(context, null);
    }

    public AnnotDrawingView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AnnotDrawingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init(context);
    }

    private void init(Context context) {
        mAnnoViewImpl = new AnnotViewImpl(context);
    }

    public void setCanDraw(boolean canDraw) {
        mCanDraw = canDraw;
    }

    public void setAnnotStyle(PDFViewCtrl pdfViewCtrl, AnnotStyle annotStyle) {
        mAnnoViewImpl.setAnnotStyle(pdfViewCtrl, annotStyle);
        updateIcon(annotStyle.getIcon());
    }

    public void setAnnotBitmap(Bitmap bitmap) {
        mAnnotBitmap = bitmap;
    }

    public void setCurvePainter(CurvePainter curvePainter) {
        if (curvePainter == null) {
            return;
        }
        if (mAnnoViewImpl.mCurvePainter != null && mRotated) {
            // don't update the appearance if rotated
            return;
        }
        mAnnoViewImpl.mCurvePainter = curvePainter;
        if (curvePainter.getRect() != null) {
            mInitialWidthScreen = mScaleWidthScreen = curvePainter.getRect().width();
            mInitialHeightScreen = mScaleHeightScreen = curvePainter.getRect().height();
        }
        invalidate();
    }

    private boolean isSizeOfAnnot() {
        // whether the annot drawing view is size of the annot bbox
        return ToolConfig.getInstance().getAnnotationHandlerToolMode(mAnnoViewImpl.mAnnotStyle.getAnnotType()) == ToolManager.ToolMode.ANNOT_EDIT ||
                mAnnoViewImpl.mAnnotStyle.getAnnotType() == Annot.e_Link ||
                mAnnoViewImpl.mAnnotStyle.getAnnotType() == Annot.e_Widget;
    }

    public void initInkItem(Annot inkAnnot, PointF offset) {
        if (mAnnoViewImpl.mAnnotStyle.getAnnotType() == Annot.e_Ink) {
            try {
                InkItem item = new InkItem(mAnnoViewImpl.mPaint, mAnnoViewImpl.mStrokeColor, mAnnoViewImpl.mOpacity, mAnnoViewImpl.mThickness, false);
                mInks.add(item);
                Ink ink = new Ink(inkAnnot);
                com.pdftron.pdf.Rect rect = inkAnnot.getRect();
                rect.normalize();
                FreehandCreate.setupInkItem(ink, item);
                mInkOffset = offset;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public void updateColor(int color) {
        mAnnoViewImpl.updateColor(color);
        if (!mInks.isEmpty()) {
            for (InkItem inkItem : mInks) {
                inkItem.mPaint = mAnnoViewImpl.mPaint;
                inkItem.mColor = color;
            }
        }
        invalidate();
    }

    public void updateFillColor(int color) {
        mAnnoViewImpl.updateFillColor(color);
        invalidate();
    }

    public void updateThickness(float thickness) {
        mAnnoViewImpl.updateThickness(thickness);
        if (!mInks.isEmpty()) {
            for (InkItem inkItem : mInks) {
                inkItem.mPaint = mAnnoViewImpl.mPaint;
                inkItem.mThickness = thickness;
            }
        }
        invalidate();
    }

    public void updateOpacity(float opacity) {
        mAnnoViewImpl.updateOpacity(opacity);
        if (!Utils.isNullOrEmpty(mIcon)) {
            updateIcon(mIcon);
        } else {
            if (!mInks.isEmpty()) {
                for (InkItem inkItem : mInks) {
                    inkItem.mPaint = mAnnoViewImpl.mPaint;
                    inkItem.mOpacity = opacity;
                }
            }
            invalidate();
        }
    }

    public void updateRulerItem(RulerItem rulerItem) {
        mAnnoViewImpl.updateRulerItem(rulerItem);
        invalidate();
    }

    public void setZoom(double zoom) {
        mAnnoViewImpl.setZoom(zoom);
    }

    public void setHasPermission(boolean hasPermission) {
        mAnnoViewImpl.mHasSelectionPermission = hasPermission;
    }

    public void setCtrlPts(PointF[] pts) {
        mAnnoViewImpl.mCtrlPts = pts;
    }

    public void setAnnotRect(@Nullable Rect rect) {
        if (null == rect) {
            return;
        }
        if (!mInitRectSet) {
            mInitialWidthScreen = rect.width();
            mInitialHeightScreen = rect.height();
            mScaleWidthScreen = mInitialWidthScreen;
            mScaleHeightScreen = mInitialHeightScreen;
            mInitRectSet = true;
        }
        mAnnoViewImpl.mPt1.set(rect.left, rect.top);
        mAnnoViewImpl.mPt2.set(rect.right, rect.bottom);

        mScaleWidthScreen = rect.width();
        mScaleHeightScreen = rect.height();

        mAnnoViewImpl.mAnnotRect = rect;
        mBBox.set(rect);
    }

    public void setOffset(int x, int y) {
        mXOffset = x;
        mYOffset = y;
        invalidate();
    }

    public void setPageNum(int pageNum) {
        mPageNum = pageNum;
    }

    public void setVertices(PointF... points) {
        mVertices.clear();
        if (points != null) {
            mVertices.addAll(Arrays.asList(points));
            mPolyShapeCtrlPts = points;
        }
    }

    public ArrayList<PointF> getVertices() {
        return mVertices;
    }

    public PointF[] getPolyShapeCtrlPts() {
        return mPolyShapeCtrlPts;
    }

    public void updateIcon(String icon) {
        mIcon = icon;
    }

    public void removeCtrlPts() {
        mAnnoViewImpl.removeCtrlPts();
        invalidate();
    }

    public RotateInfo handleRotation(PointF downPt, PointF movePt, boolean done) {
        mRotating = !done;
        mRotated = true;
        PointF pivot = center();

        mRotDegree = (float) Utils.angleBetweenTwoPointsWithPivot(downPt.x, downPt.y, movePt.x, movePt.y, pivot.x, pivot.y);
        if (done) {
            mRotDegreeSave += mRotDegree;
        }
        invalidate();
        return new RotateInfo(-mRotDegree, pivot);
    }

    public void snapToDegree(Integer degree, float startDegree) {
        mSnapDegree = degree;
        if (mSnapDegree != null) {
            mRotDegree = -(mSnapDegree - startDegree); // clockwise
        }
        invalidate();
    }

    public PointF center() {
        if (mAnnoViewImpl.mAnnotRect == null) {
            return null;
        }
        return new PointF(mAnnoViewImpl.mAnnotRect.exactCenterX(), mAnnoViewImpl.mAnnotRect.exactCenterY());
    }

    private boolean isFreeHighlighter() {
        return mAnnoViewImpl.mAnnotStyle.getAnnotType() == AnnotStyle.CUSTOM_ANNOT_TYPE_FREE_HIGHLIGHTER;
    }

    private boolean isAnnotResizable() {
        return !(mAnnoViewImpl.mAnnotStyle.getAnnotType() == Annot.e_Text) &&
                !(mAnnoViewImpl.mAnnotStyle.getAnnotType() == Annot.e_Sound) &&
                !(mAnnoViewImpl.mAnnotStyle.getAnnotType() == Annot.e_FileAttachment);
    }

    private void drawSelectionBox(Canvas canvas) {
        if (mAnnoViewImpl.mAnnotStyle.getAnnotType() == Annot.e_Stamp) {
            float left = mAnnoViewImpl.mCtrlPts[e_ul].x;
            float top = mAnnoViewImpl.mCtrlPts[e_ul].y;
            float right = mAnnoViewImpl.mCtrlPts[e_lr].x;
            float bottom = mAnnoViewImpl.mCtrlPts[e_lr].y;
            DrawingUtils.drawSelectionBox(mAnnoViewImpl.mCtrlPtsPaint, getContext(),
                    canvas, left, top, right, bottom, mAnnoViewImpl.mHasSelectionPermission);
        }
    }

    private void drawCtrlPts(Canvas canvas) {
        if (!mAnnoViewImpl.mCanDrawCtrlPts) {
            return;
        }
        if (!isAnnotResizable()) {
            // do not draw control points for annotation that is not resizable
            return;
        }
        if (mAnnoViewImpl.mAnnotStyle.getAnnotType() == Annot.e_Line ||
                mAnnoViewImpl.mAnnotStyle.getAnnotType() == AnnotStyle.CUSTOM_ANNOT_TYPE_ARROW ||
                mAnnoViewImpl.mAnnotStyle.getAnnotType() == AnnotStyle.CUSTOM_ANNOT_TYPE_RULER) {
            DrawingUtils.drawCtrlPtsLine(getContext().getResources(), canvas,
                    mAnnoViewImpl.mCtrlPtsPaint, mVertices.get(0), mVertices.get(1),
                    mAnnoViewImpl.mCtrlRadius, mAnnoViewImpl.mHasSelectionPermission);
        } else if (mAnnoViewImpl.mAnnotStyle.getAnnotType() == Annot.e_Polyline ||
                mAnnoViewImpl.mAnnotStyle.getAnnotType() == Annot.e_Polygon ||
                mAnnoViewImpl.mAnnotStyle.getAnnotType() == AnnotStyle.CUSTOM_ANNOT_TYPE_CLOUD ||
                mAnnoViewImpl.mAnnotStyle.getAnnotType() == AnnotStyle.CUSTOM_ANNOT_TYPE_CALLOUT ||
                mAnnoViewImpl.mAnnotStyle.getAnnotType() == AnnotStyle.CUSTOM_ANNOT_TYPE_PERIMETER_MEASURE ||
                mAnnoViewImpl.mAnnotStyle.getAnnotType() == AnnotStyle.CUSTOM_ANNOT_TYPE_AREA_MEASURE) {
            try {
                DrawingUtils.drawCtrlPtsAdvancedShape(getContext().getResources(), canvas,
                        mAnnoViewImpl.mCtrlPtsPaint, mPolyShapeCtrlPts, mAnnoViewImpl.mCtrlRadius,
                        mAnnoViewImpl.mHasSelectionPermission, false);
            } catch (Exception ignored) {
            }
        } else {
            try {
                DrawingUtils.drawCtrlPts(getContext().getResources(), canvas,
                        mAnnoViewImpl.mCtrlPtsPaint, mAnnoViewImpl.mCtrlPts[e_ul], mAnnoViewImpl.mCtrlPts[e_lr],
                        mAnnoViewImpl.mCtrlPts[e_lm], mAnnoViewImpl.mCtrlPts[e_ml], mAnnoViewImpl.mCtrlRadius,
                        mAnnoViewImpl.mHasSelectionPermission, mAnnoViewImpl.mAnnotStyle.getAnnotType() == Annot.e_Stamp);
            } catch (Exception ignored) {
            }
        }
    }

    private boolean canUseCoreRender() {
        // for stamp, we can generate both, prefer core version
        return mAnnoViewImpl.mAnnotStyle.hasAppearance() || mAnnoViewImpl.mAnnotStyle.getAnnotType() == Annot.e_Stamp;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        try {
            canvas.save();
            PointF centerPt = center();
            if (centerPt != null) {
                float degree = mRotating ? mRotDegreeSave + mRotDegree : mRotDegreeSave;
                canvas.rotate(degree, centerPt.x, centerPt.y);
            }
            if (mAnnoViewImpl.mCurvePainter != null && canUseCoreRender() && mCanDraw) {
                if (isSizeOfAnnot()) {
                    if (mAnnoViewImpl.mCurvePainter.getBitmap() != null) {
                        Paint paint = mAnnoViewImpl.mBmpPaint;
                        if (isFreeHighlighter() && !mAnnoViewImpl.isNightMode()) {
                            paint = mAnnoViewImpl.mBmpMultBlendPaint;
                        }
                        mOffsetRect.left = mAnnoViewImpl.mCurvePainter.getRect().left + mAnnoViewImpl.mAnnotRect.left;
                        mOffsetRect.right = mOffsetRect.left + mAnnoViewImpl.mCurvePainter.getRect().width();
                        mOffsetRect.top = mAnnoViewImpl.mCurvePainter.getRect().top + mAnnoViewImpl.mAnnotRect.top;
                        mOffsetRect.bottom = mOffsetRect.top + mAnnoViewImpl.mCurvePainter.getRect().height();
                        canvas.drawBitmap(mAnnoViewImpl.mCurvePainter.getBitmap(), null, mOffsetRect, paint);
                    } else {
                        mAnnoViewImpl.mCurvePainter.draw(canvas, mAnnoViewImpl.mAnnotRect.left, mAnnoViewImpl.mAnnotRect.top,
                                mScaleWidthScreen / mInitialWidthScreen * mAnnoViewImpl.mZoom,
                                mScaleHeightScreen / mInitialHeightScreen * mAnnoViewImpl.mZoom,
                                mAnnoViewImpl.mZoom, mAnnoViewImpl.mZoom);
                    }
                } else {
                    Rect rect = mAnnoViewImpl.mAnnotRect;
                    if (rect != null) {
                        if (mAnnoViewImpl.mCurvePainter.getBitmap() != null) {
                            // draw bitmap into annot rect
                            canvas.drawBitmap(mAnnoViewImpl.mCurvePainter.getBitmap(), rect.left + mXOffset, rect.top + mYOffset, mAnnoViewImpl.mBmpPaint);
                        } else {
                            mAnnoViewImpl.mCurvePainter.draw(canvas, rect.left + mXOffset, rect.top + mYOffset,
                                    mAnnoViewImpl.mZoom, mAnnoViewImpl.mZoom,
                                    mAnnoViewImpl.mZoom, mAnnoViewImpl.mZoom);
                        }
                    }
                }
            } else if (mCanDraw) {
                if (mAnnoViewImpl.mAnnotStyle.getAnnotType() == Annot.e_Square) {
                    DrawingUtils.drawRectangle(canvas,
                            mAnnoViewImpl.mPt1, mAnnoViewImpl.mPt2,
                            mAnnoViewImpl.mThicknessDraw,
                            mAnnoViewImpl.mFillColor, mAnnoViewImpl.mStrokeColor,
                            mAnnoViewImpl.mFillPaint, mAnnoViewImpl.mPaint);
                } else if (mAnnoViewImpl.mAnnotStyle.getAnnotType() == Annot.e_Circle) {
                    DrawingUtils.drawOval(canvas,
                            mAnnoViewImpl.mPt1, mAnnoViewImpl.mPt2,
                            mAnnoViewImpl.mThicknessDraw,
                            mOval,
                            mAnnoViewImpl.mFillColor, mAnnoViewImpl.mStrokeColor,
                            mAnnoViewImpl.mFillPaint, mAnnoViewImpl.mPaint);
                } else if (mAnnoViewImpl.mAnnotStyle.getAnnotType() == Annot.e_Line) {
                    DrawingUtils.drawLine(canvas, mVertices.get(0), mVertices.get(1), mAnnoViewImpl.mPaint);
                } else if (mAnnoViewImpl.mAnnotStyle.getAnnotType() == AnnotStyle.CUSTOM_ANNOT_TYPE_ARROW) {
                    DrawingUtils.calcArrow(mVertices.get(0), mVertices.get(1),
                            mPt3, mPt4, mAnnoViewImpl.mThickness, mAnnoViewImpl.mZoom);
                    DrawingUtils.drawArrow(canvas, mVertices.get(0), mVertices.get(1),
                            mPt3, mPt4, mOnDrawPath, mAnnoViewImpl.mPaint);
                } else if (mAnnoViewImpl.mAnnotStyle.getAnnotType() == AnnotStyle.CUSTOM_ANNOT_TYPE_RULER) {
                    DrawingUtils.calcRuler(mVertices.get(0), mVertices.get(1),
                            mPt3, mPt4, mPt5, mPt6, mAnnoViewImpl.mThickness, mAnnoViewImpl.mZoom);

                    // calc distance
                    double[] pts1, pts2;
                    pts1 = mAnnoViewImpl.mPdfViewCtrl.convScreenPtToPagePt(mVertices.get(0).x, mVertices.get(0).y, mPageNum);
                    pts2 = mAnnoViewImpl.mPdfViewCtrl.convScreenPtToPagePt(mVertices.get(1).x, mVertices.get(1).y, mPageNum);

                    String text = RulerCreate.getLabel(mAnnoViewImpl.mAnnotStyle.getRulerItem(), pts1[0], pts1[1], pts2[0], pts2[1]);

                    DrawingUtils.drawRuler(canvas, mVertices.get(0), mVertices.get(1),
                            mPt3, mPt4, mPt5, mPt6, mOnDrawPath, mAnnoViewImpl.mPaint,
                            text, mAnnoViewImpl.mZoom);
                } else if (mAnnoViewImpl.mAnnotStyle.getAnnotType() == Annot.e_Polyline ||
                        mAnnoViewImpl.mAnnotStyle.getAnnotType() == AnnotStyle.CUSTOM_ANNOT_TYPE_PERIMETER_MEASURE) {
                    DrawingUtils.drawPolyline(mAnnoViewImpl.mPdfViewCtrl, mPageNum,
                            canvas, mVertices, mOnDrawPath, mAnnoViewImpl.mPaint, mAnnoViewImpl.mStrokeColor);
                } else if (mAnnoViewImpl.mAnnotStyle.getAnnotType() == Annot.e_Polygon ||
                        mAnnoViewImpl.mAnnotStyle.getAnnotType() == AnnotStyle.CUSTOM_ANNOT_TYPE_AREA_MEASURE) {
                    DrawingUtils.drawPolygon(mAnnoViewImpl.mPdfViewCtrl, mPageNum,
                            canvas, mVertices, mOnDrawPath, mAnnoViewImpl.mPaint, mAnnoViewImpl.mStrokeColor,
                            mAnnoViewImpl.mFillPaint, mAnnoViewImpl.mFillColor);
                } else if (mAnnoViewImpl.mAnnotStyle.getAnnotType() == AnnotStyle.CUSTOM_ANNOT_TYPE_CLOUD) {
                    DrawingUtils.drawCloud(mAnnoViewImpl.mPdfViewCtrl, mPageNum, canvas,
                            mVertices, mOnDrawPath, mAnnoViewImpl.mPaint, mAnnoViewImpl.mStrokeColor,
                            mAnnoViewImpl.mFillPaint, mAnnoViewImpl.mFillColor, mAnnoViewImpl.mAnnotStyle.getBorderEffectIntensity());
                } else if (mAnnoViewImpl.mAnnotStyle.getAnnotType() == Annot.e_Ink) {
                    DrawingUtils.drawInk(mAnnoViewImpl.mPdfViewCtrl, canvas, mInks, false,
                            mInkOffset, mScaleWidthScreen / mInitialWidthScreen, mScaleHeightScreen / mInitialHeightScreen, false);
                } else if (mAnnoViewImpl.mAnnotStyle.getAnnotType() == Annot.e_Stamp && mAnnotBitmap != null) {
                    canvas.drawBitmap(mAnnotBitmap, null, mAnnoViewImpl.mAnnotRect, mAnnoViewImpl.mBmpPaint);
                }
            }

            // draw selection box
            PathEffect pathEffect = mAnnoViewImpl.mCtrlPtsPaint.getPathEffect();
            if (!mRotated) {
                drawSelectionBox(canvas);
            }
            mAnnoViewImpl.mCtrlPtsPaint.setPathEffect(pathEffect);
            mAnnoViewImpl.mCtrlPtsPaint.setStrokeWidth(1);
            // draw control points
            if (!mRotated) {
                drawCtrlPts(canvas);
            }
            canvas.restore();
            // draw rotation guideline
            if (mSnapDegree != null) {
                DrawingUtils.drawGuideline(mSnapDegree, mAnnoViewImpl.mRotateCenterRadius,
                        canvas, mBBox, mRotatePath, mAnnoViewImpl.mRotateGuidelinePaint);
            }
        } catch (Exception ex) {
            AnalyticsHandlerAdapter.getInstance().sendException(ex);
        }
    }
}
