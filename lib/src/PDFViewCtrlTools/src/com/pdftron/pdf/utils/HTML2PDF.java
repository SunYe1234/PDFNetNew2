package com.pdftron.pdf.utils;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.print.PdfPrint;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.webkit.URLUtil;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.pdftron.pdf.tools.R;

import java.io.File;

/**
 * Utility class to convert HTML to PDF. API 19+ only.
 * The following permissions are required:
 * <code><uses-permission android:name="android.permission.INTERNET" /></code>
 * <code><uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" /></code>
 *
 * <p>
 * Sample usage:
 * <pre>
 *  HTML2PDF.fromUrl(activity, someLink, new HTML2PDF.HTML2PDFListener() {
 *
 *      public void onConversionFinished(String pdfOutput) {
 *
 *      }
 *
 *
 *      public void onConversionFailed() {
 *
 *      }
 *  });
 * </pre>
 */
@TargetApi(Build.VERSION_CODES.KITKAT)
public class HTML2PDF {

    private static final boolean sDebug = false;
    public static final String TAG = HTML2PDF.class.getName();

    public interface HTML2PDFListener {
        void onConversionFinished(String pdfOutput, boolean isLocal);

        void onConversionFailed(@Nullable String error);
    }

    @NonNull
    private Uri mOutputFolderUri = Uri.fromFile(
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + "/HTML2PDF/")
    );
    private WebView mWebView;
    private HTML2PDFListener mListener;

    /**
     * The output folder path, default to "Download/HTML2PDF"
     *
     * @param outputFolder The output file path
     */
    public void setOutputFolder(File outputFolder) {
        this.mOutputFolderUri = Uri.fromFile(outputFolder);
    }

    /**
     * The output folder path, default to "Download/HTML2PDF"
     *
     * @param outputFolder The output path as a Uri
     */
    public void setOutputFolder(Uri outputFolder) {
        this.mOutputFolderUri = outputFolder;
    }

    /**
     * The output file name, default to website title.
     *
     * @param fileName the output file name
     */
    public void setOutputFileName(String fileName) {
        this.mOutputFileName = fileName;
    }

    /**
     * The horizontal dpi, default to 600
     *
     * @param horizontalDpi The horizontal dpi
     */
    public void setHorizontalDpi(int horizontalDpi) {
        this.mHorizontalDpi = horizontalDpi;
    }

    /**
     * The vertical dpi, default to 600
     *
     * @param verticalDpi The vertical dpi
     */
    public void setVerticalDpi(int verticalDpi) {
        this.mVerticalDpi = verticalDpi;
    }

    /**
     * The margin, default to {@link PrintAttributes.Margins#NO_MARGINS}
     *
     * @param margins The margin
     */
    public void setMargins(PrintAttributes.Margins margins) {
        this.mMargins = margins;
    }

    /**
     * The page size, default to {@link PrintAttributes.MediaSize#NA_LETTER}
     *
     * @param mediaSize The page size
     */
    public void setMediaSize(PrintAttributes.MediaSize mediaSize) {
        this.mMediaSize = mediaSize;
    }

    private static final String DEFAULT_FILE_NAME = "untitled.pdf";
    private String mOutputFileName = DEFAULT_FILE_NAME;
    private int mHorizontalDpi = 600;
    private int mVerticalDpi = 600;
    private PrintAttributes.Margins mMargins = PrintAttributes.Margins.NO_MARGINS;
    private PrintAttributes.MediaSize mMediaSize = PrintAttributes.MediaSize.NA_LETTER;

    public void setHTML2PDFListener(HTML2PDFListener listener) {
        mListener = listener;
    }

    public HTML2PDF(Context context) {
        mWebView = new WebView(context);
    }

    public HTML2PDF(WebView webView) {
        mWebView = webView;
    }

    public void doHtml2Pdf() {
        mWebView.setWebViewClient(new WebViewClient() {

            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                if (sDebug) {
                    Log.i(TAG, "page finished loading " + url);
                }
                // If output file name is not set, then use the webpage title as output pdf name
                mOutputFileName =
                    Utils.getValidFilename(
                        mOutputFileName.equals(DEFAULT_FILE_NAME) ?
                            view.getTitle() : mOutputFileName);

                createWebPrintJob();
            }
        });
    }

    /**
     * Convert from a URL link
     *
     * @param context  the context
     * @param url      the link
     * @param listener the listener
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static void fromUrl(Context context, String url, HTML2PDFListener listener) {
        HTML2PDF html2PDF = new HTML2PDF(context);
        html2PDF.setHTML2PDFListener(listener);
        html2PDF.fromUrl(url);
    }

    /**
     * Convert from a URL link
     *
     * @param context  the context
     * @param url      the link
     * @param folder   the destination folder
     * @param listener the listener
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static void fromUrl(@NonNull Context context, @NonNull String url, @NonNull File folder, @Nullable HTML2PDFListener listener) {
        HTML2PDF html2PDF = new HTML2PDF(context);
        html2PDF.setOutputFolder(folder);
        html2PDF.setHTML2PDFListener(listener);
        html2PDF.fromUrl(url);
    }

    /**
     * Convert from a URL link
     *
     * @param context  the context
     * @param url      the link
     * @param folder   the destination folder
     * @param listener the listener
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static void fromUrl(@NonNull Context context, @NonNull String url, @NonNull Uri folder, @Nullable HTML2PDFListener listener) {
        HTML2PDF html2PDF = new HTML2PDF(context);
        html2PDF.setOutputFolder(folder);
        html2PDF.setHTML2PDFListener(listener);
        html2PDF.fromUrl(url);
    }

    /**
     * Convert from a URL link
     *
     * @param context  the context
     * @param url      the link
     * @param folder   the destination folder
     * @param listener the listener
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static void fromUrl(@NonNull Context context, @NonNull String url, @NonNull Uri folder,
                               @NonNull String outputFileName, @Nullable HTML2PDFListener listener) {
        HTML2PDF html2PDF = new HTML2PDF(context);
        html2PDF.setOutputFolder(folder);
        html2PDF.setHTML2PDFListener(listener);
        html2PDF.setOutputFileName(outputFileName);
        html2PDF.fromUrl(url);
    }

    /**
     * Convert from HTML document
     *
     * @param context      the context
     * @param baseUrl      the base URL
     * @param htmlDocument the HTML string
     * @param listener     the listener
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static void fromHTMLDocument(@NonNull Context context, @Nullable String baseUrl, @NonNull String htmlDocument, @Nullable HTML2PDFListener listener) {
        HTML2PDF html2PDF = new HTML2PDF(context);
        html2PDF.setHTML2PDFListener(listener);
        html2PDF.fromHTMLDocument(baseUrl, htmlDocument);
    }

    /**
     * Convert from content in a WebView
     *
     * @param webView  the WebView
     * @param listener the listener
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static void fromWebView(@NonNull WebView webView, @Nullable HTML2PDFListener listener) {
        HTML2PDF html2PDF = new HTML2PDF(webView);
        html2PDF.setHTML2PDFListener(listener);
        html2PDF.doHtml2Pdf();
    }

    /**
     * Convert from a URL link
     *
     * @param url the URL link
     */
    public void fromUrl(String url) {
        mWebView.loadUrl(url);
        doHtml2Pdf();
    }

    /**
     * Convert from HTML document
     *
     * @param baseUrl      the base URL
     * @param htmlDocument the HTML string
     */
    public void fromHTMLDocument(String baseUrl, String htmlDocument) {
        mWebView.loadDataWithBaseURL(baseUrl, htmlDocument, "text/HTML", "UTF-8", null);
        doHtml2Pdf();
    }

    private void createWebPrintJob() {
        if (mWebView == null) {
            return;
        }
        Context context = mWebView.getContext();

        PrintDocumentAdapter printAdapter = null;
        if (Utils.isLollipop()) {
            String jobName = context.getString(R.string.app_name) + " Document";
            printAdapter = mWebView.createPrintDocumentAdapter(jobName);
        } else if (Utils.isKitKat()) {
            printAdapter = mWebView.createPrintDocumentAdapter();
        } else {
            throw new RuntimeException("Android 19 (KitKat) is required to use HTML2PDF");
        }

        if (printAdapter != null) {
            if (ContentResolver.SCHEME_CONTENT.equals(mOutputFolderUri.getScheme())) {
                // If scheme is a content
                PdfPrint pdfPrint = setupPdfPrint(false);
                pdfPrint.print(context, printAdapter, mOutputFolderUri, mOutputFileName);
            } else if (URLUtil.isHttpUrl(mOutputFolderUri.toString()) || URLUtil.isHttpsUrl(mOutputFolderUri.toString())) {
                // unsupported output, this should not happen
                if (mListener != null) {
                    mListener.onConversionFailed(null);
                }
            } else {
                // If scheme is a file
                PdfPrint pdfPrint = setupPdfPrint(true);
                String outputPath = mOutputFolderUri.getPath();
                if (outputPath != null) {
                    pdfPrint.print(printAdapter, new File(mOutputFolderUri.getPath()), mOutputFileName);
                } else {
                    if (mListener != null) {
                        mListener.onConversionFailed(null);
                    }
                }
            }
        }
    }

    private PdfPrint setupPdfPrint(final boolean isLocal) {
        PrintAttributes attributes = new PrintAttributes.Builder()
            .setMediaSize(mMediaSize)
            .setResolution(new PrintAttributes.Resolution("pdf", "pdf", mHorizontalDpi, mVerticalDpi))
            .setMinMargins(mMargins).build();

        PdfPrint pdfPrint = new PdfPrint(attributes);
        pdfPrint.setPdfPrintListener(new PdfPrint.PdfPrintListener() {
            @Override
            public void onWriteFinished(String output) {
                if (sDebug) {
                    Log.i(TAG, "done creating pdf at: " + output);
                }
                if (mListener != null) {
                    mListener.onConversionFinished(output, isLocal);
                }
            }

            @Override
            public void onError(String error) {
                if (mListener != null) {
                    mListener.onConversionFailed(error);
                }
            }
        });
        return pdfPrint;
    }

}
