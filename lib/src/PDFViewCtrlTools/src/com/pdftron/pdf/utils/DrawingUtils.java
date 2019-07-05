package com.pdftron.pdf.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.pdftron.pdf.PDFViewCtrl;
import com.pdftron.pdf.model.InkItem;
import com.pdftron.pdf.tools.AnnotEditAdvancedShape;
import com.pdftron.pdf.tools.CloudCreate;
import com.pdftron.pdf.tools.FreehandCreate;
import com.pdftron.pdf.tools.R;

import java.util.ArrayList;

public class DrawingUtils {

    public static final int sRotateHandleSize = 36;

    /**
     * Draws the annotation selection box. Color and style of the box depends on whether
     * selection permission is granted. If permission granted, draws a blue rectangle with no padding,
     * otherwise draw a red dashed rectangle with padding.
     *
     * @param canvas to draw the box
     * @param left   The left side of the rectangle to be drawn
     * @param top    The top side of the rectangle to be drawn
     * @param right  The right side of the rectangle to be drawn
     * @param bottom The bottom side of the rectangle to be drawn
     */
    public static void drawSelectionBox(@NonNull Paint paint, @NonNull Context context, @NonNull Canvas canvas,
            float left, float top, float right, float bottom,
            boolean hasSelectionPermission) {
        DashPathEffect dashPathEffect = new DashPathEffect(new float[]{Utils.convDp2Pix(context, 4.5f), Utils.convDp2Pix(context, 2.5f)}, 0);
        paint.setStyle(Paint.Style.STROKE);
        final float thickness = Utils.convDp2Pix(context, 2.2f);
        final float padding = thickness * 1.5f;
        final float cornerRad = thickness / 2.0f;
        final float lineOverlapPadding = thickness / 2.0f;
        final float calculatedPadding;
        if (hasSelectionPermission) {
            calculatedPadding = lineOverlapPadding;
            paint.setColor(context.getResources().getColor(R.color.tools_annot_edit_line_shadow));
            paint.setStrokeWidth(thickness);
        } else {
            calculatedPadding = lineOverlapPadding + padding;
            paint.setColor(context.getResources().getColor(R.color.tools_annot_edit_line_shadow_no_permission));
            paint.setPathEffect(dashPathEffect);
            paint.setStrokeWidth(thickness * 1.1f); // dash effect makes line look thinner, so increase the stroke width
        }
        canvas.drawRoundRect(
                new RectF(left - calculatedPadding,
                        top - calculatedPadding,
                        right + calculatedPadding,
                        bottom + calculatedPadding),
                cornerRad,
                cornerRad,
                paint
        );
    }

    public static void drawCtrlPtsLine(@NonNull Resources resources, @NonNull Canvas canvas,
            @NonNull Paint paint, @NonNull PointF pt1, @NonNull PointF pt2,
            float radius, boolean hasPermission) {
        float left = pt1.x;
        float bottom = pt1.y;
        float right = pt2.x;
        float top = pt2.y;

        paint.setColor(resources.getColor(R.color.tools_selection_control_point));
        paint.setStyle(Paint.Style.FILL);
        if (hasPermission) {
            canvas.drawCircle(left, bottom, radius, paint);
            canvas.drawCircle(right, top, radius, paint);
        }

        paint.setColor(resources.getColor(R.color.tools_selection_control_point_border));
        paint.setStyle(Paint.Style.STROKE);
        if (hasPermission) {
            canvas.drawCircle(left, bottom, radius, paint);
            canvas.drawCircle(right, top, radius, paint);
        }
    }

