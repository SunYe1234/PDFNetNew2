package com.pdftron.pdf.dialog.pagelabel;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.pdftron.common.PDFNetException;
import com.pdftron.pdf.PDFDoc;
import com.pdftron.pdf.PDFViewCtrl;
import com.pdftron.pdf.PageLabel;
import com.pdftron.pdf.tools.R;
import com.pdftron.pdf.utils.AnalyticsHandlerAdapter;

import java.text.Collator;

/**
 * Class containing utility methods for handling page labels in a PDF document.
 */
public class PageLabelUtils {
    /**
     * Sets the page label for the given page range, style, prefix, and starting value.
     *
     * @param pdfViewCtrl the PDFViewCtrl
     * @param style       one of {@link PageLabel#e_alphabetic_lowercase}, {@link PageLabel#e_alphabetic_uppercase}
     *                    {@link PageLabel#e_decimal}, {@link PageLabel#e_none},
     *                    {@link PageLabel#e_roman_lowercase}, {@link PageLabel#e_roman_uppercase}
     * @param prefix      text to add to the page label title
     * @param startValue  the value to use when generating the numeric portion of the first label in this range
     * @param fromPage    the first page associated with this page label
     * @param toPage      the last page associated with this page label
     * @return the page label set to this page, null if unsuccessfully set.
     */
    @Nullable
    public static boolean setPageLabel(@NonNull PDFViewCtrl pdfViewCtrl, int style,
                                      @NonNull String prefix, int startValue, int fromPage, int toPage) {
        return PageLabelUtils.setDocPageLabel(pdfViewCtrl, style, prefix, startValue, fromPage, toPage);
    }

    /**
     * See {@link PageLabelUtils#setPageLabel(PDFViewCtrl, int, String, int, int, int)}
     *
     * @param pdfViewCtrl      the PDFViewCtrl
     * @param pageLabelSetting data model containing page modification user settings
     * @return
     */
    @Nullable
    public static boolean setPageLabel(@NonNull PDFViewCtrl pdfViewCtrl, @NonNull PageLabelSetting pageLabelSetting) {
        return PageLabelUtils.setPageLabel(pdfViewCtrl,
            pageLabelSetting.getPageLabelStyle(), pageLabelSetting.getPrefix(),
            pageLabelSetting.getStartNum(), pageLabelSetting.getFromPage(),
            pageLabelSetting.getToPage());
    }

    @Nullable
    static boolean setDocPageLabel(@NonNull PDFViewCtrl pdfViewCtrl, int style,
                                  @NonNull String prefix, int startValue, int fromPage, int toPage) {
        boolean shouldUnlock = false;
        boolean result = false;
        try {
            pdfViewCtrl.docLock(true);
            shouldUnlock = true;
            PDFDoc doc = pdfViewCtrl.getDoc();
            int totalPages = doc.getPageCount();

            if (!(fromPage >= 1 && toPage <= totalPages && (toPage >= fromPage))) {
                throw new IndexOutOfBoundsException("Invalid to and from pages. Was given from page %d to page %d");
            }

            // Check to see if we are overriding any pageLabels
            PageLabel lastOverridenLabel = null;
            for (int i = fromPage; i <= toPage; i++) {
                PageLabel currentLabel = doc.getPageLabel(i);
                if (currentLabel.isValid()) {
                    lastOverridenLabel = currentLabel;
                    // Remove all labels within this range
                    if (currentLabel.getFirstPageNum() >= fromPage) {
                        doc.removePageLabel(i);
                    }
                }
            }

            // Set the new page label
            PageLabel newLabel = PageLabel.create(doc, style, prefix, startValue);
            doc.setPageLabel(fromPage, newLabel);
            newLabel = doc.getPageLabel(fromPage);

            // Also set the page label for next pages if it does not have a page label
            int nextPage = toPage + 1;
            if (nextPage <= totalPages) {
                PageLabel nextLabel = doc.getPageLabel(nextPage);
                if (pageLabelEquals(nextLabel, newLabel)) { // If there is no old page label here, so write a new one
                    if (lastOverridenLabel == null) {
                        // If we did not override any page labels, then just make a new one
                        nextLabel = PageLabel.create(doc, PageLabel.e_decimal, "", fromPage);
                    } else {
                        // Otherwise we place the last overridden page label after our new one
                        int nextStart = lastOverridenLabel.getStart() +
                            fromPage - lastOverridenLabel.getFirstPageNum();
                        nextLabel = PageLabel.create(doc, lastOverridenLabel.getStyle(),
                            lastOverridenLabel.getPrefix(), nextStart);
                    }
                    doc.setPageLabel(nextPage, nextLabel);
                }
            }
            result = true;
        } catch (Exception e) {
            AnalyticsHandlerAdapter.getInstance().sendException(e);
        } finally {
            if (shouldUnlock) {
                pdfViewCtrl.docUnlock();
            }
        }
        return result;
    }

    private static boolean pageLabelEquals(PageLabel nextLabel, PageLabel newLabel) throws PDFNetException {
        return nextLabel.getPrefix().equals(newLabel.getPrefix()) &&
            nextLabel.getFirstPageNum() == newLabel.getFirstPageNum() &&
            nextLabel.getStyle() == newLabel.getStyle();
    }

