//---------------------------------------------------------------------------------------
// Copyright (c) 2001-2019 by PDFTron Systems Inc. All Rights Reserved.
// Consult legal.txt regarding legal and license information.
//---------------------------------------------------------------------------------------

package com.pdftron.pdf.tools;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.support.annotation.Keep;
import android.support.annotation.NonNull;
import android.view.MotionEvent;

import com.pdftron.common.PDFNetException;
import com.pdftron.pdf.Annot;
import com.pdftron.pdf.PDFDoc;
import com.pdftron.pdf.PDFViewCtrl;
import com.pdftron.pdf.Rect;
import com.pdftron.pdf.annots.Line;
import com.pdftron.pdf.model.AnnotStyle;
import com.pdftron.pdf.model.MeasureInfo;
import com.pdftron.pdf.model.RulerItem;
import com.pdftron.pdf.utils.AnalyticsHandlerAdapter;
import com.pdftron.pdf.utils.AnnotUtils;
import com.pdftron.pdf.utils.DrawingUtils;
import com.pdftron.pdf.utils.MeasureImpl;
import com.pdftron.pdf.utils.MeasureUtils;
import com.pdftron.pdf.utils.UnitConverter;

import java.util.Locale;

@Keep
public class RulerCreate extends ArrowCreate {

    private PointF mPt5, mPt6;

    private String mText = "";

    private MeasureImpl mMeasureImpl;

    /**
     * Class constructor
     */
    public RulerCreate(@NonNull PDFViewCtrl ctrl) {
        super(ctrl);

        mPt5 = new PointF(0, 0);
        mPt6 = new PointF(0, 0);

        mMeasureImpl = new MeasureImpl(getCreateAnnotType());

        ToolManager toolManager = (ToolManager) mPdfViewCtrl.getToolManager();
        setSnappingEnabled(toolManager.isSnappingEnabledForMeasurementTools());
    }

    @Override
    public ToolManager.ToolModeBase getToolMode() {
        return ToolManager.ToolMode.RULER_CREATE;
    }

    @Override
    public int getCreateAnnotType() {
        return AnnotStyle.CUSTOM_ANNOT_TYPE_RULER;
    }

    @Override
    public void setupAnnotProperty(AnnotStyle annotStyle) {
        super.setupAnnotProperty(annotStyle);

        mMeasureImpl.setupAnnotProperty(mPdfViewCtrl.getContext(), annotStyle);
    }

    @Override
    public boolean onDown(MotionEvent e) {

        mMeasureImpl.handleDown(mPdfViewCtrl.getContext());

        return super.onDown(e);
    }

    @Override
    public boolean onMove(MotionEvent e1, MotionEvent e2, float x_dist, float y_dist) {
        super.onMove(e1, e2, x_dist, y_dist);

        // We are scrolling
        if (mAllowTwoFingerScroll) {
            return false;
        }
        if (mAllowOneFingerScrollWithStylus) {
            return false;
        }

        mText = adjustContents();

        return true;
    }

    @Override
    protected Annot createMarkup(@NonNull PDFDoc doc, Rect bbox) throws PDFNetException {
        Line line = new Line(super.createMarkup(doc, bbox));
        line.setShowCaption(true);
        line.setContents(adjustContents());
        line.setEndStyle(Line.e_Butt);
        line.setStartStyle(Line.e_Butt);
        line.setCaptionPosition(Line.e_Top);
        mMeasureImpl.commit(line);
        return line;
    }

    @Override
    protected void calculateEndingStyle() {
        DrawingUtils.calcRuler(mPt1, mPt2, mPt3, mPt4, mPt5, mPt6,
            mThickness, mZoom);
    }

    @Override
    public void onDraw(Canvas canvas, Matrix tfm) {
        // We are scrolling
        if (mAllowTwoFingerScroll) {
            return;
        }
        if (mIsAllPointsOutsidePage) {
            return;
        }

        DrawingUtils.drawRuler(canvas, mPt1, mPt2,
            mPt3, mPt4, mPt5, mPt6, mOnDrawPath, mPaint, mText, mZoom);
    }

    private String adjustContents() {
        double[] pts1, pts2;
        pts1 = mPdfViewCtrl.convScreenPtToPagePt(mPt1.x, mPt1.y, mDownPageNum);
        pts2 = mPdfViewCtrl.convScreenPtToPagePt(mPt2.x, mPt2.y, mDownPageNum);

        double pt1x = pts1[0];
        double pt1y = pts1[1];
        double pt2x = pts2[0];
        double pt2y = pts2[1];

        return adjustContents(mMeasureImpl, pt1x, pt1y, pt2x, pt2y);
    }

    private static String adjustContents(MeasureImpl measureImpl, double pt1x, double pt1y, double pt2x, double pt2y) {
        double lineLength = MeasureUtils.getLineLength(pt1x, pt1y, pt2x, pt2y);
        MeasureInfo axis = measureImpl.getAxis();
        MeasureInfo distanceMeasure = measureImpl.getMeasure();
        if (axis == null || distanceMeasure == null) {
            return "";
        }

        double distance = lineLength * axis.getFactor() * distanceMeasure.getFactor();
        return measureImpl.getMeasurementText(distance, distanceMeasure);
    }

    /**
     * Modifies the annotation's measurement entries based on input.
     * @param annot the annotation
     * @param rulerItem the scale information
     * @param pt1x pt1x
     * @param pt1y pt1y
     * @param pt2x pt2x
     * @param pt2y pt2y
     */
    public static void adjustContents(Annot annot, RulerItem rulerItem, double pt1x, double pt1y, double pt2x, double pt2y) {
        try {
            MeasureImpl measure = new MeasureImpl(AnnotUtils.getAnnotType(annot));
            measure.updateRulerItem(rulerItem);
            String result = adjustContents(measure, pt1x, pt1y, pt2x, pt2y);
            annot.setContents(result);
            measure.commit(annot);
        } catch (Exception ex) {
            AnalyticsHandlerAdapter.getInstance().sendException(ex);
        }
    }

    /**
     * Gets measurement label for display purpose.
     * @param rulerItem the scale information
     * @param pt1x pt1x
     * @param pt1y pt1y
     * @param pt2x pt2x
     * @param pt2y pt2y
     * @return the label
     */
    public static String getLabel(RulerItem rulerItem, double pt1x, double pt1y, double pt2x, double pt2y) {
        MeasureImpl measure = new MeasureImpl(AnnotStyle.CUSTOM_ANNOT_TYPE_RULER);
        measure.updateRulerItem(rulerItem);
        return adjustContents(measure, pt1x, pt1y, pt2x, pt2y);
    }

    @Deprecated
    public static String getRulerLabel(RulerItem rulerItem, double pt1x, double pt1y, double pt2x, double pt2y) {
        float distance = Math.round(Math.sqrt(Math.pow((pt2x - pt1x), 2) + Math.pow((pt2y - pt1y), 2)));

        float inches = UnitConverter.pointsToInches(distance);

        float baseMeasure = UnitConverter.convert(inches).from(UnitConverter.INCH).to(rulerItem.mRulerBaseUnit);
        float convertedMeasure = UnitConverter.convert(baseMeasure).from(rulerItem.mRulerBaseUnit).to(rulerItem.mRulerTranslateUnit);
        float ratio = rulerItem.mRulerTranslate / rulerItem.mRulerBase;

        float distanceFinal = convertedMeasure * ratio;
        String distanceFinals = String.format(Locale.getDefault(), "%.2f", distanceFinal);

        return distanceFinals + UnitConverter.getDisplayUnit(rulerItem.mRulerTranslateUnit);
    }
}