    public static void drawCtrlPts(@NonNull Resources resources, @NonNull Canvas canvas,
            @NonNull Paint paint, @NonNull PointF pt1, @NonNull PointF pt2,
            @NonNull PointF midH, @NonNull PointF midV,
            float radius, boolean hasPermission,
            boolean maintainAspectRatio) {
        float left = Math.min(pt1.x, pt2.x);
        float right = Math.max(pt1.x, pt2.x);
        float top = Math.min(pt1.y, pt2.y);
        float bottom = Math.max(pt1.y, pt2.y);

        float middle_x = midH.x;
        float middle_y = midV.y;

        // Control point fill color
        paint.setColor(resources.getColor(R.color.tools_selection_control_point));
        paint.setStyle(Paint.Style.FILL);
        if (hasPermission) {
            canvas.drawCircle(left, bottom, radius, paint);
            canvas.drawCircle(right, bottom, radius, paint);
            canvas.drawCircle(right, top, radius, paint);
            canvas.drawCircle(left, top, radius, paint);
        }
        // if maintain aspect ratio is false, draw middle control pts
        if (!maintainAspectRatio) {
            if (hasPermission) {
                canvas.drawCircle(middle_x, bottom, radius, paint);
                canvas.drawCircle(right, middle_y, radius, paint);
                canvas.drawCircle(middle_x, top, radius, paint);
                canvas.drawCircle(left, middle_y, radius, paint);
            }
        }

        // Control point border
        paint.setColor(resources.getColor(R.color.tools_selection_control_point_border));
        paint.setStyle(Paint.Style.STROKE);
        if (hasPermission) {
            canvas.drawCircle(left, bottom, radius, paint);
            canvas.drawCircle(right, bottom, radius, paint);
            canvas.drawCircle(right, top, radius, paint);
            canvas.drawCircle(left, top, radius, paint);
        }
        // if maintain aspect ratio is false, draw middle control pts
        if (!maintainAspectRatio) {
            if (hasPermission) {
                canvas.drawCircle(middle_x, bottom, radius, paint);
                canvas.drawCircle(right, middle_y, radius, paint);
                canvas.drawCircle(middle_x, top, radius, paint);
                canvas.drawCircle(left, middle_y, radius, paint);
            }
        }
    }

    public static void drawCtrlPtsAdvancedShape(@NonNull Resources resources, @NonNull Canvas canvas,
            @NonNull Paint paint, @NonNull PointF[] ctrlPts,
            float radius, boolean hasPermission,
            boolean skipEndPoint) {
        // Control point fill color
        paint.setColor(resources.getColor(R.color.tools_selection_control_point));
        paint.setStyle(Paint.Style.FILL);
        if (hasPermission) {
            for (int i = 0; i < ctrlPts.length; i++) {
                if (skipEndPoint && i == AnnotEditAdvancedShape.CALLOUT_END_POINT_INDEX) {
                    // for callout we want to skip the end point (3rd point)
                    continue;
                }
                PointF pt = ctrlPts[i];
                if (pt != null) {
                    canvas.drawCircle(pt.x, pt.y, radius, paint);
                }
            }
        }

        // Control point border
        paint.setColor(resources.getColor(R.color.tools_selection_control_point_border));
        paint.setStyle(Paint.Style.STROKE);
        if (hasPermission) {
            for (int i = 0; i < ctrlPts.length; i++) {
                if (skipEndPoint && i == AnnotEditAdvancedShape.CALLOUT_END_POINT_INDEX) {
                    // for callout we want to skip the end point (3rd point)
                    continue;
                }
                PointF pt = ctrlPts[i];
                if (pt != null) {
                    canvas.drawCircle(pt.x, pt.y, radius, paint);
                }
            }
        }
    }

    private static float xWithRotation(float x, float y, float width, float height, int degree) {
        double rad = Math.toRadians(degree);
        return (float) (x + height * Math.sin(rad) + width * Math.cos(rad));
    }

    private static float yWithRotation(float x, float y, float width, float height, int degree) {
        double rad = Math.toRadians(degree);
        return (float) (y + height * Math.cos(rad) - width * Math.sin(rad));
    }

    private static void drawDashedLine(@NonNull Canvas canvas, @NonNull Path path,
            float startX, float startY, float stopX, float stopY,
            @NonNull Paint paint) {
        path.moveTo(startX, startY);
        path.lineTo(stopX, stopY);

        canvas.drawPath(path, paint);
    }

