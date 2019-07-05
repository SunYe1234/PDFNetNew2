package com.pdftron.demo.browser;

import com.pdftron.pdf.utils.Constants;
import com.pdftron.pdf.utils.PdfViewCtrlSettingsManager;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Test to ensure FileFilter retains the correct state when filter state is changed.
 */
public class FileFilterTest {
    private FileFilter mFileFilter;
    private MyFilterListener mListener;

    /**
     * Called before every @Test
     */
    @Before
    public void setupTestFileFilter() {
        mFileFilter = new FileFilter(null, PdfViewCtrlSettingsManager.KEY_PREF_SUFFIX_LOCAL_FILES);
        mListener = new MyFilterListener();
        mFileFilter.setListener(mListener);
    }

    @Test
    public void check_filter_flow() {
        // Assert default state in listener
        assertState(true, true, true, true);

        // Enable filters one by one
        startFiltering(Constants.FILE_TYPE_PDF);
        assertState(true, false, false, true);

        startFiltering(Constants.FILE_TYPE_DOC);
        assertState(true, true, false, true);

        startFiltering(Constants.FILE_TYPE_IMAGE);
        assertState(true, true, true, true);

        // Clear the filters
        clearFiltering();
        assertState(false, false, false, false);
    }

    @Test
    public void check_pdf_filter() {
        checkTypeFiltering(Constants.FILE_TYPE_PDF);
    }

    @Test
    public void check_doc_filter() {
        checkTypeFiltering(Constants.FILE_TYPE_DOC);
    }

    @Test
    public void check_images_filter() {
        checkTypeFiltering(Constants.FILE_TYPE_IMAGE);
    }

    private void checkTypeFiltering(int type) {
        // Assert default state in listener
        assertState(true, true, true, true);

        // Clear the filters
        clearFiltering();
        assertState(false, false, false, false);

        // Toggle PDF filtering
        startFiltering(type);
        assertState(type == Constants.FILE_TYPE_PDF,
            type == Constants.FILE_TYPE_DOC,
            type == Constants.FILE_TYPE_IMAGE,
            true);
        stopFiltering(type);
        assertState(false, false, false, false);
    }

    /*
        Convenience methods for testing
     */
    private void startFiltering(int type) {
        mFileFilter.enableFilteringFor(type);
    }

    private void stopFiltering(int type) {
        mFileFilter.disableFilteringFor(type);
    }

    private void clearFiltering() {
        mFileFilter.clearFiltering();
    }

    private void assertState(boolean isFilteringPdf, boolean isFilteringDoc,
                             boolean isFilteringImage, boolean isFiltering) {
        assertEquals(mListener.isFilteringPdf, isFilteringPdf);
        assertEquals(mListener.isFilteringDoc, isFilteringDoc);
        assertEquals(mListener.isFilteringImage, isFilteringImage);
        assertEquals(mListener.isFiltering, isFiltering);
    }

    /*
        Dummy Listener for testing
     */
    private static class MyFilterListener implements FileFilter.OnFilterStateChangedListener {

        boolean isFilteringPdf = true;
        boolean isFilteringDoc = true;
        boolean isFilteringImage = true;
        boolean isFiltering = true;

        @Override
        public void onFilterStateChanged(boolean isFilteringPdf, boolean isFilteringDoc,
                                         boolean isFilteringImage, boolean isFiltering) {
            this.isFilteringPdf = isFilteringPdf;
            this.isFilteringDoc = isFilteringDoc;
            this.isFilteringImage = isFilteringImage;
            this.isFiltering = isFiltering;
        }
    }
}
