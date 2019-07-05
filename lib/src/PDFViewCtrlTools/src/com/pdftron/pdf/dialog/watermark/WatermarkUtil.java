package com.pdftron.pdf.dialog.watermark;

import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.pdftron.pdf.ColorPt;
import com.pdftron.pdf.PDFDoc;
import com.pdftron.pdf.PDFViewCtrl;
import com.pdftron.pdf.PageSet;
import com.pdftron.pdf.Stamper;
import com.pdftron.pdf.utils.AnalyticsHandlerAdapter;
import com.pdftron.pdf.utils.Utils;

import java.util.Calendar;
import java.util.TimeZone;

/**
 * Class containing utility methods for handling watermark functionality.
 */
public class WatermarkUtil {

    /**
     * Applies a text watermark to all pages in the document.
     */
    public static void setTextWatermark(@NonNull PDFViewCtrl pdfViewCtrl, @Nullable String watermarkText,
                                        @ColorInt int textColor, float textOpacity, float textSize,
                                        boolean showTimestamp) {
        boolean shouldUnlock = false;
        try {
            pdfViewCtrl.docLock(true);
            shouldUnlock = true;
            PDFDoc doc = pdfViewCtrl.getDoc();
            PageSet ps = new PageSet(1, pdfViewCtrl.getPageCount(), PageSet.e_all);

            // Get stamper settings
            int rot = -45;
            ColorPt colorPt = Utils.color2ColorPt(textColor);

            // Create the stamper and stamp every page in this pdf
            Stamper watermark = new Stamper(Stamper.e_font_size, textSize, 0.05);
            watermark.setPosition(0, 0);
            watermark.setFontColor(colorPt);
            watermark.setOpacity(textOpacity);
            watermark.setRotation(rot);
            watermark.setTextAlignment(Stamper.e_align_center);
            watermark.stampText(doc, watermarkText, ps);

            // Show timestamp if specified
            if (showTimestamp) {
                Calendar cal = Calendar.getInstance();
                String date = cal.getTime().toString();
                String timeZone = TimeZone.getDefault().getDisplayName();
                String timeStampStr = String.format("%s \n (%s)", date, timeZone);
                Stamper timeStamp = new Stamper(Stamper.e_font_size, 24, 0.05);
                timeStamp.setPosition(0, 0);
                timeStamp.setFontColor(colorPt);
                timeStamp.setOpacity(textOpacity);
                timeStamp.setTextAlignment(Stamper.e_align_center);
                timeStamp.setAlignment(Stamper.e_horizontal_center, Stamper.e_vertical_top);
                timeStamp.stampText(doc, timeStampStr, ps);
            }

            pdfViewCtrl.update(true);
        } catch (Exception e) {
            AnalyticsHandlerAdapter.getInstance().sendException(e);
        } finally {
            if (shouldUnlock) {
                pdfViewCtrl.docUnlock();
            }
        }
    }

    /**
     * Clear all watermarks from a document
     */
    public static void clearWatermark(@NonNull PDFViewCtrl pdfViewCtrl) {
        boolean shouldUnlock = false;
        try {
            pdfViewCtrl.docLock(true);
            shouldUnlock = true;
            PDFDoc doc = pdfViewCtrl.getDoc();

            PageSet ps = new PageSet(1, pdfViewCtrl.getPageCount(), PageSet.e_all);
            Stamper.deleteStamps(doc, ps);
            pdfViewCtrl.update(true);

        } catch (Exception e) {
            AnalyticsHandlerAdapter.getInstance().sendException(e);
        } finally {
            if (shouldUnlock) {
                pdfViewCtrl.docUnlock();
            }
        }
    }
}