    /**
     * Degree in counter-clockwise
     */
    public static void drawGuideline(int degree, float radius, @NonNull Canvas canvas,
            @NonNull RectF bbox, @NonNull Path path, @NonNull Paint paint) {
        float centerX = bbox.centerX();
        float centerY = bbox.centerY();
        float maxSize = Math.max(bbox.width(), bbox.height());
        float size = maxSize / 4 * 3;
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(centerX, centerY, radius, paint);
        paint.setStyle(Paint.Style.STROKE);
        path.reset();

        drawDashedLine(canvas, path, centerX, centerY, centerX + size, centerY, paint); // baseline exists for all angles
        if (degree == 90) {
            drawDashedLine(canvas, path, centerX, centerY - size, centerX, centerY, paint);
        } else if (degree == 180) {
            drawDashedLine(canvas, path, centerX - size, centerY, centerX, centerY, paint);
        } else if (degree == -90) {
            drawDashedLine(canvas, path, centerX, centerY, centerX, centerY + size, paint);
        }
        if (degree == 45 || degree == 135 || degree == 225 || degree == -45) {
            drawDashedLine(canvas, path, centerX, centerY,
                    xWithRotation(centerX, centerY, size, 0, degree),
                    yWithRotation(centerX, centerY, size, 0, degree),
                    paint);
        }
    }

    public static void drawInk(@NonNull PDFViewCtrl pdfViewCtrl, @NonNull Canvas canvas,
            @NonNull ArrayList<InkItem> inks, boolean flinging) {
        drawInk(pdfViewCtrl, canvas, inks, flinging, null, 1, 1, false);
    }

    public static void drawInk(@NonNull PDFViewCtrl pdfViewCtrl, @NonNull Canvas canvas,
            @NonNull ArrayList<InkItem> inks, boolean flinging,
            @Nullable PointF offset, float scaleX, float scaleY, boolean forceRecalculate) {
        for (InkItem ink : inks) {
            if (!pdfViewCtrl.isContinuousPagePresentationMode(pdfViewCtrl.getPagePresentationMode())
                    && !isPageInPages(pdfViewCtrl.getVisiblePagesInTransition(), ink.mPageForFreehandAnnot)) {
                // if ink page is not visible, do not draw
                continue;
            }

            // recalculate the drawing points from page points if:
            // - we are not currently flinging (the scroll is settled)
            // - the zoom has changed
            // - the eraser has removed some of the page points
            float oldThickness = ink.mPaint.getStrokeWidth();
            float newThickness = (float) (pdfViewCtrl.getZoom() * ink.mThickness);
            if ((!flinging && (oldThickness != newThickness || ink.mDirtyDrawingPts)) || forceRecalculate) {
                ink.mPaint.setStrokeWidth(newThickness);
                ink.mDrawingStrokes = FreehandCreate.createDrawingStrokesFromPageStrokes(pdfViewCtrl, ink.mPageStrokes, ink.mStylusUsed, ink.mPageForFreehandAnnot, offset, scaleX, scaleY);
                ink.mDirtyPaths = true;
                ink.mDirtyDrawingPts = false;
            }

            // Draw each stroke. Each stroke has to be its own path,
            // as transparent strokes (opacity < 1) should look like
            // they overlap.
            // If the paths need to be recalculated, recalculate and
            // then draw them; otherwise, use the pre-calculated paths.
            Path currentInkPath;
            if (ink.mDirtyPaths && !ink.mDirtyDrawingPts) {
                PathPool.getInstance().recycle(ink.mPaths);
                ink.mPaths.clear();
                for (ArrayList<PointF> drawingPoints : ink.mDrawingStrokes) {
                    currentInkPath = createPathFromDrawingPts(drawingPoints, ink.mStylusUsed);
                    ink.mPaths.add(currentInkPath);
                }
                ink.mDirtyPaths = false;
            }
            for (Path path : ink.mPaths) {
                if (pdfViewCtrl.isMaintainZoomEnabled()) {
                    canvas.save();
                    try {
                        canvas.translate(0, -pdfViewCtrl.getScrollYOffsetInTools(ink.mPageForFreehandAnnot));
                        canvas.drawPath(path, ink.mPaint);
                    } finally {
                        canvas.restore();
                    }
                } else {
                    canvas.drawPath(path, ink.mPaint);
                }
            }
        }
    }

