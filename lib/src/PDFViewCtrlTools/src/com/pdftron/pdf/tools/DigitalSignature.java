package com.pdftron.pdf.tools;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Environment;
import android.support.annotation.Keep;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.pdftron.common.PDFNetException;
import com.pdftron.pdf.Annot;
import com.pdftron.pdf.Field;
import com.pdftron.pdf.PDFDoc;
import com.pdftron.pdf.PDFViewCtrl;
import com.pdftron.pdf.Page;
import com.pdftron.pdf.annots.Widget;
import com.pdftron.pdf.dialog.DialogSignatureInfo;
import com.pdftron.pdf.model.AnnotStyle;
import com.pdftron.pdf.utils.AnalyticsHandlerAdapter;
import com.pdftron.pdf.utils.CommonToast;
import com.pdftron.pdf.utils.MySignatureHandler;
import com.pdftron.pdf.utils.Utils;
import com.pdftron.sdf.Obj;
import com.pdftron.sdf.SDFDoc;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

@Keep
public class DigitalSignature extends Signature {

    private static final String DEFAULT_KEYSTORE = "https://pdftron.s3.amazonaws.com/downloads/android/pdftron.pfx";

    // Disposables
    private CompositeDisposable mDisposables;

    private File mKeystore;
    private String mPassword = "password";

    private boolean mDownloadingKeystore;


    /**
     * Class constructor
     */
    public DigitalSignature(@NonNull PDFViewCtrl ctrl) {
        super(ctrl);

        mConfirmBtnStrRes = R.string.tools_qm_sign_and_save;
        mDisposables = new CompositeDisposable();

        mNextToolMode = getToolMode();
    }

    @Override
    public void onClose() {
        super.onClose();

        if (mDisposables != null && !mDisposables.isDisposed()) {
            mDisposables.dispose();
        }
    }

    @Override
    public ToolManager.ToolModeBase getToolMode() {
        return ToolManager.ToolMode.DIGITAL_SIGNATURE;
    }

    @Override
    public int getCreateAnnotType() {
        return AnnotStyle.CUSTOM_ANNOT_TYPE_SIGNATURE;
    }

    private final static String DEFAULT_FILE_NAME_SIGNED = Environment.getExternalStorageDirectory().getAbsolutePath() + "/sample_signed_0.pdf";

    @Override
    protected boolean addSignatureStampToWidget(Page page) {
        boolean success = super.addSignatureStampToWidget(page);

        if (success) {
            signPdf();
        }

        return success;
    }

    @Override
    protected void handleExistingSignatureWidget(int x, int y) {
        boolean handled = false;
        if (mWidget != null) {
            try {
                Obj sigDict = mWidget.getField().getValue();
                if (sigDict != null) {
                    showSignatureInfo();
                    handled = true;
                }
            } catch (Exception ignored) {
            }
        }
        if (!handled) {
            super.handleExistingSignatureWidget(x, y);
        }
    }

