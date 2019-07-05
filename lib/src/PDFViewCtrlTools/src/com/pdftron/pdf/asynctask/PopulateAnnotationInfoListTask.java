//---------------------------------------------------------------------------------------
// Copyright (c) 2001-2019 by PDFTron Systems Inc. All Rights Reserved.
// Consult legal.txt regarding legal and license information.
//---------------------------------------------------------------------------------------

package com.pdftron.pdf.asynctask;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.pdftron.common.PDFNetException;
import com.pdftron.pdf.Annot;
import com.pdftron.pdf.PDFDoc;
import com.pdftron.pdf.PDFViewCtrl;
import com.pdftron.pdf.Page;
import com.pdftron.pdf.PageIterator;
import com.pdftron.pdf.Rect;
import com.pdftron.pdf.TextExtractor;
import com.pdftron.pdf.annots.Markup;
import com.pdftron.pdf.controls.AnnotationDialogFragment;
import com.pdftron.pdf.utils.AnalyticsHandlerAdapter;
import com.pdftron.pdf.utils.AnnotUtils;
import com.pdftron.pdf.utils.Utils;

import java.lang.ref.WeakReference;
import java.text.DateFormat;
import java.util.ArrayList;

/**
 * A class that asynchronously populates a list of annotation info, See {@link AnnotationDialogFragment.AnnotationInfo}
 */
@Deprecated
public class PopulateAnnotationInfoListTask extends AsyncTask<Void, Void, ArrayList<AnnotationDialogFragment.AnnotationInfo>> {

    private WeakReference<PDFViewCtrl> mPdfViewCtrlRef;

    private ArrayList<AnnotationDialogFragment.AnnotationInfo> mAnnotList;

    private Callback mCallback;

    /**
     * Callback interface invoked when annotations info are populated.
     */
    public interface Callback {
        /**
         * Called when annotations info have been populated.
         *
         * @param result The populated annotations info
         */
        void getAnnotationsInfo(ArrayList<AnnotationDialogFragment.AnnotationInfo> result, boolean done);
    }

    /**
     * Class constructor
     *
     * @param pdfViewCtrl The {@link com.pdftron.pdf.PDFViewCtrl}
     */
    public PopulateAnnotationInfoListTask(@NonNull PDFViewCtrl pdfViewCtrl) {
        mPdfViewCtrlRef = new WeakReference<>(pdfViewCtrl);
        mAnnotList = new ArrayList<>();
    }

    /**
     * Sets the callback listener.
     *
     * Sets the callback to null when the task is cancelled.
     *
     * @param callback The callback when the task is finished
     */
    public void setCallback(@Nullable Callback callback) {
        mCallback = callback;
    }

    /**
     * The overloaded implementation of {@link android.os.AsyncTask#doInBackground(Object[])}.
     **/
    @SuppressWarnings("ConstantConditions")
    @Override
    protected ArrayList<AnnotationDialogFragment.AnnotationInfo> doInBackground(Void... params) {
        PDFViewCtrl pdfViewCtrl = mPdfViewCtrlRef.get();
        if (pdfViewCtrl == null) {
            return null;
        }
        PageIterator pageIterator;
        boolean shouldUnlockRead = false;
        try {
            pdfViewCtrl.docLockRead();
            shouldUnlockRead = true;
            PDFDoc doc = pdfViewCtrl.getDoc();
            if (doc != null) {
                pageIterator = doc.getPageIterator(1);

                int pageNum = 0;
                TextExtractor textExtractor = new TextExtractor();

                while (pageIterator.hasNext() || mPdfViewCtrlRef.get() == null) {
                    if (isCancelled()) {
                        return null;
                    }

                    pageNum++;
                    Page page = pageIterator.next();

                    if (page.isValid()) {
                        ArrayList<Annot> annotations = pdfViewCtrl.getAnnotationsOnPage(pageNum);
                        for (Annot annotation : annotations) {
                            if (isCancelled() || mPdfViewCtrlRef.get() == null) {
                                return null;
                            }

                            try {
                                if (annotation == null || !annotation.isValid()) {
                                    continue;
                                }

                                String contents = "";
                                int type = AnnotUtils.getAnnotType(annotation);

                                if (AnnotUtils.getAnnotImageResId(type) == android.R.id.empty) {
                                    continue;
                                }

                                Markup markup = new Markup(annotation);
                                switch (type) {
                                    case Annot.e_FreeText:
                                        contents = annotation.getContents();
                                        break;
                                    case Annot.e_Underline:
                                    case Annot.e_StrikeOut:
                                    case Annot.e_Highlight:
                                    case Annot.e_Squiggly:
                                        // For text markup we show the text itself as the contents
                                        textExtractor.begin(page);
                                        contents = textExtractor.getTextUnderAnnot(annotation);
                                        break;
                                    default:
                                        break;
                                }
                                if (markup.getPopup() != null && markup.getPopup().isValid()) {
                                    String popupContent = markup.getPopup().getContents();
                                    if (!Utils.isNullOrEmpty(popupContent)) {
                                        contents = popupContent;
                                    }
                                }
                                java.util.Date annotLocalDate = AnnotUtils.getAnnotLocalDate(annotation);
                                String dateStr = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(annotLocalDate);
                                Rect rect = markup.getRect();
                                rect.normalize();
                                mAnnotList.add(
                                        new AnnotationDialogFragment.AnnotationInfo(
                                                type,
                                                pageNum,
                                                contents,
                                                markup.getTitle(),
                                                dateStr,
                                                annotation,
                                                rect.getY2())
                                );
                            } catch (PDFNetException ignored) {
                                // this annotation has some problem, let's skip it and continue with others
                            }
                        }
                        publishProgress();
                    }
                }
            }
        } catch (Exception e) {
            AnalyticsHandlerAdapter.getInstance().sendException(e);
        } finally {
            if (shouldUnlockRead) {
                pdfViewCtrl.docUnlockRead();
            }
        }

        return mAnnotList;
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);

        if (mCallback != null) {
            mCallback.getAnnotationsInfo(mAnnotList, false);
        }
    }

    /**
     * The overloaded implementation of {@link android.os.AsyncTask#onPostExecute(Object)}.
     **/
    @Override
    protected void onPostExecute(ArrayList<AnnotationDialogFragment.AnnotationInfo> result) {
        if (mCallback != null) {
            mCallback.getAnnotationsInfo(result, true);
        }
    }
}
