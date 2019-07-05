package com.pdftron.demo.navigation.component.html2pdf.view;

import android.app.ProgressDialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;

import com.pdftron.demo.R;

/**
 * View for HTML conversion. Contains a progress dialog to show
 * conversion in progress.
 */
public class HtmlConversionUiView implements Html2PdfView {
    @NonNull
    private final ProgressDialog mImportWebpageProgressDialog;

    public HtmlConversionUiView(@NonNull View container) {
        Context context = container.getContext();
        mImportWebpageProgressDialog = new ProgressDialog(context);
        mImportWebpageProgressDialog.setMessage(context.getString(R.string.import_webpage_wait));
        mImportWebpageProgressDialog.setCancelable(false);
        mImportWebpageProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mImportWebpageProgressDialog.setIndeterminate(true);
    }

    public void hideProgress() {
        mImportWebpageProgressDialog.dismiss();
    }

    public void showProgress() {
        mImportWebpageProgressDialog.show();
    }
}
