//---------------------------------------------------------------------------------------
// Copyright (c) 2001-2019 by PDFTron Systems Inc. All Rights Reserved.
// Consult legal.txt regarding legal and license information.
//---------------------------------------------------------------------------------------

package com.pdftron.pdf.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.RectF;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.pdftron.common.PDFNetException;
import com.pdftron.pdf.Annot;
import com.pdftron.pdf.ColorPt;
import com.pdftron.pdf.ColorSpace;
import com.pdftron.pdf.Element;
import com.pdftron.pdf.ElementReader;
import com.pdftron.pdf.ElementWriter;
import com.pdftron.pdf.Font;
import com.pdftron.pdf.PDFDoc;
import com.pdftron.pdf.PDFDraw;
import com.pdftron.pdf.PDFNet;
import com.pdftron.pdf.PDFViewCtrl;
import com.pdftron.pdf.Page;
import com.pdftron.pdf.PageIterator;
import com.pdftron.pdf.Point;
import com.pdftron.pdf.QuadPoint;
import com.pdftron.pdf.Rect;
import com.pdftron.pdf.annots.FreeText;
import com.pdftron.pdf.annots.Ink;
import com.pdftron.pdf.annots.Line;
import com.pdftron.pdf.annots.Markup;
import com.pdftron.pdf.annots.PolyLine;
import com.pdftron.pdf.annots.Polygon;
import com.pdftron.pdf.annots.Popup;
import com.pdftron.pdf.annots.Redaction;
import com.pdftron.pdf.annots.RubberStamp;
import com.pdftron.pdf.annots.Text;
import com.pdftron.pdf.model.AnnotStyle;
import com.pdftron.pdf.model.FontResource;
import com.pdftron.pdf.model.FreeTextCacheStruct;
import com.pdftron.pdf.model.RulerItem;
import com.pdftron.pdf.tools.R;
import com.pdftron.pdf.tools.SoundCreate;
import com.pdftron.pdf.tools.Stamper;
import com.pdftron.pdf.tools.Tool;
import com.pdftron.pdf.tools.ToolManager;
import com.pdftron.sdf.DictIterator;
import com.pdftron.sdf.Obj;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

import io.reactivex.Completable;
import io.reactivex.functions.Action;

/**
 * A utility class for handling annotation.
 */
public class AnnotUtils {

    public static String KEY_IRT = "IRT";
    public static String KEY_RT = "RT";
    public static String KEY_NM = "NM";

    /**
     * Flattens an annotation. A document lock is required around this method.
     */
    public static void flattenAnnot(@NonNull PDFViewCtrl pdfViewCtrl, @NonNull Annot annot, int pageNum) throws PDFNetException {
        PDFDoc pdfDoc = pdfViewCtrl.getDoc();
        // flatten annotation
        Rect screenRect = pdfViewCtrl.getScreenRectForAnnot(annot, pageNum);
        Page page = pdfDoc.getPage(pageNum);
        annot.flatten(page);
        if (pdfViewCtrl.isAnnotationLayerEnabled()) {
            pdfViewCtrl.update(annot, pageNum);
        }
        pdfViewCtrl.update(screenRect);
    }

    public static Completable loadSystemFonts() {
        return Completable.fromAction(new Action() {
            @Override
            public void run() throws Exception {
                try {
                    PDFNet.getSystemFontList();
                } catch (PDFNetException e) {
                    AnalyticsHandlerAdapter.getInstance().sendException(e);
                }
            }
        });
    }

    /**
     * If the sticky note has a custom icon, this will set the appearance of the icon and return true.
     * If the sticky note does not have a custom icon, it will return false.
     *
     * @param context The Context
     * @param annot   The annotation
     * @return True if the sticky note icon has been changed successfully
     */
    private static boolean refreshCustomStickyNoteAppearance(
            @NonNull Context context,
            @NonNull Annot annot) {

        InputStream fis = null;
        PDFDoc template = null;
        ElementReader reader = null;
        ElementWriter writer = null;
        try {
            // get icon name
            Text text = new Text(annot);
            String iconName = text.getIconName();

            // Open pdf containing custom sticky note icons. Each page is a different custom icon
            // with the page label the icon's name.
            fis = context.getResources().openRawResource(R.raw.stickynote_icons);
            template = new PDFDoc(fis);

            // Loop through all pages, checking if the icon name equals the page label name.
            // If none of the page labels equals the icon name, then return false - the sticky note
            // icon is not a custom icon.
            for (int pageNum = 1, pageCount = template.getPageCount(); pageNum <= pageCount; ++pageNum) {
                if (iconName.equalsIgnoreCase(template.getPageLabel(pageNum).getPrefix())) {
                    Page iconPage = template.getPage(pageNum);
                    com.pdftron.sdf.Obj contents = iconPage.getContents();
                    com.pdftron.sdf.Obj importedContents = annot.getSDFObj().getDoc().importObj(contents, true);
                    com.pdftron.pdf.Rect bbox = iconPage.getMediaBox();
                    importedContents.putRect("BBox", bbox.getX1(), bbox.getY1(), bbox.getX2(), bbox.getY2());
                    importedContents.putName("Subtype", "Form");
                    importedContents.putName("Type", "XObject");
                    reader = new ElementReader();
                    writer = new ElementWriter();
                    reader.begin(importedContents);
                    writer.begin(importedContents, true);
                    ColorPt rgbColor = text.getColorAsRGB();
                    double opacity = text.getOpacity();
                    for (Element element = reader.next(); element != null; element = reader.next()) {
                        if (element.getType() == Element.e_path && !element.isClippingPath()) {
                            element.getGState().setFillColorSpace(ColorSpace.createDeviceRGB());
                            element.getGState().setFillColor(rgbColor);
                            element.getGState().setFillOpacity(opacity);
                            element.getGState().setStrokeOpacity(opacity);
                            element.setPathStroke(true);
                            element.setPathFill(true);
                        }
                        writer.writeElement(element);
                    }
                    reader.end();
                    writer.end();

                    // set the appearance of sticky note icon to the custom icon
                    text.setAppearance(importedContents);

                    // keep analytics for the icon
                    AnalyticsHandlerAdapter.getInstance().sendEvent(AnalyticsHandlerAdapter.CATEGORY_ANNOTATIONTOOLBAR,
                            "sticky note icon: " + iconName, AnalyticsHandlerAdapter.LABEL_STYLEEDITOR);
                    return true;
                }
            }
        } catch (Exception e) {
            AnalyticsHandlerAdapter.getInstance().sendException(e);
        } finally {
            if (reader != null) {
                try {
                    reader.destroy();
                } catch (Exception ignored) {

                }
            }
            if (writer != null) {
                try {
                    writer.destroy();
                } catch (Exception ignored) {

                }
            }
            Utils.closeQuietly(template);
            Utils.closeQuietly(fis);
        }

        return false;
    }