    protected void signPdf() {
        try {
            ToolManager toolManager = (ToolManager) mPdfViewCtrl.getToolManager();
            String keystore = toolManager.getDigitalSignatureKeystore();
            String password = toolManager.getDigitalSignatureKeystorePassword();
            if (!Utils.isNullOrEmpty(keystore) && (new File(keystore)).exists() &&
                !Utils.isNullOrEmpty(password)) {
                mKeystore = new File(keystore);
                mPassword = password;
                signPdfImpl();
            } else {
                File sigFolder = new File(mPdfViewCtrl.getContext().getFilesDir(), "digitalsignature");
                if (!sigFolder.exists() || !sigFolder.isDirectory()) {
                    FileUtils.forceMkdir(sigFolder);
                }
                mKeystore = new File(sigFolder, "pdftron.pfx");
                if (mKeystore.exists()) {
                    signPdfImpl();
                } else {
                    final ProgressDialog progressDialog = new ProgressDialog(mPdfViewCtrl.getContext());
                    mDownloadingKeystore = true;
                    mDisposables.add(Utils.simpleHTTPDownload(DEFAULT_KEYSTORE, mKeystore)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                            .doOnSubscribe(new Consumer<Disposable>() {
                                @Override
                                public void accept(Disposable disposable) throws Exception {
                                    progressDialog.setMessage(mPdfViewCtrl.getContext().getString(R.string.tools_misc_please_wait));
                                    progressDialog.setCancelable(false);
                                    progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                                    progressDialog.setIndeterminate(true);
                                    progressDialog.show();
                                }
                            })
                        .subscribe(new Consumer<File>() {
                            @Override
                            public void accept(File file) throws Exception {
                                mKeystore = file;
                                signPdfImpl();
                                progressDialog.dismiss();
                                mDownloadingKeystore = false;
                            }
                        }, new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) throws Exception {
                                progressDialog.dismiss();
                                mDownloadingKeystore = false;
                            }
                        })
                    );
                }
            }
        } catch (Exception ex) {
            AnalyticsHandlerAdapter.getInstance().sendException(ex);
        }
    }

    /**
     * Signs the PDF document.
     */
    protected void signPdfImpl() {
        if (mKeystore == null || !mKeystore.exists()) {
            return;
        }
        String fileName;
        String origFileName = null;
        // Try to use the current doc filename
        try {
            fileName = mPdfViewCtrl.getDoc().getFileName();
            origFileName = mPdfViewCtrl.getDoc().getFileName();
            if (fileName == null || fileName.length() == 0) {
                fileName = DEFAULT_FILE_NAME_SIGNED;
            } else {
                String s = fileName.substring(0, fileName.lastIndexOf("."));
                fileName = s + "_signed_0.pdf";
            }
        } catch (Exception e) {
            fileName = DEFAULT_FILE_NAME_SIGNED;
        }

        // Check for existing signed files and pick up a new name
        // so to not overwrite them.
        int i = 1;
        do {
            File signedFile = new File(fileName);
            if (signedFile.exists()) {
                String s = fileName.substring(0, fileName.lastIndexOf("_"));
                fileName = s + "_" + (i++) + ".pdf";
            } else {
                break;
            }
        } while (true);

        if (null != origFileName) {
            saveFile(origFileName);
            copyFile(origFileName, fileName);
            File copiedFile = new File(fileName);
            int currPage = mPdfViewCtrl.getCurrentPage();
            boolean shouldUnlock = false;
            PDFDoc copiedDoc = null;

            try {
                Annot selectedAnnot = null;
                Widget widget = null;
                copiedDoc = new PDFDoc(copiedFile.getAbsolutePath());
                // get field from new PDFDoc
                copiedDoc.lock();
                shouldUnlock = true;
                Page page = copiedDoc.getPage(currPage);
                int annotationCount = page.getNumAnnots();
                for (int a = 0; a < annotationCount; a++) {
                    try {
                        Annot annotation = page.getAnnot(a);
                        if (null != annotation &&
                            annotation.isValid() &&
                            annotation.getSDFObj().getObjNum() == mAnnot.getSDFObj().getObjNum()) {
                            selectedAnnot = annotation;
                            break;
                        }
                    } catch (PDFNetException e) {
                        // this annotation has some problem, let's skip it and continue with others
                        AnalyticsHandlerAdapter.getInstance().sendException(e);
                    }
                }

                if (selectedAnnot != null) {
                    widget = new Widget(selectedAnnot);
                }

                if (widget != null) {
                    // There are two options to digitally sign the document with PDFNet:
                    // 1. The full version has a built-in signature handler, which can be used by
                    // calling PDFDoc.addStdSignatureHandler(). This way you don't need to extend the
                    // SignatureHandler interface and don't need to include any cryptographic libraries
                    // to your project (eg, Spongy Castle).
                    // 2. In case you are using the standard version, then you will have to create a
                    // class that extends the SignatureHandler interface and code your own code that
                    // defines the digest and cipher algorithms to sign the document.

                    // If you are using the full version, you can use the code below:
                    // ===== FULL VERSION START =====
//                  InputStream is = mPdfViewCtrl.getContext().getResources().openRawResource(R.raw.pdftron);
//                  ByteArrayOutputStream baos = new ByteArrayOutputStream();
//                  int reads = is.read();
//                  while (reads != -1) {
//                      baos.write(reads);
//                      reads = is.read();
//                  }
//                  long sigHandlerId = copiedDoc.addStdSignatureHandler(baos.toByteArray(), mPassword");
//                  ===== FULL VERSION END =====

                    // In case your are using the standard version, then you can use the following
                    // implementation:
                    // ===== STANDARD VERSION START =====
                    // Create a new instance of the SignatureHandler.

                    MySignatureHandler sigCreator = new MySignatureHandler(mKeystore.getAbsolutePath(), mPassword);
                    // Add the SignatureHandler instance to PDFDoc, making sure to keep track of
                    // it using the ID returned.
                    long sigHandlerId = copiedDoc.addSignatureHandler(sigCreator);
                    // ===== STANDARD VERSION END =====

                    // Tell PDFNet to use the SignatureHandler created to sign the new signature
                    // form field.
                    Field sigField = widget.getField();
                    Obj sigDict = sigField.useSignatureHandler(sigHandlerId);

                    // Add more information to the signature dictionary
                    sigDict.putName("SubFilter", "adbe.pkcs7.detached");
                    sigDict.putString("Name", "PDFTron");
                    sigDict.putString("Location", "Vancouver, BC");
                    sigDict.putString("Reason", "Document verification.");

                    copiedDoc.save(copiedFile.getAbsolutePath(), SDFDoc.SaveMode.INCREMENTAL, null);
                    ToolManager toolManager = (ToolManager) mPdfViewCtrl.getToolManager();
                    toolManager.onNewFileCreated(copiedFile);
                    CommonToast.showText(mPdfViewCtrl.getContext(), String.format(getStringFromResId(R.string.tools_digitalsignature_msg_saved), copiedFile.getAbsolutePath()), Toast.LENGTH_LONG);
                } else {
                    CommonToast.showText(mPdfViewCtrl.getContext(), getStringFromResId(R.string.tools_digitalsignature_msg_file_locked), Toast.LENGTH_LONG);
                }
            } catch (Exception e) {
                CommonToast.showText(mPdfViewCtrl.getContext(), String.format(getStringFromResId(R.string.tools_digitalsignature_msg_failed_to_save), e.getMessage()), Toast.LENGTH_LONG);
            } finally {
                if (shouldUnlock) {
                    Utils.unlockQuietly(copiedDoc);
                }
                Utils.closeQuietly(copiedDoc);
            }
        }
    }

    @Override
    protected void unsetAnnot() {
        if (mDownloadingKeystore) {
            return;
        }
        super.unsetAnnot();
    }

    /**
     * Shows the signature info
     */
    protected void showSignatureInfo() {
        // Get some information from the /V entry
        if (mAnnot != null) {
            try {
                Widget widget = new Widget(mAnnot);
                Obj sigDict = widget.getField().getValue();
                if (sigDict != null) {
                    String location = sigDict.findObj("Location").getAsPDFText();
                    String reason = sigDict.findObj("Reason").getAsPDFText();
                    String name = sigDict.findObj("Name").getAsPDFText();

                    DialogSignatureInfo dialog = new DialogSignatureInfo(mPdfViewCtrl.getContext());
                    dialog.setLocation(location);
                    dialog.setReason(reason);
                    dialog.setName(name);
                    dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            unsetAnnot();
                            mNextToolMode = ToolManager.ToolMode.PAN;
                        }
                    });
                    dialog.show();
                }
            } catch (Exception e) {
                // Do nothing...
            }
        }
    }

    private void saveFile(String oldPath) {
        boolean shouldUnlock = false;
        try {
            mPdfViewCtrl.docLock(true);
            shouldUnlock = true;
            mPdfViewCtrl.getDoc().save(oldPath, SDFDoc.SaveMode.INCREMENTAL, null);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (shouldUnlock) {
                mPdfViewCtrl.docUnlock();
            }
        }
    }


    private void copyFile(String oldPath, String newPath) {
        InputStream is = null;
        OutputStream fos = null;
        try {
            is = new FileInputStream(oldPath);
            fos = new FileOutputStream(newPath);
            IOUtils.copy(is, fos);
        } catch (Exception e) {
            AnalyticsHandlerAdapter.getInstance().sendException(e);
        } finally {
            Utils.closeQuietly(fos);
            Utils.closeQuietly(is);
        }
    }
}
