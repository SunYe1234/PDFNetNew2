package com.pdftron.pdf.dialog.pdflayer;

import android.support.annotation.Nullable;

import com.pdftron.common.PDFNetException;
import com.pdftron.pdf.PDFDoc;
import com.pdftron.pdf.PDFViewCtrl;
import com.pdftron.pdf.ocg.Config;
import com.pdftron.pdf.ocg.Context;
import com.pdftron.pdf.ocg.Group;
import com.pdftron.pdf.utils.AnalyticsHandlerAdapter;
import com.pdftron.pdf.utils.Utils;
import com.pdftron.sdf.Obj;

import java.util.ArrayList;

/**
 * Utility for PDF layer.
 */
public class PdfLayerUtils {

    /**
     * Gets the ordered OCG layer.
     *
     * @param pdfViewCtrl the {@link PDFViewCtrl}
     * @param pdfDoc      the {@link PDFDoc}
     * @return An array list of ordered OCG layers.
     * @throws PDFNetException
     */
    public static ArrayList<LayerInfo> getLayers(@Nullable PDFViewCtrl pdfViewCtrl, @Nullable PDFDoc pdfDoc) throws PDFNetException {
        if (null == pdfDoc) {
            return new ArrayList<>();
        }
        ArrayList<LayerInfo> result = new ArrayList<>();
        Config cfg = pdfDoc.getOCGConfig();
        Obj ocgs = cfg.getOrder(); // Get the array of all OCGs in the document.
        if (ocgs != null) {
            int i, sz = (int) ocgs.size();
            for (i = 0; i < sz; ++i) {
                Group ocg = new Group(ocgs.getAt(i));
                result.add(new LayerInfo(ocg, getChecked(pdfViewCtrl, ocg)));
            }
        }
        return result;
    }

    /**
     * Gets the checked state of the OCG layer group.
     *
     * @param pdfViewCtrl the {@link PDFViewCtrl}
     * @param group       the {@link PDFDoc}
     * @return true if OCG layer group is checked, false otherwise.
     * @throws PDFNetException
     */
    public static boolean getChecked(@Nullable PDFViewCtrl pdfViewCtrl, @Nullable Group group) throws PDFNetException {
        if (pdfViewCtrl == null || group == null) {
            return false;
        }
        Context ctx = pdfViewCtrl.getOCGContext();
        return ctx.getState(group);
    }

    /**
     * Sets the checked state of the OCG layer group.
     *
     * @param pdfViewCtrl the {@link PDFViewCtrl}
     * @param group       the OCG group
     * @param checked     true if layer is checked, false otherwise
     * @throws PDFNetException
     */
    public static void setLayerCheckedChange(@Nullable PDFViewCtrl pdfViewCtrl, @Nullable Group group, boolean checked) throws PDFNetException {
        if (pdfViewCtrl == null || group == null) {
            return;
        }
        Context ctx = pdfViewCtrl.getOCGContext();
        ctx.setState(group, checked);
        pdfViewCtrl.update(true);
    }

    public static boolean hasPdfLayer(PDFDoc doc) {
        boolean shouldUnlockRead = false;
        try {
            doc.lockRead();
            shouldUnlockRead = true;
            return doc.hasOC();
        } catch (PDFNetException e) {
            AnalyticsHandlerAdapter.getInstance().sendException(e);
        } finally {
            if (shouldUnlockRead) {
                Utils.unlockReadQuietly(doc);
            }
        }
        return false;
    }

    public static class LayerInfo {
        public Group group;
        public boolean checked;

        LayerInfo(Group group, boolean checked) {
            this.group = group;
            this.checked = checked;
        }
    }
}
