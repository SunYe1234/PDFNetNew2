// http://www.annalytics.co.uk/android/pdf/2017/04/06/Save-PDF-From-An-Android-WebView/
package android.print;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.support.annotation.Nullable;

import com.pdftron.pdf.model.ExternalFileInfo;
import com.pdftron.pdf.utils.AnalyticsHandlerAdapter;
import com.pdftron.pdf.utils.Utils;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * @hide
 */
@TargetApi(19)
public class PdfPrint {

    public interface PdfPrintListener {
        void onWriteFinished(String output);

        void onError(@Nullable String error);
    }

    private final PrintAttributes printAttributes;

    private PdfPrintListener mListener;

    public void setPdfPrintListener(PdfPrintListener listener) {
        mListener = listener;
    }

    public PdfPrint(PrintAttributes printAttributes) {
        this.printAttributes = printAttributes;
    }

    // For local files
    public void print(final PrintDocumentAdapter printAdapter, final File outputFolder, final String fileName) {
        boolean folderExists = outputFolder.exists() || outputFolder.mkdirs();
        // Get the output file path
        String outputPath = Utils.getFileNameNotInUse(new File(outputFolder, FilenameUtils.removeExtension(fileName) + ".pdf").getAbsolutePath());
        // Create the file from a File directory
        ParcelFileDescriptor fileDesc = folderExists ? getOutputFile(new File(outputPath)) : null;
        // Now print
        print(printAdapter, new File(outputPath), null, fileDesc);
    }

    // For external files (SD Card)
    public void print(Context context, final PrintDocumentAdapter printAdapter, final Uri outputFolder, final String fileName) {
        // Create the file in content Uri path
        ExternalFileInfo externalFile = new ExternalFileInfo(context, null, outputFolder);
        String outputPath = Utils.getFileNameNotInUse(externalFile, fileName);
        ExternalFileInfo outputFile = externalFile.createFile("application/pdf", outputPath);

        if (outputFile != null) {
            ParcelFileDescriptor fileDesc = getOutputUriFile(context, outputFile.getUri());
            // Now print
            print(printAdapter, null, outputFile, fileDesc);
        } else {
            mListener.onError(null);
        }
    }

    private void print(final PrintDocumentAdapter printAdapter,
                       final File localFile,
                       final ExternalFileInfo externalFile,
                       final ParcelFileDescriptor fileDescriptor) {

        String inputPath = null;
        if (localFile != null) {
            inputPath = localFile.getAbsolutePath();
        } else if (externalFile != null) {
            inputPath = externalFile.getUri().toString();
        }
        if (null == inputPath) {
            return;
        }
        final String outputFilePath = inputPath;

        final PrintDocumentAdapter.WriteResultCallback wc = new PrintDocumentAdapter.WriteResultCallback() {

            private void handleError() {
                if (localFile != null) {
                    localFile.delete();
                } else {
                    externalFile.delete();
                }
            }

            private void closeFileDescriptor() {
                if (fileDescriptor != null) {
                    try {
                        fileDescriptor.close();
                    } catch (IOException e) {
                        AnalyticsHandlerAdapter.getInstance().sendException(e);
                    }
                }
            }

            @Override
            public void onWriteFinished(PageRange[] pages) {
                closeFileDescriptor();
                super.onWriteFinished(pages);
                if (mListener != null) {
                    mListener.onWriteFinished(outputFilePath);
                }
            }

            @Override
            public void onWriteFailed(CharSequence error) {
                closeFileDescriptor();
                handleError();
                super.onWriteFailed(error);
                if (mListener != null) {
                    mListener.onError(error != null ? error.toString() : null);
                }
            }

            @Override
            public void onWriteCancelled() {
                closeFileDescriptor();
                handleError();
                super.onWriteCancelled();
                if (mListener != null) {
                    mListener.onError(null);
                }
            }
        };

        final PrintDocumentAdapter.LayoutResultCallback lc = new PrintDocumentAdapter.LayoutResultCallback() {

            @Override
            public void onLayoutCancelled() {
                super.onLayoutCancelled();
                if (mListener != null) {
                    mListener.onError(null);
                }
            }

            @Override
            public void onLayoutFailed(CharSequence error) {
                super.onLayoutFailed(error);
                if (mListener != null) {
                    mListener.onError(error != null ? error.toString() : null);
                }
            }

            @Override
            public void onLayoutFinished(PrintDocumentInfo info, boolean changed) {
                if (null == fileDescriptor) {
                    if (mListener != null) {
                        mListener.onError(null);
                    }
                    return;
                }
                printAdapter.onWrite(new PageRange[]{PageRange.ALL_PAGES}, fileDescriptor, new CancellationSignal(), wc);
            }
        };

        printAdapter.onLayout(null, printAttributes, null, lc, null);
    }

    private ParcelFileDescriptor getOutputUriFile(Context context, Uri filePath) {
        try {
            ContentResolver cr = Utils.getContentResolver(context);
            if (cr == null) {
                return null;
            }
            return cr.openFileDescriptor(filePath, "rw");
        } catch (FileNotFoundException e) {
            AnalyticsHandlerAdapter.getInstance().sendException(e);
        }
        return null;
    }

    private ParcelFileDescriptor getOutputFile(File outputPath) {
        // Get the output file path
        try {
            boolean success = outputPath.createNewFile();
            if (success) {
                return ParcelFileDescriptor.open(outputPath, ParcelFileDescriptor.MODE_READ_WRITE);
            }
        } catch (Exception e) {
            AnalyticsHandlerAdapter.getInstance().sendException(e);
        }
        return null;
    }
}
