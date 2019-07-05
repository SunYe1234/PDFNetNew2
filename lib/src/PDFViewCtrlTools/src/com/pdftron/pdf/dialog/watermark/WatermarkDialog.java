package com.pdftron.pdf.dialog.watermark;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.view.View;

import com.pdftron.pdf.Annot;
import com.pdftron.pdf.PDFViewCtrl;
import com.pdftron.pdf.controls.AnnotStyleDialogFragment;
import com.pdftron.pdf.model.AnnotStyle;
import com.pdftron.pdf.model.FontResource;
import com.pdftron.pdf.model.RulerItem;

/**
 * Dialog Fragment containing UI for watermark settings from the user.
 */
public class WatermarkDialog extends AnnotStyleDialogFragment {
    public static final String TAG = WatermarkDialog.class.getName();

    /**
     * Initializes a {@link WatermarkDialog} dialog with the default annotation style.
     */
    public static WatermarkDialog newInstance(@NonNull final PDFViewCtrl pdfViewCtrl) {
        // Default style for watermark
        final AnnotStyle annotStyle = new AnnotStyle();
        annotStyle.setAnnotType(Annot.e_Watermark);
        annotStyle.setOpacity(0.8f);
        annotStyle.setTextColor(Color.RED);
        annotStyle.setTextSize(72.0f);
        annotStyle.hasFillColor();
        annotStyle.setOverlayText("Sample Watermark");
        return newInstance(annotStyle, pdfViewCtrl);
    }

    /**
     * Initializes a {@link WatermarkDialog} with the given annotation style.
     *
     * @param annotStyle  initial style for the watermark style picker
     * @param pdfViewCtrl the PDFViewCtrl to add watermark to
     * @return the WatermarkDialog
     */
    public static WatermarkDialog newInstance(@NonNull AnnotStyle annotStyle, @NonNull final PDFViewCtrl pdfViewCtrl) {
        Bundle bundle = new Bundle();
        bundle.putString(ARGS_KEY_ANNOT_STYLE, annotStyle.toJSONString());

        final WatermarkDialog watermarkDialog = new WatermarkDialog();
        watermarkDialog.setArguments(bundle);
        watermarkDialog.setOnAnnotStyleChangeListener(new AnnotStyle.OnAnnotStyleChangeListener() {

            @Override
            public void onChangeAnnotThickness(float thickness, boolean done) {
                //do nothing
            }

            @Override
            public void onChangeAnnotTextSize(float textSize, boolean done) {
                if (done) {
                    WatermarkUtil.clearWatermark(pdfViewCtrl);
                    int color = watermarkDialog.getAnnotStyle().getTextColor();
                    float opacity = watermarkDialog.getAnnotStyle().getOpacity();
                    String text = watermarkDialog.getAnnotStyle().getOverlayText();
                    WatermarkUtil.setTextWatermark(pdfViewCtrl, text,
                            color, opacity, textSize, true);
                }
            }

            @Override
            public void onChangeAnnotTextColor(int textColor) {
                WatermarkUtil.clearWatermark(pdfViewCtrl);
                float opacity = watermarkDialog.getAnnotStyle().getOpacity();
                float textSize = watermarkDialog.getAnnotStyle().getTextSize();
                String text = watermarkDialog.getAnnotStyle().getOverlayText();
                WatermarkUtil.setTextWatermark(pdfViewCtrl, text,
                        textColor, opacity, textSize, true);
            }

            @Override
            public void onChangeAnnotOpacity(float opacity, boolean done) {
                if (done) {
                    WatermarkUtil.clearWatermark(pdfViewCtrl);
                    int color = watermarkDialog.getAnnotStyle().getTextColor();
                    float textSize = watermarkDialog.getAnnotStyle().getTextSize();
                    String text = watermarkDialog.getAnnotStyle().getOverlayText();
                    WatermarkUtil.setTextWatermark(pdfViewCtrl, text,
                            color, opacity, textSize, true);
                }
            }

            @Override
            public void onChangeAnnotStrokeColor(int color) {
                //do nothing
            }

            @Override
            public void onChangeAnnotFillColor(int color) {
                //do nothing
            }

            @Override
            public void onChangeAnnotIcon(String icon) {
                //do nothing
            }

            @Override
            public void onChangeAnnotFont(FontResource font) {
                //do nothing
            }

            @Override
            public void onChangeRulerProperty(RulerItem rulerItem) {
                //do nothing
            }

            @Override
            public void onChangeOverlayText(String overlayText) {
                WatermarkUtil.clearWatermark(pdfViewCtrl);
                int color = watermarkDialog.getAnnotStyle().getTextColor();
                float opacity = watermarkDialog.getAnnotStyle().getOpacity();
                float textSize = watermarkDialog.getAnnotStyle().getTextSize();
                WatermarkUtil.setTextWatermark(pdfViewCtrl, overlayText,
                        color, opacity, textSize, true);
            }

            @Override
            public void onChangeSnapping(boolean snap) {

            }
        });
        return watermarkDialog;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setAnnotPreviewVisibility(View.GONE);
    }

    @Override
    public void show(@NonNull FragmentManager fragmentManager) {
        if (isAdded()) {
            return;
        }
        show(fragmentManager, TAG);
    }
}