    private static Path createPathFromDrawingPts(ArrayList<PointF> points, boolean isStylus) {
        Path path = PathPool.getInstance().obtain();
        if (points.size() <= 1) {
            return path;
        }

        if (isStylus) {
            path.moveTo(points.get(0).x, points.get(0).y);
            for (PointF point : points) {
                path.lineTo(point.x, point.y);
            }
        } else {
            path.moveTo(points.get(0).x, points.get(0).y);
            for (int i = 1, cnt = points.size(); i < cnt; i += 3) {
                path.cubicTo(points.get(i).x, points.get(i).y, points.get(i + 1).x,
                        points.get(i + 1).y, points.get(i + 2).x, points.get(i + 2).y);
            }
        }
        return path;
    }

    private static boolean isPageInPages(int[] pages, int page) {
        for (int p : pages) {
            if (p == page) {
                return true;
            }
        }
        return false;
    }

    public static void drawRectangle(@NonNull Canvas canvas,
            @NonNull PointF pt1, @NonNull PointF pt2,
            float thicknessDraw,
            int fillColor, int strokeColor,
            @NonNull Paint fillPaint, @NonNull Paint paint) {
        float min_x = Math.min(pt1.x, pt2.x);
        float max_x = Math.max(pt1.x, pt2.x);
        float min_y = Math.min(pt1.y, pt2.y);
        float max_y = Math.max(pt1.y, pt2.y);

        // Android aligns in the middle of the line, while PDFNet aligns along the outer boundary;
        // so need to adjust the temporary shape drawn.
        float adjust = thicknessDraw / 2;

        if (fillColor != Color.TRANSPARENT) {
            canvas.drawRect(min_x + thicknessDraw, min_y + thicknessDraw,
                    max_x - thicknessDraw, max_y - thicknessDraw, fillPaint);
        }
        if (strokeColor != Color.TRANSPARENT) {
            canvas.drawRect(min_x + adjust, min_y + adjust,
                    max_x - adjust, max_y - adjust, paint);
        }
    }

    public static void drawOval(@NonNull Canvas canvas,
            @NonNull PointF pt1, @NonNull PointF pt2,
            float thicknessDraw,
            @NonNull RectF oval,
            int fillColor, int strokeColor,
            @NonNull Paint fillPaint, @NonNull Paint paint) {
        float min_x = Math.min(pt1.x, pt2.x);
        float max_x = Math.max(pt1.x, pt2.x);
        float min_y = Math.min(pt1.y, pt2.y);
        float max_y = Math.max(pt1.y, pt2.y);

        // Android aligns in the middle of a line, while PDFNet aligns along the outer boundary;
        // so need to adjust the temporary shape drawn.
        float adjust = thicknessDraw / 2;
        min_x += adjust;
        max_x -= adjust;
        min_y += adjust;
        max_y -= adjust;

        oval.set(min_x, min_y, max_x, max_y);
        if (fillColor != Color.TRANSPARENT) {
            canvas.drawArc(oval, 0, 360, false, fillPaint);
        }
        if (strokeColor != Color.TRANSPARENT) {
            canvas.drawArc(oval, 0, 360, false, paint);
        }
    }

    public static void drawLine(Canvas canvas, PointF pt1, PointF pt2, Paint paint) {
        canvas.drawLine(pt1.x, pt1.y, pt2.x, pt2.y, paint);
    }

    public static void drawArrow(Canvas canvas, PointF pt1, PointF pt2,
            PointF pt3, PointF pt4,
            Path path, Paint paint) {
        path.reset();
        // draw the line
        path.moveTo(pt1.x, pt1.y);
        path.lineTo(pt2.x, pt2.y);

        // draw the arrow
        path.moveTo(pt3.x, pt3.y);
        path.lineTo(pt2.x, pt2.y);
        path.lineTo(pt4.x, pt4.y);

        canvas.drawPath(path, paint);
    }