    /**
     * If the sticky note has a custom icon, this will set the appearance of the icon and return true.
     * If the sticky note does not have a custom icon, it will return false.
     *
     * @param context The Context
     * @param annot   The annotation
     * @return True if the sticky note icon has been changed successfully
     */
    private static boolean refreshCustomStampAppearance(
            @NonNull Context context,
            @NonNull Annot annot) {

        InputStream fis = null;
        PDFDoc template = null;
        try {
            // get icon name
            RubberStamp stamp = new RubberStamp(annot);
            String iconName = stamp.getIconName();

            // Open pdf containing custom rubber stamp icons. Each page is a different custom icon
            // with the page label the icon's name.
            fis = context.getResources().openRawResource(R.raw.stamps_icons);
            template = new PDFDoc(fis);

            // Loop through all pages, checking if the icon name equals the page label name.
            // If none of the page labels equals the icon name, then return false - the rubber stamp
            // icon is not a custom icon.
            for (int pageNum = 1, pageCount = template.getPageCount(); pageNum <= pageCount; ++pageNum) {
                if (iconName.equalsIgnoreCase(template.getPageLabel(pageNum).getPrefix())) {
                    Page iconPage = template.getPage(pageNum);
                    com.pdftron.sdf.Obj contents = iconPage.getContents();
                    com.pdftron.sdf.Obj importedContents = annot.getSDFObj().getDoc().importObj(contents, true);
                    com.pdftron.pdf.Rect bbox = iconPage.getMediaBox();
                    importedContents.putRect("BBox", bbox.getX1(), bbox.getY1(), bbox.getX2(), bbox.getY2());
                    importedContents.putName("Subtype", "Form");
                    importedContents.putName("Type", "XObject");

                    // insert background color
                    com.pdftron.sdf.Obj res = iconPage.getResourceDict();
                    if (res != null) {
                        com.pdftron.sdf.Obj importedRes = annot.getSDFObj().getDoc().importObj(res, true);
                        importedContents.put("Resources", importedRes);
                    }

                    // set the appearance of rubber stamp icon to the custom icon
                    stamp.setAppearance(importedContents);

                    // keep analytics for the icon
                    AnalyticsHandlerAdapter.getInstance().sendEvent(AnalyticsHandlerAdapter.CATEGORY_ANNOTATIONTOOLBAR,
                            "rubber stamp icon: " + iconName, AnalyticsHandlerAdapter.LABEL_STYLEEDITOR);
                    return true;
                }
            }
        } catch (Exception e) {
            AnalyticsHandlerAdapter.getInstance().sendException(e);
        } finally {
            Utils.closeQuietly(template);
            Utils.closeQuietly(fis);
        }

        return false;
    }

    /**
     * helper function to refresh annotation appearance. Note that some
     * annotations may have their own custom appearance such as Text annotation.
     *
     * @param context The Context
     * @param annot   The annotation
     * @throws PDFNetException PDFNet exception
     */
    public static void refreshAnnotAppearance(
            @NonNull Context context,
            @NonNull Annot annot)
            throws PDFNetException {

        switch (annot.getType()) {
            case Annot.e_Text:
                // if the icon is not a custom icon, then call refresh appearance.
                // if the icon is a custom icon, refreshCustomStickyNoteAppearance will set the appearance
                if (!refreshCustomStickyNoteAppearance(context, annot)) {
                    annot.refreshAppearance();
                }
                break;
            case Annot.e_Stamp:
                if (!refreshCustomStampAppearance(context, annot)) {
                    annot.refreshAppearance();
                }
                break;
            default:
                annot.refreshAppearance();
        }
    }

    /**
     * Gets the page corresponding to a certain stamp in our stamps repository.
     *
     * @param context   The context
     * @param stampName The name of stamp icon
     * @return The page size; null if cannot find the stamp
     */
    @Nullable
    public static double[] getStampSize(
            @NonNull Context context,
            @NonNull String stampName) {

        InputStream fis = null;
        PDFDoc template = null;
        try {
            fis = context.getResources().openRawResource(R.raw.stamps_icons);
            template = new PDFDoc(fis);

            for (int pageNum = 1, pageCount = template.getPageCount(); pageNum <= pageCount; ++pageNum) {
                if (stampName.equalsIgnoreCase(template.getPageLabel(pageNum).getPrefix())) {
                    Page page = template.getPage(pageNum);
                    double[] size = new double[2];
                    size[0] = page.getPageWidth();
                    size[1] = page.getPageHeight();
                    return size;
                }
            }
        } catch (Exception e) {
            AnalyticsHandlerAdapter.getInstance().sendException(e);
        } finally {
            Utils.closeQuietly(template);
            Utils.closeQuietly(fis);
        }

        return null;
    }

    /**
     * Returns annotation type as string.
     *
     * @param context The context
     * @param typeId  The annotation type ID
     * @return The annotation type as string
     */
    static public String getAnnotTypeAsString(
            @NonNull Context context,
            int typeId) {

        switch (typeId) {
            case Annot.e_Text:
                return context.getResources().getString(R.string.annot_text).toLowerCase();
            case Annot.e_Link:
                return context.getResources().getString(R.string.annot_link).toLowerCase();
            case Annot.e_FreeText:
                return context.getResources().getString(R.string.annot_free_text).toLowerCase();
            case AnnotStyle.CUSTOM_ANNOT_TYPE_CALLOUT:
                return context.getResources().getString(R.string.annot_callout).toLowerCase();
            case Annot.e_Line:
                return context.getResources().getString(R.string.annot_line).toLowerCase();
            case Annot.e_Square:
                return context.getResources().getString(R.string.annot_square).toLowerCase();
            case Annot.e_Circle:
                return context.getResources().getString(R.string.annot_circle).toLowerCase();
            case Annot.e_Polygon:
                return context.getResources().getString(R.string.annot_polygon).toLowerCase();
            case Annot.e_Polyline:
                return context.getResources().getString(R.string.annot_polyline).toLowerCase();
            case Annot.e_Highlight:
                return context.getResources().getString(R.string.annot_highlight).toLowerCase();
            case Annot.e_Underline:
                return context.getResources().getString(R.string.annot_underline).toLowerCase();
            case Annot.e_Squiggly:
                return context.getResources().getString(R.string.annot_squiggly).toLowerCase();
            case Annot.e_StrikeOut:
                return context.getResources().getString(R.string.annot_strikeout).toLowerCase();
            case Annot.e_Stamp:
                return context.getResources().getString(R.string.annot_stamp).toLowerCase();
            case Annot.e_Caret:
                return context.getResources().getString(R.string.annot_caret).toLowerCase();
            case Annot.e_Ink:
                return context.getResources().getString(R.string.annot_ink).toLowerCase();
            case Annot.e_Redact:
                return context.getResources().getString(R.string.annot_redaction).toLowerCase();
            case AnnotStyle.CUSTOM_ANNOT_TYPE_SIGNATURE:
                return context.getResources().getString(R.string.annot_signature).toLowerCase();
            case AnnotStyle.CUSTOM_ANNOT_TYPE_ARROW:
                return context.getResources().getString(R.string.annot_arrow).toLowerCase();
            case AnnotStyle.CUSTOM_ANNOT_TYPE_RULER:
                return context.getResources().getString(R.string.annot_ruler).toLowerCase();
            case AnnotStyle.CUSTOM_ANNOT_TYPE_FREE_HIGHLIGHTER:
                return context.getResources().getString(R.string.annot_free_highlight).toLowerCase();
            case AnnotStyle.CUSTOM_ANNOT_TYPE_CLOUD:
                return context.getResources().getString(R.string.annot_cloud).toLowerCase();
            case Annot.e_FileAttachment:
                return context.getResources().getString(R.string.annot_file_attachment).toLowerCase();
            case Annot.e_Sound:
                return context.getResources().getString(R.string.annot_sound).toLowerCase();
            case AnnotStyle.CUSTOM_ANNOT_TYPE_PERIMETER_MEASURE:
                return context.getResources().getString(R.string.annot_perimeter_measure).toLowerCase();
            case AnnotStyle.CUSTOM_ANNOT_TYPE_AREA_MEASURE:
                return context.getResources().getString(R.string.annot_area_measure).toLowerCase();
            case Annot.e_Popup:
            case Annot.e_Movie:
            case Annot.e_Widget:
            case Annot.e_Screen:
            case Annot.e_PrinterMark:
            case Annot.e_TrapNet:
            case Annot.e_Watermark:
            case Annot.e_3D:
            case Annot.e_Projection:
            case Annot.e_RichMedia:
            default:
                return context.getResources().getString(R.string.annot_misc).toLowerCase();
        }
    }

    /**
     * Returns annotation type as string.
     *
     * @param context The context
     * @param annot   The annotation
     * @return The annotation type as string
     */
    static public String getAnnotTypeAsString(
            @NonNull Context context,
            @NonNull Annot annot)
            throws PDFNetException {

        int typeId = getAnnotType(annot);
        return getAnnotTypeAsString(context, typeId);
    }

