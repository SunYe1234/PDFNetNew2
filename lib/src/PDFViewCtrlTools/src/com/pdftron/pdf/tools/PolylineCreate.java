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

import com.pdftron.common.PDFNetException;
import com.pdftron.pdf.Annot;
import com.pdftron.pdf.PDFDoc;
import com.pdftron.pdf.PDFViewCtrl;
import com.pdftron.pdf.Point;
import com.pdftron.pdf.Rect;
import com.pdftron.pdf.annots.PolyLine;
import com.pdftron.pdf.tools.ToolManager.ToolMode;
import com.pdftron.pdf.utils.DrawingUtils;
import com.pdftron.pdf.utils.Utils;

import java.util.ArrayList;

/**
 * This class is for creating a rectangle annotation.
 */
@Keep
public class PolylineCreate extends AdvancedShapeCreate {

    /**
     * Class constructor
     */
    public PolylineCreate(@NonNull PDFViewCtrl ctrl) {
        super(ctrl);
        mNextToolMode = getToolMode();
    }

    /**
     * The overload implementation of {@link Tool#getToolMode()}.
     */
    @Override
    public ToolManager.ToolModeBase getToolMode() {
        return ToolMode.POLYLINE_CREATE;
    }

    @Override
    public int getCreateAnnotType() {
        return Annot.e_Polyline;
    }

    /**
     * The overload implementation of {@link AdvancedShapeCreate#createMarkup(PDFDoc, ArrayList)}.
     */
    @Override
    protected Annot createMarkup(@NonNull PDFDoc doc,
                                 ArrayList<Point> pagePoints) throws PDFNetException {
        Rect annotRect = Utils.getBBox(pagePoints);
        if (annotRect == null) {
            return null;
        }
        annotRect.inflate(mThickness);

        PolyLine poly = new PolyLine(PolyLine.create(doc, Annot.e_Polyline, annotRect));

        int pointIdx = 0;
        for (Point point : pagePoints) {
            poly.setVertex(pointIdx++, point);
        }
        poly.setRect(annotRect);

        return poly;
    }

    @Override
    protected void drawMarkup(@NonNull Canvas canvas,
                              Matrix tfm,
                              @NonNull ArrayList<PointF> canvasPoints) {
        if (mPdfViewCtrl == null) {
            return;
        }
        DrawingUtils.drawPolyline(mPdfViewCtrl, getPageNum(),
            canvas, canvasPoints, mPath, mPaint, mStrokeColor);
    }
}