    public static void calcArrow(@NonNull PointF pt1, @NonNull PointF pt2,
            @NonNull PointF pt3, @NonNull PointF pt4,
            float thickness, double zoom) {
        // mPt1 and mPt2 are the first touch-down point and the moving point,
        // and mPt3 and mPt4 are the end points of the arrow's two shorter lines.

        pt3.set(pt2);
        pt4.set(pt2);

        double lineangle = Math.atan2((pt2.y - pt1.y), (pt2.x - pt1.x));
        double revangle = lineangle > Math.PI ? lineangle - Math.PI : lineangle + Math.PI;
        double phi1, phi2, ax, ay, len, bx, by;
        len = getLenOfLine(pt1, pt2, thickness, zoom, false);

        phi1 = revangle + Math.PI / 6.0;
        phi2 = revangle - Math.PI / 6.0;
        ax = pt2.x + len * Math.cos(phi1);
        ay = pt2.y + len * Math.sin(phi1);

        bx = pt2.x + len * Math.cos(phi2);
        by = pt2.y + len * Math.sin(phi2);

        pt3.set((float) ax, (float) ay);
        pt4.set((float) bx, (float) by);
    }

    private static double getLenOfLine(@NonNull PointF pt1, @NonNull PointF pt2,
            float thickness, double zoom, boolean halfALine) {
        double len = 5 * thickness + 2;
        len = len * zoom;
        double constant = 0.35;
        if (halfALine) {
            constant = 0.7;
        }
        len = Math.min(distance(pt1, pt2) * constant, len);
        return len;
    }

    public static double distance(@NonNull PointF p1, @NonNull PointF p2) {
        double w = p1.x - p2.x;
        double h = p1.y - p2.y;
        return Math.sqrt(w * w + h * h);
    }

    public static void drawRuler(@NonNull Canvas canvas, @NonNull PointF pt1, @NonNull PointF pt2,
            @NonNull PointF pt3, @NonNull PointF pt4,
            @NonNull PointF pt5, @NonNull PointF pt6,
            @NonNull Path path, @NonNull Paint paint, @NonNull String text, double zoom) {
        path.reset();
        // draw the line
        path.moveTo(pt1.x, pt1.y);
        path.lineTo(pt2.x, pt2.y);

        // butt A
        path.moveTo(pt3.x, pt3.y);
        path.lineTo(pt4.x, pt4.y);

        // butt B
        path.moveTo(pt5.x, pt5.y);
        path.lineTo(pt6.x, pt6.y);

        canvas.drawPath(path, paint);

        // text
        float width = paint.getStrokeWidth();
        paint.setStrokeWidth(0);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize((float) (12 * zoom));
        double yOffset = -4 * zoom;
        canvas.drawTextOnPath(text, path, 0, (float) yOffset, paint);
        paint.setStrokeWidth(width);
    }

    public static void calcRuler(@NonNull PointF pt1, @NonNull PointF pt2,
            @NonNull PointF pt3, @NonNull PointF pt4,
            @NonNull PointF pt5, @NonNull PointF pt6,
            float thickness, double zoom) {
        double len = getLenOfLine(pt1, pt2, thickness, zoom, true);

        Vec2 start = new Vec2(pt1.x, pt1.y);
        Vec2 end = new Vec2(pt2.x, pt2.y);
        PointF midPt = midpoint(pt1, pt2);
        Vec2 mid = new Vec2(midPt.x, midPt.y);

        // first end
        calRulerButt(mid, start, len, pt3, pt4);

        // second end
        calRulerButt(mid, end, len, pt5, pt6);
    }