    public static int getAnnotType(@NonNull Annot annot) throws PDFNetException {
        int typeId = annot.getType();
        switch (typeId) {
            case Annot.e_Line:
                if (isArrow(annot)) {
                    return AnnotStyle.CUSTOM_ANNOT_TYPE_ARROW;
                } else if (isRuler(annot)) {
                    return AnnotStyle.CUSTOM_ANNOT_TYPE_RULER;
                }
            case Annot.e_Polyline:
                if (isPerimeterMeasure(annot)) {
                    return AnnotStyle.CUSTOM_ANNOT_TYPE_PERIMETER_MEASURE;
                }
            case Annot.e_Polygon:
                if (isCloud(annot)) {
                    return AnnotStyle.CUSTOM_ANNOT_TYPE_CLOUD;
                } else if (isAreaMeasure(annot)) {
                    return AnnotStyle.CUSTOM_ANNOT_TYPE_AREA_MEASURE;
                }
            case Annot.e_Ink:
                if (isFreeHighlighter(annot)) {
                    return AnnotStyle.CUSTOM_ANNOT_TYPE_FREE_HIGHLIGHTER;
                }
            case Annot.e_FreeText:
                if (isCallout(annot)) {
                    return AnnotStyle.CUSTOM_ANNOT_TYPE_CALLOUT;
                }
        }
        return annot.getType();
    }

    /**
     * Gets the annot style
     * A read lock is required for this function.
     *
     * @param annot the annotation
     * @return the annot style
     * @throws PDFNetException PDFNet exception
     */
    public static AnnotStyle getAnnotStyle(@NonNull Annot annot) throws PDFNetException {
        int annotType = annot.getType();
        boolean annotIsSticky = (annotType == Annot.e_Text);
        boolean annotIsFreeText = (annotType == Annot.e_FreeText ||
                annotType == AnnotStyle.CUSTOM_ANNOT_TYPE_CALLOUT);
        // thickness
        float thickness = (float) annot.getBorderStyle().getWidth();

        if (thickness == 0) {
            Obj sdfObj = annot.getSDFObj().findObj(Tool.PDFTRON_THICKNESS);
            if (sdfObj != null) {
                thickness = (float) sdfObj.getNumber();
            }
        }

        // stroke color
        ColorPt colorPt;
        int compNum;

        if (annotIsFreeText) {
            FreeText freeText = new FreeText(annot);
            colorPt = freeText.getLineColor();
            compNum = freeText.getLineColorCompNum();
        } else {
            colorPt = annot.getColorAsRGB();
            compNum = annot.getColorCompNum();
        }
        int color = Utils.colorPt2color(colorPt);
        if (compNum == 0) {
            color = Color.TRANSPARENT;
        }

        // get fill color
        int fillColor = Color.TRANSPARENT;
        float opacity = 1.0f;
        if (annot.isMarkup()) {
            Markup m = new Markup(annot);
            opacity = (float) m.getOpacity();

            if (annotIsFreeText) {
                FreeText freeText = new FreeText(annot);
                if (freeText.getColorCompNum() == 3) {
                    ColorPt fillColorPt = freeText.getColorAsRGB();
                    fillColor = Utils.colorPt2color(fillColorPt);
                }
            } else {
                if (m.getInteriorColorCompNum() == 3) {
                    ColorPt fillColorPt = m.getInteriorColor();
                    fillColor = Utils.colorPt2color(fillColorPt);
                }
            }
        }

        // get icon
        String icon = "";
        if (annotIsSticky) {
            Text t = new Text(annot);
            icon = t.getIconName();
        }

        if (annotType == Annot.e_Sound) {
            icon = SoundCreate.SOUND_ICON;
        }

        AnnotStyle annotStyle = new AnnotStyle();
        annotStyle.setAnnotType(annot.getType());
        annotStyle.setStyle(color, fillColor, thickness, opacity);

        if (!Utils.isNullOrEmpty(icon)) {
            annotStyle.setIcon(icon);
        }

        if (annot.getType() == Annot.e_Circle ||
                annot.getType() == Annot.e_Square ||
                annot.getType() == Annot.e_Line ||
                annot.getType() == Annot.e_Polyline ||
                annot.getType() == Annot.e_Polygon ||
                annot.getType() == Annot.e_Stamp) {
            // these are annotation types we can draw
            // with exceptions that we will go through below
            annotStyle.setHasAppearance(false);
        }

        // text style
        if (annotIsFreeText) {
            String fontName = "";
            @ColorInt int textColor;
            float textSize;
            FreeText freeText = new FreeText(annot);
            textSize = (float) freeText.getFontSize();
            textColor = Utils.colorPt2color(freeText.getTextColor());
            Obj freeTextObj = freeText.getSDFObj();
            Obj drDict = freeTextObj.findObj("DR");
            if (drDict != null && drDict.isDict()) {
                Obj fontDict = drDict.findObj("Font");
                if (fontDict != null && fontDict.isDict()) {
                    DictIterator fItr = fontDict.getDictIterator();
                    if (fItr.hasNext()) {
                        Font f = new Font(fItr.value());
                        fontName = f.getName();
                    }
                }
            }

            annotStyle.setFont(new FontResource(fontName));
            annotStyle.setTextColor(textColor);
            if (textSize == 0) {
                textSize = 12;
            }
            annotStyle.setTextSize(textSize);
        }

        if (annotType == Annot.e_Line) {
            if (!isSimpleLine(annot)) {
                annotStyle.setHasAppearance(true);
            }
            if (AnnotUtils.isArrow(annot)) {
                annotType = AnnotStyle.CUSTOM_ANNOT_TYPE_ARROW;
                annotStyle.setHasAppearance(false);
            } else if (AnnotUtils.isRuler(annot)) {
                annotType = AnnotStyle.CUSTOM_ANNOT_TYPE_RULER;
                RulerItem rulerItem = MeasureUtils.getRulerItemFromAnnot(annot);
                if (null != rulerItem) {
                    annotStyle.setRulerItem(rulerItem);
                } else {
                    rulerItem = RulerItem.getRulerItem(annot); // legacy
                    if (null != rulerItem) {
                        annotStyle.setRulerItem(rulerItem);
                    }
                }
                annotStyle.setHasAppearance(false);
            }
            annotStyle.setAnnotType(annotType);
        }
        if (annotType == Annot.e_Ink) {
            if (AnnotUtils.isFreeHighlighter(annot)) {
                annotType = AnnotStyle.CUSTOM_ANNOT_TYPE_FREE_HIGHLIGHTER;
                annotStyle.setAnnotType(annotType);
                annotStyle.setHasAppearance(true);
            }
        }
        if (annotType == Annot.e_Polyline) {
            if (AnnotUtils.isPerimeterMeasure(annot)) {
                annotType = AnnotStyle.CUSTOM_ANNOT_TYPE_PERIMETER_MEASURE;
                annotStyle.setAnnotType(annotType);
                RulerItem rulerItem = MeasureUtils.getRulerItemFromAnnot(annot);
                if (null != rulerItem) {
                    annotStyle.setRulerItem(rulerItem);
                }
            }
        }
        if (annotType == Annot.e_Polygon) {
            if (AnnotUtils.isCloud(annot)) {
                annotType = AnnotStyle.CUSTOM_ANNOT_TYPE_CLOUD;
                annotStyle.setAnnotType(annotType);
                Polygon polygon = new Polygon(annot);
                double intensity = polygon.getBorderEffectIntensity();
                annotStyle.setBorderEffectIntensity(intensity);
            } else if (AnnotUtils.isAreaMeasure(annot)) {
                annotType = AnnotStyle.CUSTOM_ANNOT_TYPE_AREA_MEASURE;
                annotStyle.setAnnotType(annotType);
                RulerItem rulerItem = MeasureUtils.getRulerItemFromAnnot(annot);
                if (null != rulerItem) {
                    annotStyle.setRulerItem(rulerItem);
                }
            }
        }
        if (annotType == Annot.e_FreeText) {
            if (AnnotUtils.isCallout(annot)) {
                annotType = AnnotStyle.CUSTOM_ANNOT_TYPE_CALLOUT;
                annotStyle.setAnnotType(annotType);
                annotStyle.setHasAppearance(true);
            }
        }
        if (annotType == Annot.e_Redact) {
            Redaction redaction = new Redaction(annot);
            annotStyle.setOverlayText(redaction.getOverlayText());
        }

        try {
            // check if border style is not solid, then client side cannot draw properly
            Annot.BorderStyle borderStyle = annot.getBorderStyle();
            if (borderStyle.getStyle() != Annot.BorderStyle.e_solid) {
                annotStyle.setHasAppearance(true);
            }
        } catch (Exception ignored) {
        }

        annotStyle.setTextContent(annot.getContents());

        return annotStyle;
    }

