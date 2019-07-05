package com.pdftron.pdf.widget;

import android.content.Context;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;

import com.pdftron.pdf.CurvePainter;
import com.pdftron.pdf.PDFRasterizer;
import com.pdftron.pdf.PDFViewCtrl;
import com.pdftron.pdf.model.AnnotStyle;
import com.pdftron.pdf.model.RulerItem;
import com.pdftron.pdf.tools.R;
import com.pdftron.pdf.tools.ToolManager;
import com.pdftron.pdf.utils.Utils;

public class AnnotViewImpl {

    public AnnotStyle mAnnotStyle;
    public CurvePainter mCurvePainter;

    public PDFViewCtrl mPdfViewCtrl;

    public PointF mPt1 = new PointF(0, 0);
    public PointF mPt2 = new PointF(0, 0);

    public Paint mPaint;
    public Paint mFillPaint;
    public Paint mCtrlPtsPaint;
    public Paint mBmpPaint;
    public Paint mBmpMultBlendPaint;
    public Paint mRotateGuidelinePaint;
    public float mRotateCenterRadius;
    public float mThickness;
    public float mThicknessReserve;
    public float mThicknessDraw;
    public int mStrokeColor;
    public int mFillColor;
    public float mOpacity;
    public double mZoom = 1.0;
    public float mCtrlRadius;
    public boolean mHasSelectionPermission = true;
    public PointF[] mCtrlPts;

    public Rect mAnnotRect;

    public boolean mCanDrawCtrlPts = true;

    public AnnotViewImpl(Context context) {
        init(context);
    }

    public AnnotViewImpl(PDFViewCtrl pdfViewCtrl, AnnotStyle annotStyle) {
        init(pdfViewCtrl.getContext());

        setAnnotStyle(pdfViewCtrl, annotStyle);
    }

    public void init(Context context) {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.RED);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.MITER);
        mPaint.setStrokeCap(Paint.Cap.BUTT);

        mFillPaint = new Paint(mPaint);
        mFillPaint.setStyle(Paint.Style.FILL);
        mFillPaint.setColor(Color.TRANSPARENT);

        mCtrlPtsPaint = new Paint(mPaint);

        mBmpPaint = new Paint();
        mBmpPaint.setStyle(Paint.Style.FILL);
        mBmpPaint.setAntiAlias(true);
        mBmpPaint.setFilterBitmap(false);

        mBmpMultBlendPaint = new Paint(mBmpPaint);
        mBmpMultBlendPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.MULTIPLY));

        mRotateGuidelinePaint = new Paint(mPaint);
        mRotateGuidelinePaint.setStyle(Paint.Style.STROKE);
        DashPathEffect dashPathEffect = new DashPathEffect(new float[]{Utils.convDp2Pix(context, 4.5f), Utils.convDp2Pix(context, 2.5f)}, 0);
        mRotateGuidelinePaint.setPathEffect(dashPathEffect);
        mRotateGuidelinePaint.setStrokeWidth(Utils.convDp2Pix(context, 1));
        mRotateGuidelinePaint.setColor(context.getResources().getColor(R.color.tools_annot_edit_rotate_guideline));
        mRotateCenterRadius = context.getResources().getDimensionPixelSize(R.dimen.rotate_guideline_center_radius);

        mThicknessDraw = 1.0f;
        mOpacity = 1.0f;
        mCtrlRadius = Utils.convDp2Pix(context, 7.5f);
    }

    public void setAnnotStyle(PDFViewCtrl pdfViewCtrl, AnnotStyle annotStyle) {
        mPdfViewCtrl = pdfViewCtrl;
        mAnnotStyle = annotStyle;

        mStrokeColor = annotStyle.getColor();
        mFillColor = annotStyle.getFillColor();
        mThickness = mThicknessReserve = annotStyle.getThickness();
        mOpacity = annotStyle.getOpacity();

        mPaint.setColor(Utils.getPostProcessedColor(mPdfViewCtrl, mStrokeColor));
        mFillPaint.setColor(Utils.getPostProcessedColor(mPdfViewCtrl, mFillColor));

        mPaint.setAlpha((int) (255 * mOpacity));
        mFillPaint.setAlpha((int) (255 * mOpacity));

        updateColor(mStrokeColor);
    }

    public void updateColor(int color) {
        mStrokeColor = color;
        mPaint.setColor(Utils.getPostProcessedColor(mPdfViewCtrl, mStrokeColor));
        updateOpacity(mOpacity);

        updateThickness(mThicknessReserve);
    }

    public void updateFillColor(int color) {
        mFillColor = color;
        mFillPaint.setColor(Utils.getPostProcessedColor(mPdfViewCtrl, mFillColor));
        updateOpacity(mOpacity);
    }

    public void updateThickness(float thickness) {
        mThickness = mThicknessReserve = thickness;
        if (mStrokeColor == Color.TRANSPARENT) {
            mThickness = 1.0f;
        } else {
            mThickness = thickness;
        }
        mThicknessDraw = (float) mZoom * mThickness;
        mPaint.setStrokeWidth(mThicknessDraw);
    }

    public void updateOpacity(float opacity) {
        mOpacity = opacity;
        mPaint.setAlpha((int) (255 * mOpacity));
        mFillPaint.setAlpha((int) (255 * mOpacity));
    }

    public void updateRulerItem(RulerItem rulerItem) {
        mAnnotStyle.setRulerItem(rulerItem);
    }

    public void setZoom(double zoom) {
        mZoom = zoom;
        mThicknessDraw = (float) mZoom * mThickness;
        mPaint.setStrokeWidth(mThicknessDraw);
    }

    public void removeCtrlPts() {
        mCanDrawCtrlPts = false;
    }

    public boolean isNightMode() {
        try {
            return mPdfViewCtrl.getColorPostProcessMode() == PDFRasterizer.e_postprocess_night_mode ||
                mPdfViewCtrl.getColorPostProcessMode() == PDFRasterizer.e_postprocess_invert;
        } catch (Exception ex) {
            return false;
        }
    }
}