    private static void calRulerButt(@NonNull Vec2 start, @NonNull Vec2 end, double len,
            @NonNull PointF pt1, @NonNull PointF pt2) {
        Vec2 diff = Vec2.subtract(end, start);
        double lineLen = diff.length();
        double max = Math.max(lineLen, 1.0 / 72.0);
        Vec2 line = Vec2.multiply(diff, 1.0 / max);
        Vec2 line90 = line.getPerp();

        Vec2 temp = Vec2.multiply(line90, len * 0.5);
        pt1.set(Vec2.subtract(end, temp).toPointF());
        pt2.set(Vec2.add(end, temp).toPointF());
    }

    public static PointF midpoint(@NonNull PointF pt1, @NonNull PointF pt2) {
        return new PointF((pt1.x + pt2.x) / 2, (pt1.y + pt2.y) / 2);
    }

    public static void drawPolyline(@NonNull PDFViewCtrl pdfViewCtrl, int pageNum, @NonNull Canvas canvas,
            @NonNull ArrayList<PointF> canvasPoints, @NonNull Path path,
            @NonNull Paint paint, int strokeColor) {
        path.reset();
        PointF startPoint = null;
        for (PointF point : canvasPoints) {
            if (startPoint != null) {
                path.lineTo(point.x, point.y);
            } else {
                startPoint = point;
                path.moveTo(point.x, point.y);
            }
        }
        if (startPoint == null) {
            return;
        }

        if (strokeColor != Color.TRANSPARENT) {
            if (pdfViewCtrl.isMaintainZoomEnabled()) {
                canvas.save();
                try {
                    canvas.translate(0, -pdfViewCtrl.getScrollYOffsetInTools(pageNum));
                    canvas.drawPath(path, paint);
                } finally {
                    canvas.restore();
                }
            } else {
                canvas.drawPath(path, paint);
            }
        }
    }

    public static void drawPolygon(@NonNull PDFViewCtrl pdfViewCtrl, int pageNum, @NonNull Canvas canvas,
            @NonNull ArrayList<PointF> canvasPoints, @NonNull Path path,
            @NonNull Paint paint, int strokeColor,
            @NonNull Paint fillPaint, int fillColor) {
        path.reset();
        PointF startPoint = null;
        for (PointF point : canvasPoints) {
            if (startPoint != null) {
                path.lineTo(point.x, point.y);
            } else {
                startPoint = point;
                path.moveTo(point.x, point.y);
            }
        }
        if (startPoint == null) {
            return;
        }
        path.lineTo(startPoint.x, startPoint.y);

        if (pdfViewCtrl.isMaintainZoomEnabled()) {
            canvas.save();
            try {
                canvas.translate(0, -pdfViewCtrl.getScrollYOffsetInTools(pageNum));
                drawPolygonHelper(canvas, path, paint, strokeColor, fillPaint, fillColor);
            } finally {
                canvas.restore();
            }
        } else {
            drawPolygonHelper(canvas, path, paint, strokeColor, fillPaint, fillColor);
        }
    }