    /**
     * Returns annotation type as plural string.
     *
     * @param context The context
     * @param typeId  The annotation type ID
     * @return The annotation type as string
     */
    private static String getAnnotTypeAsPluralString(
            @NonNull Context context,
            int typeId) {

        switch (typeId) {
            case Annot.e_Text:
                return context.getResources().getString(R.string.annot_text_plural).toLowerCase();
            case Annot.e_Link:
                return context.getResources().getString(R.string.annot_link_plural).toLowerCase();
            case Annot.e_FreeText:
                return context.getResources().getString(R.string.annot_free_text_plural).toLowerCase();
            case AnnotStyle.CUSTOM_ANNOT_TYPE_CALLOUT:
                return context.getResources().getString(R.string.annot_callout_plural).toLowerCase();
            case Annot.e_Line:
                return context.getResources().getString(R.string.annot_line_plural).toLowerCase();
            case Annot.e_Square:
                return context.getResources().getString(R.string.annot_square_plural).toLowerCase();
            case Annot.e_Circle:
                return context.getResources().getString(R.string.annot_circle_plural).toLowerCase();
            case Annot.e_Polygon:
                return context.getResources().getString(R.string.annot_polygon_plural).toLowerCase();
            case Annot.e_Polyline:
                return context.getResources().getString(R.string.annot_polyline).toLowerCase();
            case Annot.e_Highlight:
                return context.getResources().getString(R.string.annot_highlight_plural).toLowerCase();
            case Annot.e_Underline:
                return context.getResources().getString(R.string.annot_underline_plural).toLowerCase();
            case Annot.e_Squiggly:
                return context.getResources().getString(R.string.annot_squiggly_plural).toLowerCase();
            case Annot.e_StrikeOut:
                return context.getResources().getString(R.string.annot_strikeout_plural).toLowerCase();
            case Annot.e_Stamp:
                return context.getResources().getString(R.string.annot_stamp_plural).toLowerCase();
            case Annot.e_Caret:
                return context.getResources().getString(R.string.annot_caret_plural).toLowerCase();
            case Annot.e_Ink:
                return context.getResources().getString(R.string.annot_ink_plural).toLowerCase();
            case Annot.e_Redact:
                return context.getResources().getString(R.string.annot_redaction_plural).toLowerCase();
            case AnnotStyle.CUSTOM_ANNOT_TYPE_SIGNATURE:
                return context.getResources().getString(R.string.annot_signature_plural).toLowerCase();
            case AnnotStyle.CUSTOM_ANNOT_TYPE_ARROW:
                return context.getResources().getString(R.string.annot_arrow_plural).toLowerCase();
            case AnnotStyle.CUSTOM_ANNOT_TYPE_RULER:
                return context.getResources().getString(R.string.annot_ruler_plural).toLowerCase();
            case AnnotStyle.CUSTOM_ANNOT_TYPE_FREE_HIGHLIGHTER:
                return context.getResources().getString(R.string.annot_free_highlight_plural).toLowerCase();
            case AnnotStyle.CUSTOM_ANNOT_TYPE_CLOUD:
                return context.getResources().getString(R.string.annot_cloud_plural).toLowerCase();
            case Annot.e_FileAttachment:
                return context.getResources().getString(R.string.annot_file_attachment_plural).toLowerCase();
            case Annot.e_Sound:
                return context.getResources().getString(R.string.annot_sound).toLowerCase();
            case AnnotStyle.CUSTOM_ANNOT_TYPE_PERIMETER_MEASURE:
                return context.getResources().getString(R.string.annot_perimeter_measure).toLowerCase();
            case AnnotStyle.CUSTOM_ANNOT_TYPE_AREA_MEASURE:
                return context.getResources().getString(R.string.annot_area_measure).toLowerCase();
            case Annot.e_Popup:
            case Annot.e_Movie:
            case Annot.e_Widget:
            case Annot.e_Screen:
            case Annot.e_PrinterMark:
            case Annot.e_TrapNet:
            case Annot.e_Watermark:
            case Annot.e_3D:
            case Annot.e_Projection:
            case Annot.e_RichMedia:
            default:
                return context.getResources().getString(R.string.annot_misc_plural).toLowerCase();
        }
    }

    /**
     * Returns annotation type as string.
     *
     * @param context The context
     * @param annot   The annotation
     * @return The annotation type as string
     */
    static public String getAnnotTypeAsPluralString(
            @NonNull Context context,
            @NonNull Annot annot)
            throws PDFNetException {

        int typeId = getAnnotType(annot);
        return getAnnotTypeAsPluralString(context, typeId);
    }

    /**
     * Returns annotation type as raw string (not localized).
     *
     * @param annot The annotation
     * @return The annotation type as string
     */
    static public String getAnnotTypeAsString(
            Annot annot)
            throws PDFNetException {

        int annotType = annot.getType();
        switch (annotType) {
            case Annot.e_Text:
                return "sticky_note";
            case Annot.e_Link:
                return "link";
            case Annot.e_FreeText:
                if (isCallout(annot)) {
                    return "callout";
                }
                return "free_text";
            case Annot.e_Line:
                if (isArrow(annot)) {
                    return "arrow";
                }
                if (isRuler(annot)) {
                    return "ruler";
                }
                return "line";
            case Annot.e_Square:
                return "square";
            case Annot.e_Circle:
                return "circle";
            case Annot.e_Polygon:
                if (isCloud(annot)) {
                    return "cloud";
                }
                return "polygon";
            case Annot.e_Polyline:
                return "polyline";
            case Annot.e_Highlight:
                return "highlight";
            case Annot.e_Underline:
                return "underline";
            case Annot.e_Squiggly:
                return "squiggly";
            case Annot.e_StrikeOut:
                return "strikeout";
            case Annot.e_Stamp:
                return "stamp";
            case Annot.e_Caret:
                return "caret";
            case Annot.e_Ink:
                if (isFreeHighlighter(annot)) {
                    return "free_highlighter";
                }
                return "ink";
            default:
                return "annotation";
        }
    }

    /**
     * Returns annotation type as raw plural string (not localized).
     *
     * @param annot The annotation
     * @return The annotation type as string
     */
    static public String getAnnotTypeAsPluralString(
            Annot annot)
            throws PDFNetException {

        int annotType = annot.getType();
        switch (annotType) {
            case Annot.e_Text:
                return "sticky_notes";
            case Annot.e_Line:
                if (isArrow(annot)) {
                    return "arrows";
                }
                if (isRuler(annot)) {
                    return "rulers";
                }
                return "lines";
            case Annot.e_FreeText:
                if (isCallout(annot)) {
                    return "callouts";
                }
                return "free_texts";
            case Annot.e_Link:
                return "links";
            case Annot.e_Square:
                return "squares";
            case Annot.e_Circle:
                return "circles";
            case Annot.e_Polygon:
                if (isCloud(annot)) {
                    return "clouds";
                }
                return "polygons";
            case Annot.e_Polyline:
                return "polylines";
            case Annot.e_Highlight:
                return "highlights";
            case Annot.e_Underline:
                return "underlines";
            case Annot.e_Squiggly:
                return "squiggles";
            case Annot.e_StrikeOut:
                return "strikeouts";
            case Annot.e_Stamp:
                return "stamps";
            case Annot.e_Caret:
                return "carets";
            case Annot.e_Ink:
                if (isFreeHighlighter(annot)) {
                    return "free_highlighters";
                }
                return "inks";
            default:
                return "annotations";
        }
    }

    /**
     * Checks if the annotation is a line with no ending styles
     *
     * @param annot The annotation
     * @return True if the annotation is arrow
     * @throws PDFNetException PDFNet exception
     */
    public static boolean isSimpleLine(
            @NonNull Annot annot)
            throws PDFNetException {

        Line line = new Line(annot);
        return line.isValid() && line.getEndStyle() == Line.e_None && line.getStartStyle() == Line.e_None;
    }