    /**
     * Gets page label for a page
     *
     * @param pdfViewCtrl the PDFViewCtrl
     * @param page        the page
     * @return the page label
     */
    public static PageLabel getPageLabel(@NonNull PDFViewCtrl pdfViewCtrl, int page) {
        boolean shouldUnlockRead = false;
        try {
            pdfViewCtrl.docLockRead();
            shouldUnlockRead = true;
            PageLabel pageLabel = pdfViewCtrl.getDoc().getPageLabel(page);
            if (pageLabel.isValid()) {
                return pageLabel;
            }
        } catch (Exception e) {
            AnalyticsHandlerAdapter.getInstance().sendException(e);
        } finally {
            if (shouldUnlockRead) {
                pdfViewCtrl.docUnlockRead();
            }
        }
        return null;
    }

    /**
     * Gets page label prefix for a given page.
     *
     * @param pdfViewCtrl the PDFViewCtrl
     * @param page        the page
     * @return the page label title
     */
    public static String getPageLabelPrefix(@NonNull PDFViewCtrl pdfViewCtrl, int page) {
        boolean shouldUnlockRead = false;
        try {
            pdfViewCtrl.docLockRead();
            shouldUnlockRead = true;
            PageLabel pageLabel = pdfViewCtrl.getDoc().getPageLabel(page);
            if (pageLabel.isValid()) {
                return pageLabel.getPrefix();
            }
        } catch (Exception e) {
            AnalyticsHandlerAdapter.getInstance().sendException(e);
        } finally {
            if (shouldUnlockRead) {
                pdfViewCtrl.docUnlockRead();
            }
        }
        return null;
    }

    /**
     * Gets page label title for a given page.
     *
     * @param pdfViewCtrl the PDFViewCtrl
     * @param page        the page
     * @return the page label title
     */
    public static String getPageLabelTitle(@NonNull PDFViewCtrl pdfViewCtrl, int page) {
        boolean shouldUnlockRead = false;
        try {
            pdfViewCtrl.docLockRead();
            shouldUnlockRead = true;
            PageLabel pageLabel = pdfViewCtrl.getDoc().getPageLabel(page);
            if (pageLabel.isValid()) {
                return pageLabel.getLabelTitle(page);
            }
        } catch (Exception e) {
            AnalyticsHandlerAdapter.getInstance().sendException(e);
        } finally {
            if (shouldUnlockRead) {
                pdfViewCtrl.docUnlockRead();
            }
        }
        return null;
    }

    /**
     * Gets the page label in form of "curPage / pageCount"
     *
     * @param pdfViewCtrl the PDFViewCtrl
     * @param curPage     the current page number
     * @return the page label
     */
    public static String getPageNumberIndicator(@NonNull PDFViewCtrl pdfViewCtrl, int curPage) {
        return getPageNumberIndicator(pdfViewCtrl, curPage, pdfViewCtrl.getPageCount());
    }

    /**
     * Gets the page label in form of "curPage / pageCount"
     *
     * @param pdfViewCtrl the PDFViewCtrl
     * @param curPage     the current page number
     * @param pageCount   the total page number
     * @return the page label
     */
    public static String getPageNumberIndicator(@NonNull PDFViewCtrl pdfViewCtrl, int curPage, int pageCount) {
        boolean shouldUnlockRead = false;
        String pageRange = String.format(pdfViewCtrl.getContext().getResources().getString(R.string.page_range), curPage, pageCount);
        try {
            pdfViewCtrl.docLockRead();
            shouldUnlockRead = true;
            String curPageStr = getPageLabelTitle(pdfViewCtrl, curPage);
            if (curPageStr != null) {
                pageRange = String.format(pdfViewCtrl.getContext().getResources().getString(R.string.page_label_range), curPageStr, curPage, pageCount);
            }
        } catch (Exception e) {
            AnalyticsHandlerAdapter.getInstance().sendException(e);
        } finally {
            if (shouldUnlockRead) {
                pdfViewCtrl.docUnlockRead();
            }
        }
        return pageRange;
    }

    /**
     * Try to jump to a page label
     *
     * @param pdfViewCtrl the PDFViewCtrl
     * @param pageLabel   the page label
     * @return page number if found, -1 if not found
     */
    public static int getPageNumberFromLabel(@NonNull PDFViewCtrl pdfViewCtrl, String pageLabel) {
        Collator collator = Collator.getInstance();

        boolean shouldUnlockRead = false;
        try {
            pdfViewCtrl.docLockRead();
            shouldUnlockRead = true;
            int page_num = pdfViewCtrl.getDoc().getPageCount();
            PageLabel label;
            for (int i = 1; i <= page_num; ++i) {
                label = pdfViewCtrl.getDoc().getPageLabel(i);
                if (label.isValid()) {
                    String labelStr = label.getLabelTitle(i);
                    // compare two strings in the default locale
                    if (collator.equals(labelStr, pageLabel)) {
                        return i;
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
        return -1;
    }
}
