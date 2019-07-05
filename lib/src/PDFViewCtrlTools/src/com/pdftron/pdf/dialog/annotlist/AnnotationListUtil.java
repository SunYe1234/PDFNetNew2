package com.pdftron.pdf.dialog.annotlist;

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

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;

/**
 * Utility class for fetching annotations in {@link PDFViewCtrl}.
 */
public class AnnotationListUtil {

    /**
     * Create an {@link Observable} that retrieves all annotations in a {@link PDFViewCtrl}, and
     * emits a list of annotations one page at a time.
     *
     * @param pdfViewCtrl the PDFViewCtrl to obtain
     * @return observable that emits annotations each page at a time.
     */
    public static Observable<List<AnnotationDialogFragment.AnnotationInfo>> from(@Nullable final PDFViewCtrl pdfViewCtrl) {
        return Observable.create(new ObservableOnSubscribe<List<AnnotationDialogFragment.AnnotationInfo>>() {
            @Override
            public void subscribe(ObservableEmitter<List<AnnotationDialogFragment.AnnotationInfo>> emitter) throws Exception {
                if (pdfViewCtrl == null) {
                    emitter.onComplete();
                    return;
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

                        while (pageIterator.hasNext() || pdfViewCtrl == null) {
                            if (emitter.isDisposed()) {
                                emitter.onComplete();
                                return;
                            }

                            pageNum++;
                            Page page = pageIterator.next();

                            List<AnnotationDialogFragment.AnnotationInfo> pageAnnots = new ArrayList<>();
                            if (page.isValid()) {
                                ArrayList<Annot> annotations = pdfViewCtrl.getAnnotationsOnPage(pageNum);
                                for (Annot annotation : annotations) {
                                    if (emitter.isDisposed() || pdfViewCtrl == null) {
                                        emitter.onComplete();
                                        return;
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
                                        pageAnnots.add(
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
                                // Emit this page's annotations
                                emitter.onNext(pageAnnots);
                            }
                        }
                    }
                } catch (Exception e) {
                    AnalyticsHandlerAdapter.getInstance().sendException(e);
                    emitter.onError(e);
                } finally {
                    if (shouldUnlockRead) {
                        pdfViewCtrl.docUnlockRead();
                    }
                    emitter.onComplete();
                }
            }
        });
    }
}