    /**
     * Checks if the annotation is an arrow
     *
     * @param annot The annotation
     * @return True if the annotation is arrow
     * @throws PDFNetException PDFNet exception
     */
    public static boolean isArrow(
            @NonNull Annot annot)
            throws PDFNetException {

        Line line = new Line(annot);
        return line.isValid() && line.getEndStyle() == Line.e_OpenArrow;
    }

    /**
     * Checks if the annotation is an ruler
     *
     * @param annot The annotation
     * @return True if the annotation is ruler
     */
    public static boolean isRuler(
            @NonNull Annot annot) {

        try {
            String itField = MeasureUtils.getIT(annot);
            return itField != null && itField.equals(MeasureUtils.K_LineDimension);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return RulerItem.getRulerItem(annot) != null; // legacy
    }

    /**
     * Checks if the annotation is perimeter measurement
     *
     * @param annot The annotation
     * @return True if the annotation is perimeter measurement
     */
    public static boolean isPerimeterMeasure(
            @NonNull Annot annot) {

        try {
            String itField = MeasureUtils.getIT(annot);
            return itField != null && itField.equals(MeasureUtils.K_PolyLineDimension);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    /**
     * Checks if the annotation is area measurement
     *
     * @param annot The annotation
     * @return True if the annotation is area measurement
     */
    public static boolean isAreaMeasure(
            @NonNull Annot annot) {

        try {
            String itField = MeasureUtils.getIT(annot);
            return itField != null && itField.equals(MeasureUtils.K_PolygonDimension);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    /**
     * Checks if the annotation is a cloud polygon
     *
     * @param annot The annotation
     * @return True if the annotation is cloud
     * @throws PDFNetException PDFNet exception
     */
    public static boolean isCloud(
            @NonNull Annot annot)
            throws PDFNetException {

        Polygon polygon = new Polygon(annot);
        return polygon.isValid() && polygon.getBorderEffect() == Markup.e_Cloudy;
    }

    /**
     * Checks if the annotation is a free highlighter
     *
     * @param annot The annotation
     * @return True if the annotation is free highlighter
     * @throws PDFNetException PDFNet exception
     */
    public static boolean isFreeHighlighter(
            @NonNull Annot annot)
            throws PDFNetException {

        Ink ink = new Ink(annot);
        return ink.isValid() && ink.getBlendMode() == Ink.BlendMode.MULTIPLY.getValue();
    }

    public static boolean isCallout(@NonNull Annot annot)
            throws PDFNetException {
        FreeText freeText = new FreeText(annot);
        return freeText.isValid() &&
                freeText.getIntentName() == FreeText.e_FreeTextCallout;
    }

    /**
     * Returns the annotation image resource ID.
     *
     * @param type The type of annotation
     * @return The annotation image resource ID
     */
    public static int getAnnotImageResId(int type) {
        int resId = android.R.id.empty;

        switch (type) {
            case Annot.e_Text:
                resId = R.drawable.ic_annotation_sticky_note_black_24dp;
                break;
            case Annot.e_Line:
                resId = R.drawable.ic_annotation_line_black_24dp;
                break;
            case AnnotStyle.CUSTOM_ANNOT_TYPE_ARROW:
                resId = R.drawable.ic_annotation_arrow_black_24dp;
                break;
            case AnnotStyle.CUSTOM_ANNOT_TYPE_RULER:
                resId = R.drawable.ic_annotation_distance_black_24dp;
                break;
            case Annot.e_Polyline:
                resId = R.drawable.ic_annotation_polyline_black_24dp;
                break;
            case AnnotStyle.CUSTOM_ANNOT_TYPE_PERIMETER_MEASURE:
                resId = R.drawable.ic_annotation_perimeter_black_24dp;
                break;
            case Annot.e_Square:
                resId = R.drawable.ic_annotation_square_black_24dp;
                break;
            case Annot.e_Circle:
                resId = R.drawable.ic_annotation_circle_black_24dp;
                break;
            case Annot.e_Polygon:
                resId = R.drawable.ic_annotation_polygon_black_24dp;
                break;
            case AnnotStyle.CUSTOM_ANNOT_TYPE_AREA_MEASURE:
                resId = R.drawable.ic_annotation_area_black_24dp;
                break;
            case AnnotStyle.CUSTOM_ANNOT_TYPE_CLOUD:
                resId = R.drawable.ic_annotation_cloud_black_24dp;
                break;
            case Annot.e_Underline:
                resId = R.drawable.ic_annotation_underline_black_24dp;
                break;
            case Annot.e_StrikeOut:
                resId = R.drawable.ic_annotation_strikeout_black_24dp;
                break;
            case Annot.e_Ink:
                resId = R.drawable.ic_annotation_freehand_black_24dp;
                break;
            case Annot.e_Highlight:
                resId = R.drawable.ic_annotation_highlight_black_24dp;
                break;
            case Annot.e_FreeText:
                resId = R.drawable.ic_annotation_freetext_black_24dp;
                break;
            case AnnotStyle.CUSTOM_ANNOT_TYPE_CALLOUT:
                resId = R.drawable.ic_annotation_callout_black_24dp;
                break;
            case Annot.e_Squiggly:
                resId = R.drawable.ic_annotation_squiggly_black_24dp;
                break;
            case Annot.e_Stamp:
                resId = R.drawable.annotation_rubber_stamp;
                break;
            case Annot.e_Caret:
                resId = R.drawable.ic_annotation_caret_black_24dp;
                break;
            case Annot.e_Redact:
                resId = R.drawable.ic_annotation_redact_black_24dp;
                break;
            case AnnotStyle.CUSTOM_ANNOT_TYPE_SIGNATURE:
                resId = R.drawable.ic_annotation_signature_black_24dp;
                break;
            case AnnotStyle.CUSTOM_ANNOT_TYPE_ERASER:
                resId = R.drawable.ic_annotation_eraser_black_24dp;
                break;
            case AnnotStyle.CUSTOM_ANNOT_TYPE_FREE_HIGHLIGHTER:
                resId = R.drawable.ic_annotation_free_highlight_black_24dp;
                break;
            case Annot.e_Sound:
                resId = R.drawable.ic_mic_black_24dp;
                break;
            case Annot.e_FileAttachment:
                resId = R.drawable.ic_attach_file_black_24dp;
                break;
            default:
                break;
        }

        return resId;
    }

    /**
     * Returns the annotation color of the specified annotation.
     *
     * @param annot The annotation
     * @return The annotation color
     */
    public static int getAnnotColor(Annot annot) {
        int color;
        try {
            int type = annot.getType();
            ColorPt colorPt = annot.getColorAsRGB();
            color = Utils.colorPt2color(colorPt);
            if (type == Annot.e_FreeText) {
                FreeText freeText = new FreeText(annot);
                if (freeText.getTextColorCompNum() == 3) {
                    ColorPt fillColorPt = freeText.getTextColor();
                    color = Utils.colorPt2color(fillColorPt);
                }
            }
            if (annot.isMarkup()) {
                // if has fill color, use fill color
                Markup m = new Markup(annot);
                if (m.getInteriorColorCompNum() == 3) {
                    ColorPt fillColorPt = m.getInteriorColor();
                    int fillColor = Utils.colorPt2color(fillColorPt);
                    if (fillColor != Color.TRANSPARENT) {
                        color = fillColor;
                    }
                }
            }
        } catch (Exception e) {
            AnalyticsHandlerAdapter.getInstance().sendException(e);
            color = Color.BLACK;
        }
        return color;
    }

    /**
     * Returns the annotation opacity of the specified annotation.
     *
     * @param annot The annotation
     * @return The annotation opacity
     */
    public static float getAnnotOpacity(Annot annot) {
        float opacity = 1.0f;
        try {
            if (annot.isMarkup()) {
                Markup m = new Markup(annot);
                opacity = (float) m.getOpacity();
            }
        } catch (Exception e) {
            AnalyticsHandlerAdapter.getInstance().sendException(e);
            opacity = 1.0f;
        }
        return opacity;
    }

    /**
     * Returns the bounding box of the specified annotation in screen space.
     *
     * @param pdfViewCtrl The PDFViewCtrl
     * @param annot       The annotation
     * @param pg          The page number
     * @return The bounding box
     */
    public static com.pdftron.pdf.Rect computeAnnotInbox(PDFViewCtrl pdfViewCtrl, Annot annot, int pg) {
        try {
            com.pdftron.pdf.Rect r = annot.getRect();
            com.pdftron.pdf.Rect ur = new com.pdftron.pdf.Rect();
            r.normalize();
            double[] pts;
            pts = pdfViewCtrl.convPagePtToScreenPt(r.getX1(), r.getY2(), pg);
            ur.setX1(pts[0]);
            ur.setY1(pts[1]);
            pts = pdfViewCtrl.convPagePtToScreenPt(r.getX2(), r.getY1(), pg);
            ur.setX2(pts[0]);
            ur.setY2(pts[1]);
            return ur;
        } catch (PDFNetException e) {
            AnalyticsHandlerAdapter.getInstance().sendException(e);
        }
        return null;
    }

    public static com.pdftron.pdf.Rect quadToRect(QuadPoint qp) throws PDFNetException {
        float x1 = (float) Math.min(Math.min(Math.min(qp.p1.x, qp.p2.x), qp.p3.x), qp.p4.x);
        float y1 = (float) Math.min(Math.min(Math.min(qp.p1.y, qp.p2.y), qp.p3.y), qp.p4.y);
        float x2 = (float) Math.max(Math.max(Math.max(qp.p1.x, qp.p2.x), qp.p3.x), qp.p4.x);
        float y2 = (float) Math.max(Math.max(Math.max(qp.p1.y, qp.p2.y), qp.p3.y), qp.p4.y);
        Rect quadRect = new Rect(x1, y1, x2, y2);
        quadRect.normalize();
        return quadRect;
    }

    public static android.graphics.RectF getScreenRectFromPageRect(PDFViewCtrl pdfViewCtrl, Rect pageRect, int pg) throws PDFNetException {
        if (null == pdfViewCtrl || null == pageRect) {
            return null;
        }
        android.graphics.RectF rectF = new RectF();
        pageRect.normalize();
        double[] pts;
        pts = pdfViewCtrl.convPagePtToScreenPt(pageRect.getX1(), pageRect.getY2(), pg);
        rectF.left = (float) pts[0];
        rectF.top = (float) pts[1];
        pts = pdfViewCtrl.convPagePtToScreenPt(pageRect.getX2(), pageRect.getY1(), pg);
        rectF.right = (float) pts[0];
        rectF.bottom = (float) pts[1];
        return rectF;
    }

    private static JSONObject createFreeTextJson(FreeTextCacheStruct freeTextCacheStruct) {
        JSONObject object = new JSONObject();
        try {
            object.put(FreeTextCacheStruct.CONTENTS, freeTextCacheStruct.contents);
            object.put(FreeTextCacheStruct.PAGE_NUM, freeTextCacheStruct.pageNum);
            JSONObject targetPoint = new JSONObject();
            targetPoint.put(FreeTextCacheStruct.X, freeTextCacheStruct.x);
            targetPoint.put(FreeTextCacheStruct.Y, freeTextCacheStruct.y);
            object.put(FreeTextCacheStruct.TARGET_POINT, targetPoint);
        } catch (Exception e) {
            e.printStackTrace();
            object = new JSONObject();
        }
        return object;
    }

    /**
     * Saves the free text in cache.
     *
     * @param freeTextCacheStruct The FreeTextCacheStruct
     * @param pdfViewCtrl         The PDFViewCtrl
     */
    public static void saveFreeTextCache(FreeTextCacheStruct freeTextCacheStruct, PDFViewCtrl pdfViewCtrl) {
        if (null == freeTextCacheStruct || null == pdfViewCtrl) {
            return;
        }
        if (pdfViewCtrl.getToolManager() == null) {
            return;
        }
        if (Utils.isNullOrEmpty(freeTextCacheStruct.contents)) {
            return;
        }
        String cacheFileName = ((ToolManager) pdfViewCtrl.getToolManager()).getFreeTextCacheFileName();

        JSONObject obj = createFreeTextJson(freeTextCacheStruct);
        ObjectOutput out = null;
        try {
            if (!cacheFileName.trim().isEmpty()) {
                out = new ObjectOutputStream(new FileOutputStream(new File(pdfViewCtrl.getContext().getCacheDir(), "") + cacheFileName));
                out.writeObject(obj.toString());
            }
        } catch (Exception e) {
            AnalyticsHandlerAdapter.getInstance().sendException(e);
            e.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (Exception ignored) {

                }
            }
        }
    }

    /**
     * Delete all annotations except for links and form fields on pages
     * This method does not lock document, a write lock should be acquired outside
     *
     * @param doc the PDFDoc
     */
    @SuppressWarnings("unused")
    public static void safeDeleteAnnotsOnPage(PDFDoc doc, ArrayList<Integer> pages) {
        // delete all annotations except for links and form fields
        if (null == doc || null == pages) {
            return;
        }
        try {
            for (int pageNum : pages) {
                if (pageNum > -1) {
                    pageNum = pageNum + 1; // webviewer is 0-indexed
                    Page page = doc.getPage(pageNum);
                    if (page.isValid()) {
                        int annotationCount = page.getNumAnnots();
                        for (int a = annotationCount - 1; a >= 0; a--) {
                            try {
                                Annot annotation = page.getAnnot(a);
                                if (annotation == null || !annotation.isValid()) {
                                    continue;
                                }
                                if (annotation.getType() != Annot.e_Link &&
                                        annotation.getType() != Annot.e_Widget) {
                                    page.annotRemove(annotation);
                                }
                            } catch (PDFNetException e) {
                                // this annotation has some problem, let's skip it and continue with others
                            }
                        }
                    }
                }
            }
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }
    }

    /**
     * Delete all annotations except for links and form fields
     * This method does not lock document, a write lock should be acquired outside
     *
     * @param doc the PDFDoc
     */
    public static void safeDeleteAllAnnots(PDFDoc doc) {
        // delete all annotations except for links and form fields
        if (null == doc) {
            return;
        }
        try {
            PageIterator pageIterator = doc.getPageIterator();
            while (pageIterator.hasNext()) {
                Page page = pageIterator.next();
                if (page.isValid()) {
                    int annotationCount = page.getNumAnnots();
                    for (int a = annotationCount - 1; a >= 0; a--) {
                        try {
                            Annot annotation = page.getAnnot(a);
                            if (annotation == null || !annotation.isValid()) {
                                continue;
                            }
                            if (annotation.getType() != Annot.e_Link &&
                                    annotation.getType() != Annot.e_Widget) {
                                page.annotRemove(annotation);
                            }
                        } catch (PDFNetException e) {
                            // this annotation has some problem, let's skip it and continue with others
                        }
                    }
                }
            }
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }
    }

    /**
     * Sets the author for the specified annotation, if possible.
     * <p>
     * <b>This method does not lock the document, so a write lock should be acquired outside.</b>
     *
     * @param annot  the Annot
     * @param author the desired author name
     */
    public static void setAuthor(Annot annot, String author) {
        try {
            if (annot != null && annot.isMarkup()) {
                Markup markup = new Markup(annot);
                setAuthor(markup, author);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets the annotation date in local date time
     *
     * @param annot the annotation
     * @return the date
     */
    public static Date getAnnotLocalDate(@NonNull Annot annot) throws PDFNetException {
        com.pdftron.pdf.Date date = annot.getDate();
        Calendar calendar = Calendar.getInstance();
        int month = date.getMonth() - 1; // android calendar month is 0-indexed
        calendar.set(date.getYear(), month, date.getDay(), date.getHour(), date.getMinute(), date.getSecond());
        int offset = TimeZone.getDefault().getRawOffset() + TimeZone.getDefault().getDSTSavings();
        long localTime = calendar.getTimeInMillis() + offset;
        return new Date(localTime);
    }

    /**
     * Sets the author for the specified markup annotation.
     * <p>
     * <b>This method does not lock the document, so a write lock should be acquired outside.</b>
     *
     * @param markup the Markup annotation
     * @param author the desired author name
     */
    public static void setAuthor(Markup markup, String author) {
        if (markup == null) {
            return;
        }
        try {
            markup.setTitle(author);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Sets the unique identifier for the specified annotation, if possible.
     * <p>
     * <b>This method does not lock the document, so a write lock should be acquired outside.</b>
     *
     * @param annot the Annot
     * @param id    the unique identifier
     */
    public static void setUniqueId(Annot annot, String id) {
        try {
            if (annot != null) {
                annot.setUniqueID(id);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets the vertices of a Polyline or a Polygon.
     * <b>This method does not lock the document, so a read lock should be acquired outside.</b>
     *
     * @param annot annotation with type of polyline or polygon
     * @return the vertices
     */
    @Nullable
    public static ArrayList<Point> getPolyVertices(Annot annot) {
        try {
            if (annot == null) {
                return null;
            }
            if (annot.getType() == Annot.e_Polyline ||
                    annot.getType() == Annot.e_Polygon) {
                PolyLine polyLine = new PolyLine(annot);
                int count = polyLine.getVertexCount();

                ArrayList<Point> points = new ArrayList<>();
                for (int i = 0; i < count; ++i) {
                    Point pagePoint = polyLine.getVertex(i);
                    points.add(pagePoint);
                }
                return points;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * @hide
     */
    public static boolean isMadeByPDFTron(Annot annot) throws PDFNetException {
        String[] supportedTag = new String[]{Tool.PDFTRON_ID, "pdftronlink"};
        if (annot != null && annot.getSDFObj() != null) {
            Obj sdfObj = annot.getSDFObj();
            for (String tag : supportedTag) {
                Object selfMadeObj = sdfObj.findObj(tag);
                if (selfMadeObj != null) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if the annotation is an annotation reply
     *
     * @param annot the annotation
     * @return true if it is an annotation reply, false otherwise
     */
    public static boolean hasInReplyTo(Annot annot) throws PDFNetException {
        if (annot == null || annot.getType() != Annot.e_Text) {
            return false;
        }
        Obj annotSDFObj = annot.getSDFObj();
        if (annotSDFObj != null) {
            Obj irt = annotSDFObj.findObj(KEY_IRT);
            return irt != null;
        }
        return false;
    }

    public static Annot createAnnotationReply(@NonNull String parentAnnotId, int pageNum,
            @NonNull PDFViewCtrl pdfViewCtrl, @NonNull String authorId,
            @NonNull String contents) throws PDFNetException {
        Annot parent = ViewerUtils.getAnnotById(pdfViewCtrl, parentAnnotId, pageNum);
        if (parent == null) {
            return null;
        }
        boolean shouldUnlock = false;
        try {
            if (!parent.isValid()) {
                return null;
            }
            pdfViewCtrl.docLock(true);
            shouldUnlock = true;
            Rect rect = parent.getRect();
            rect.normalize();
            double left = rect.getX1();
            double top = rect.getY2();
            // use parent top left corner as reply annot rect
            Point p = new Point(left, top);
            Text reply = Text.create(pdfViewCtrl.getDoc(), p);
            setAuthor(reply, authorId);
            reply.getSDFObj().putString(KEY_IRT, parentAnnotId);
            Rect popupRect = new Rect();
            popupRect.set(left + 20, top + 20, left + 90, top + 90);
            Popup popup = Popup.create(pdfViewCtrl.getDoc(), popupRect);
            popup.setParent(reply);
            popup.setContents(contents);
            reply.setPopup(popup);

            Page page = pdfViewCtrl.getDoc().getPage(pageNum);
            page.annotPushBack(reply);
            page.annotPushBack(popup);

            return reply;
        } finally {
            if (shouldUnlock) {
                pdfViewCtrl.docUnlock();
            }
        }
    }

    public static Annot updateAnnotationReply(@NonNull String replyId, int pageNum,
            @NonNull PDFViewCtrl pdfViewCtrl,
            @Nullable ToolManager toolManager,
            @NonNull String contents) throws PDFNetException {
        Annot reply = ViewerUtils.getAnnotById(pdfViewCtrl, replyId, pageNum);
        if (reply == null || !reply.isMarkup()) {
            return null;
        }
        boolean shouldUnlock = false;
        try {
            pdfViewCtrl.docLock(true);
            shouldUnlock = true;
            Markup markup = new Markup(reply);
            HashMap<Annot, Integer> annots = new HashMap<>(1);
            annots.put(reply, pageNum);
            if (toolManager != null) {
                toolManager.raiseAnnotationsPreModifyEvent(annots);
            }
            // edit contents
            Utils.handleEmptyPopup(pdfViewCtrl, markup);
            Popup popup = markup.getPopup();
            popup.setContents(contents);
            setDateToNow(pdfViewCtrl, markup);

            if (toolManager != null) {
                toolManager.raiseAnnotationsModifiedEvent(annots, new Bundle());
            }
            return reply;
        } finally {
            if (shouldUnlock) {
                pdfViewCtrl.docUnlock();
            }
        }
    }

    public static void deleteAnnotationReply(@NonNull String replyId, int pageNum,
            @NonNull PDFViewCtrl pdfViewCtrl,
            @Nullable ToolManager toolManager) throws PDFNetException {
        Annot reply = ViewerUtils.getAnnotById(pdfViewCtrl, replyId, pageNum);
        if (reply == null) {
            return;
        }
        boolean shouldUnlock = false;
        try {
            pdfViewCtrl.docLock(true);
            shouldUnlock = true;
            HashMap<Annot, Integer> annots = new HashMap<>(1);
            annots.put(reply, pageNum);
            if (toolManager != null) {
                toolManager.raiseAnnotationsPreRemoveEvent(annots);
            }
            // remove from page
            Page page = pdfViewCtrl.getDoc().getPage(pageNum);
            page.annotRemove(reply);
            if (toolManager != null) {
                toolManager.raiseAnnotationsRemovedEvent(annots);
            }
        } finally {
            if (shouldUnlock) {
                pdfViewCtrl.docUnlock();
            }
        }
    }

    public static void setDateToNow(PDFViewCtrl pdfViewCtrl, Annot annot) {
        boolean shouldUnlock = false;
        try {
            pdfViewCtrl.docLock(true);
            shouldUnlock = true;
            annot.setDateToNow();
        } catch (Exception e) {
            AnalyticsHandlerAdapter.getInstance().sendException(e);
        } finally {
            if (shouldUnlock) {
                pdfViewCtrl.docUnlock();
            }
        }
    }

    /**
     * Gets the unique Id of the IRT parent annotation of this annotation
     */
    @Nullable
    public static String getIRTAsString(Annot annot) throws PDFNetException {
        if (annot == null) {
            return null;
        }
        Obj annotSDFObj = annot.getSDFObj();
        if (annotSDFObj != null) {
            Obj irt = annotSDFObj.findObj(KEY_IRT);
            if (irt != null) {
                // first check string, for backwards compatibility
                if (irt.isString()) {
                    return irt.getAsPDFText();
                }
                // IRT is dict link
                if (irt.isDict()) {
                    Obj nm = irt.findObj(KEY_NM);
                    if (nm != null && nm.isString()) {
                        return nm.getAsPDFText();
                    }
                }
            }
        }
        return null;
    }

    /**
     * Checks if the annotation is part of a group annotation
     *
     * @param annot the annotation
     * @return true if it is part of a group annotation, false otherwise
     */
    public static boolean hasReplyTypeGroup(Annot annot) throws PDFNetException {
        if (annot == null) {
            return false;
        }
        // group annotation
        // RT is only meaningful if IRT is present
        // valid values are "R" and "Group"
        String VALUE_Group = "Group";
        Obj annotSDFObj = annot.getSDFObj();
        if (annotSDFObj != null) {
            Obj irt = annotSDFObj.findObj(KEY_IRT);
            Obj rt = annotSDFObj.findObj(KEY_RT);
            if (irt != null && rt != null && rt.isName()) {
                String rtVal = rt.getName();
                return VALUE_Group.equals(rtVal);
            }
        }
        return false;
    }

    /**
     * Returns all annotations in this group if the annotation passed in is in an annotation group
     */
    @Nullable
    public static ArrayList<Annot> getAnnotationsInGroup(PDFViewCtrl pdfViewCtrl, Annot annot, int pageNum) throws PDFNetException {
        if (null == pdfViewCtrl || null == annot) {
            return null;
        }
        if (!annot.isValid()) {
            return null;
        }
        boolean shouldUnlockRead = false;
        try {
            ArrayList<Annot> annotsInGroup = new ArrayList<>();
            pdfViewCtrl.docLockRead();
            shouldUnlockRead = true;

            String mainAnnotId = null;
            if (hasReplyTypeGroup(annot)) {
                // this is a sub annot in a group
                // its IRT will be the primary annot in the group
                mainAnnotId = getIRTAsString(annot);
            } else if (annot.getUniqueID() != null) {
                // this is a primary annot
                mainAnnotId = annot.getUniqueID().getAsPDFText();
            }

            ArrayList<Annot> annotsInPage = pdfViewCtrl.getAnnotationsOnPage(pageNum);
            for (Annot ann : annotsInPage) {
                if (mainAnnotId != null && ann != null && ann.isValid() && ann.getUniqueID() != null) {
                    // find all annotations in the group
                    String id = ann.getUniqueID().getAsPDFText();
                    if (id != null && id.equals(mainAnnotId)) {
                        // add primary annot
                        annotsInGroup.add(ann);
                    }
                    String irt = getIRTAsString(ann);
                    if (hasReplyTypeGroup(ann) && irt != null && irt.equals(mainAnnotId)) {
                        // add subordinate annot
                        annotsInGroup.add(ann);
                    }
                }
            }
            return annotsInGroup;
        } finally {
            if (shouldUnlockRead) {
                pdfViewCtrl.docUnlockRead();
            }
        }
    }

    @Nullable
    public static Bitmap getStampAppearance(PDFViewCtrl pdfViewCtrl, Annot stamp) throws PDFNetException {
        if (pdfViewCtrl == null || stamp == null) {
            return null;
        }
        if (!stamp.isValid() || stamp.getType() != Annot.e_Stamp) {
            return null;
        }
        boolean shouldUnlockRead = false;
        PDFDoc doc = null;
        try {
            pdfViewCtrl.cancelRenderingAsync();
            pdfViewCtrl.docLockRead();
            shouldUnlockRead = true;
            PDFDraw draw = new PDFDraw();
            draw.setDPI(92.0);
            draw.setPageTransparent(true);
            draw.setAntiAliasing(true);

            Rect annotRect = stamp.getRect();

            // Create a new transparent page
            doc = new PDFDoc();
            Rect pageRect = new Rect(0, 0, annotRect.getX2() - annotRect.getX1(), annotRect.getY2() - annotRect.getY1());
            Page page = doc.pageCreate(pageRect);
            doc.pagePushBack(page);

            // copy the annotation
            Obj srcAnnotation = stamp.getSDFObj();
            Obj pEntry = srcAnnotation.findObj("P");

            Obj[] objList = new Obj[]{
                    srcAnnotation
            };
            Obj[] exclList = new Obj[]{
                    pEntry
            };
            Obj[] destAnnot = doc.getSDFDoc().importObjs(objList, exclList);
            if (destAnnot != null && destAnnot.length > 0) {
                Annot dest = new Annot(destAnnot[0]);
                dest.setRect(pageRect);
                page.annotPushBack(dest);

                return draw.getBitmap(page);
            }
        } catch (Exception ignored) {

        } finally {
            if (doc != null) {
                doc.close();
            }
            if (shouldUnlockRead) {
                pdfViewCtrl.docUnlockRead();
            }
        }
        return null;
    }

    /**
     * Sets the rotation degree from Stamp annotation.
     * A write lock is expected around this method.
     */
    public static void putStampDegree(Annot annot, int rotation) throws PDFNetException {
        Obj stampObj = annot.getSDFObj();
        stampObj.putNumber(Stamper.STAMPER_ROTATION_DEGREE_ID, rotation);
    }

    /**
     * Gets the rotation degree from Stamp annotation.
     * A read lock is expected around this method.
     */
    public static int getStampDegree(Annot annot) throws PDFNetException {
        Obj stampObj = annot.getSDFObj();
        Obj rotationObj = stampObj.findObj(Stamper.STAMPER_ROTATION_DEGREE_ID);
        int rotation = 0;
        if (rotationObj != null && rotationObj.isNumber()) {
            rotation = (int) rotationObj.getNumber();
            return rotation;
        }
        // try old stamp degree
        int oldDegree = getStampDegreeOld(annot);
        // old degree is clockwise, new degree is counter clockwise
        // let's convert here
        switch (oldDegree) {
            case 90:
                return 270;
            case 180:
                return 180;
            case 270:
                return 90;
        }
        return 0;
    }

    /**
     * Gets the deprecated rotation degree from Stamp annotation.
     * A read lock is expected around this method.
     */
    public static int getStampDegreeOld(Annot annot) throws PDFNetException {
        Obj stampObj = annot.getSDFObj();
        Obj rotationObj = stampObj.findObj(Stamper.STAMPER_ROTATION_ID);
        int rotation = 0;
        if (rotationObj != null && rotationObj.isNumber()) {
            rotation = (int) rotationObj.getNumber();
        }
        return rotation;
    }

    /**
     * Traverse over all pages in the specified document. The {@link PageVisitor visitor} will be
     * called for each page via {@link PageVisitor#visit(Page)}, allowing for arbitrary operations
     * to be performed on the page.
     * <p>
     * <b>This method does not lock the document, so a write lock should be acquired outside.</b>
     *
     * @param doc     the PDFDoc
     * @param visitor the {@link PageVisitor} to visit each page
     */
    @SuppressWarnings("WeakerAccess")
    public static void traversePages(PDFDoc doc, PageVisitor visitor) {
        if (doc == null || visitor == null) {
            return;
        }
        try {
            PageIterator iterator = doc.getPageIterator();
            while (iterator.hasNext()) {
                Page page = iterator.next();
                if (page != null && page.isValid()) {
                    visitor.visit(page);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Traverse over all annotations on the specified page. The {@link AnnotVisitor visitor} will be
     * called for each annot via {@link AnnotVisitor#visit(Annot)}, allowing for arbitrary operations
     * to be performed on the annot.
     * <p>
     * <b>This method does not lock the document, so a write lock should be acquired outside.</b>
     *
     * @param page    the Page
     * @param visitor the {@link AnnotVisitor} to visit each annot
     */
    @SuppressWarnings("WeakerAccess")
    public static void traverseAnnots(Page page, AnnotVisitor visitor) {
        if (page == null || visitor == null) {
            return;
        }
        try {
            final int numAnnots = page.getNumAnnots();
            for (int i = 0; i < numAnnots; i++) {
                Annot annot = page.getAnnot(i);
                if (annot != null && annot.isValid()) {
                    visitor.visit(annot);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Traverse over all annotations in the specified document. The {@link AnnotVisitor} will be
     * called for each annot via {@link AnnotVisitor#visit(Annot)}, allowing for arbitrary operations
     * to be performed on the annot.
     * <p>
     * <b>This method does not lock the document, so a write lock should be acquired outside.</b>
     *
     * @param doc     the PDFDoc
     * @param visitor the {@link AnnotVisitor} to visit each annot
     */
    public static void traverseAnnots(PDFDoc doc, final AnnotVisitor visitor) {
        if (doc == null || visitor == null) {
            return;
        }
        traversePages(doc, new PageVisitor() {
            @Override
            public void visit(@NonNull Page page) {
                traverseAnnots(page, visitor);
            }
        });
    }

    /**
     * Base interface for a visitor that will visit all members of a collection.
     *
     * @param <T> the collection member type
     */
    private interface Visitor<T> {
        void visit(@NonNull T node);
    }

    /**
     * Visitor that visits pages within a document.
     */
    public interface PageVisitor extends Visitor<Page> {
        /**
         * The overloaded implementation of {@link Visitor#visit(Object)}
         */
        @Override
        void visit(@NonNull Page page);
    }

    /**
     * Visitor that visits annotations on a single page or in an entire document.
     */
    public interface AnnotVisitor extends Visitor<Annot> {
        /**
         * The overloaded implementation of {@link Visitor#visit(Object)}
         */
        @Override
        void visit(@NonNull Annot annot);
    }
}
