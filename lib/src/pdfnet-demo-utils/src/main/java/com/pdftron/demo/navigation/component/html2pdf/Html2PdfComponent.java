package com.pdftron.demo.navigation.component.html2pdf;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import com.pdftron.demo.navigation.component.html2pdf.view.HtmlConversionUiView;
import com.pdftron.pdf.utils.AnalyticsHandlerAdapter;
import com.pdftron.pdf.utils.HTML2PDF;

/**
 * Component used to convert HTML pages to PDF using {@link HTML2PDF}
 */
public class Html2PdfComponent extends HtmlConversionComponent {

    public Html2PdfComponent(@NonNull View view, @Nullable HtmlConversionListener listener) {
        super(new HtmlConversionUiView(view), listener);
    }

    @Override
    protected void fromUrl(@NonNull final Context context, @NonNull final String url,
                           @NonNull final Uri outputFolderUri, @NonNull String outputFileName) {

        // Only use HTML2PDF for html conversion if at least kitkat
        // Update the UI when starting conversion
        mHtml2PdfView.showProgress();

        // Do the conversion
        AnalyticsHandlerAdapter.getInstance().sendTimedEvent(AnalyticsHandlerAdapter.HTML2PDF_CONVERSION);
        HTML2PDF.HTML2PDFListener listener = new HTML2PDF.HTML2PDFListener() {
            @Override
            public void onConversionFinished(String pdfOutput, boolean isLocal) {
                if (mListener != null) {
                    mListener.onConversionFinished(pdfOutput, isLocal);
                }
                // Finish conversion, hide the progress bar
                mHtml2PdfView.hideProgress();
                AnalyticsHandlerAdapter.getInstance().endTimedEvent(AnalyticsHandlerAdapter.HTML2PDF_CONVERSION);
            }

            @Override
            public void onConversionFailed(String error) {
                if (mListener != null) {
                    mListener.onConversionFailed(error != null ? error : "Could not convert webpage.");
                }
                // Finish conversion, hide the progress bar
                mHtml2PdfView.hideProgress();
                AnalyticsHandlerAdapter.getInstance().endTimedEvent(AnalyticsHandlerAdapter.HTML2PDF_CONVERSION);
            }
        };
        HTML2PDF.fromUrl(context, url, outputFolderUri, outputFileName, listener);
    }

    @Override
    public void setOutputFilename(@NonNull String outputFilename) {
        mOutputFilename = outputFilename;
    }

}