    public static void drawCloud(@NonNull PDFViewCtrl pdfViewCtrl, int pageNum, @NonNull Canvas canvas,
            @NonNull ArrayList<PointF> canvasPoints, @NonNull Path path,
            @NonNull Paint paint, int strokeColor,
            @NonNull Paint fillPaint, int fillColor, double borderIntensity) {
        ArrayList<PointF> poly = CloudCreate.getClosedPoly(canvasPoints);
        int size = poly.size();
        if (size < 3) {
            return;
        }

        final double SAME_VERTEX_TH = 1.0 / 8192.0;
        if (borderIntensity < 0.1) {
            borderIntensity = CloudCreate.BORDER_INTENSITY;
        }
        borderIntensity *= pdfViewCtrl.getZoom();
        final boolean clockwise = CloudCreate.IsPolyWrapClockwise(poly);
        final double sweepDirection = clockwise ? -1.0 : 1.0;
        final double maxCloudSize = 8 * borderIntensity;

        double lastCloudSize = maxCloudSize;
        double firstCloudSize = maxCloudSize;
        double edgeDegrees = 0.0;
        PointF firstPos = poly.get(0);
        PointF lastEdge = CloudCreate.subtract(poly.get(0), poly.get(size - 2));
        boolean useLargeFirstArc = true;
        boolean hasFirstPoint = false;
        path.reset();
        float startX = 0, startY = 0;

        for (int i = 0; i < size - 1; ++i) {
            PointF pos = poly.get(i);
            PointF edge = CloudCreate.subtract(poly.get(i + 1), pos);
            double length = edge.length();
            // avoid division by 0 from duplicated points.
            if (length <= SAME_VERTEX_TH) {
                continue;
            }

            // split the edge into some integral number of clouds
            PointF direction = CloudCreate.divide(edge, length);
            int numClouds = (int) Math.max(Math.floor(length / maxCloudSize), 1);
            double cloudSize = length / numClouds;
            double edgeAngle = Math.atan2(direction.y, direction.x); // angle from x-axis

            // back start position out to before the vertex
            // as we're going to increment before using it
            pos = CloudCreate.subtract(pos, CloudCreate.multiply(direction, cloudSize * .5));

            // which direction are we turning on this vertex?
            double cross = CloudCreate.cross(lastEdge, edge);

            int c = 0;
            if (!hasFirstPoint) {
                // skip the first iteration for the first leg (we'll complete it at the end)
                ++c;
                firstCloudSize = cloudSize;
                useLargeFirstArc = (cross * sweepDirection) < 0;
                pos = CloudCreate.add(pos, CloudCreate.multiply(direction, cloudSize));
                firstPos = pos;
                // start the curve
                path.moveTo(firstPos.x, firstPos.y);
                startX = firstPos.x;
                startY = firstPos.y;
                hasFirstPoint = true;
            }
            // for the first iteration, combine the radius with the previous edge
            double radius = (lastCloudSize + cloudSize) * 0.25;
            for (; c < numClouds; ++c) {
                if (c == 1) {
                    // on the second iteration on, we can use values exclusive to this edge
                    edgeDegrees = CloudCreate.toDegreesMod360(edgeAngle);
                    radius = cloudSize * 0.5;
                }
                pos = CloudCreate.add(pos, CloudCreate.multiply(direction, cloudSize));
                boolean useLargeArc = (c == 0 && (cross * sweepDirection) < 0);
                PointF point = CloudCreate.arcTo(path, startX, startY, radius, radius, edgeDegrees, useLargeArc, clockwise, pos.x, pos.y);
                startX = point.x;
                startY = point.y;
            }
            edgeDegrees = CloudCreate.toDegreesMod360(edgeAngle);
            lastEdge = edge;
            lastCloudSize = cloudSize;
        }
        if (!hasFirstPoint) {
            path.moveTo(firstPos.x, firstPos.y);
            startX = firstPos.x;
            startY = firstPos.y;
        }
        double closingRadius = (firstCloudSize + lastCloudSize) * 0.25;
        // now we close the poly, using the values we saved on the first vertex.
        CloudCreate.arcTo(path, startX, startY, closingRadius, closingRadius, edgeDegrees,
                useLargeFirstArc, clockwise, firstPos.x, firstPos.y);

        if (pdfViewCtrl.isMaintainZoomEnabled()) {
            canvas.save();
            try {
                canvas.translate(0, -pdfViewCtrl.getScrollYOffsetInTools(pageNum));
                drawPolygonHelper(canvas, path, paint, strokeColor, fillPaint, fillColor);
            } finally {
                canvas.restore();
            }
        } else {
            drawPolygonHelper(canvas, path, paint, strokeColor, fillPaint, fillColor);
        }
    }

    private static void drawPolygonHelper(@NonNull Canvas canvas, @NonNull Path path,
            @NonNull Paint paint, int strokeColor,
            @NonNull Paint fillPaint, int fillColor) {
        if (fillColor != Color.TRANSPARENT) {
            canvas.drawPath(path, fillPaint);
        }
        if (strokeColor != Color.TRANSPARENT) {
            canvas.drawPath(path, paint);
        }
    }
}
