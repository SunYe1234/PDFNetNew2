package com.pdftron.demo.browser;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import com.pdftron.demo.utils.MiscUtils;
import com.pdftron.pdf.model.FileInfo;
import com.pdftron.pdf.utils.Constants;
import com.pdftron.pdf.utils.PdfViewCtrlSettingsManager;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import org.apache.commons.io.FilenameUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @hide
 */
@SuppressWarnings("RedundantThrows")
class FileFilter {
    private final String[] mPDFExtensions = Constants.FILE_NAME_EXTENSIONS_PDF;
    private final String[] mDocExtensions = Constants.FILE_NAME_EXTENSIONS_DOC;
    private final String[] mImageExtensions = Constants.FILE_NAME_EXTENSIONS_IMAGE;
    private final FilterState mFilterState = new FilterState();
    private final String mFilteringSuffix;
    @Nullable
    private OnFilterStateChangedListener mListener;

    FileFilter(@Nullable Context context, @NonNull String filteringSuffix) {
        mFilteringSuffix = filteringSuffix;
        if (context != null) {
            loadCachedFilterState(context);
        }
    }

    void setListener(@Nullable OnFilterStateChangedListener listener) {
        mListener = listener;
    }

    void clearFiltering() {
        mFilterState.stopFiltering();
    }

    void enableFilteringFor(int fileType) {
        setFiltering(fileType, true);
    }

    void disableFilteringFor(int fileType) {
        setFiltering(fileType, false);
    }

    void setSearchQuery(@NonNull String query) {
        mFilterState.mSearchQuery = query;
    }

    void updateFromEvent(@NonNull FilesComponent.UserFilterEvent filterEvent) {
        switch (filterEvent) {
            // Handle filtering events from the UI
            case FILTER_NONE: {
                clearFiltering();
                break;
            }
            // Start filtering file types
            case ON_FILTER_PDF: {
                enableFilteringFor(Constants.FILE_TYPE_PDF);
                break;
            }
            case ON_FILTER_OFFICE: {
                enableFilteringFor(Constants.FILE_TYPE_DOC);
                break;
            }
            case ON_FILTER_IMAGES: {
                enableFilteringFor(Constants.FILE_TYPE_IMAGE);
                break;
            }
            // Stop filtering file types
            case OFF_FILTER_PDF: {
                disableFilteringFor(Constants.FILE_TYPE_PDF);
                break;
            }
            case OFF_FILTER_OFFICE: {
                disableFilteringFor(Constants.FILE_TYPE_DOC);
                break;
            }
            case OFF_FILTER_IMAGES: {
                disableFilteringFor(Constants.FILE_TYPE_IMAGE);
                break;
            }
        }
    }

    private void loadCachedFilterState(@NonNull Context context) {
        // Grab filter settings from shared preferences
        boolean shouldFilterPdf = PdfViewCtrlSettingsManager.getFileFilter(context,
            Constants.FILE_TYPE_PDF, mFilteringSuffix);
        boolean shouldFilterOffice = PdfViewCtrlSettingsManager.getFileFilter(context,
            Constants.FILE_TYPE_DOC, mFilteringSuffix);
        boolean shouldFilterImage = PdfViewCtrlSettingsManager.getFileFilter(context,
            Constants.FILE_TYPE_IMAGE, mFilteringSuffix);

        // Set filter settings
        setFiltering(Constants.FILE_TYPE_PDF, shouldFilterPdf);
        setFiltering(Constants.FILE_TYPE_DOC, shouldFilterOffice);
        setFiltering(Constants.FILE_TYPE_IMAGE, shouldFilterImage);
    }

    private void setFiltering(int fileType, boolean shouldFilter) {
        switch (fileType) {
            case Constants.FILE_TYPE_PDF:
                mFilterState.setShouldFilterPdf(shouldFilter);
                break;
            case Constants.FILE_TYPE_DOC:
                mFilterState.setShouldFilterDoc(shouldFilter);
                break;
            case Constants.FILE_TYPE_IMAGE:
                mFilterState.setShouldFilterImage(shouldFilter);
                break;
        }
    }

    @NonNull
    @WorkerThread
    private List<FileInfo> performFiltering(@NonNull List<FileInfo> originalFiles,
                                            @Nullable CharSequence constraint,
                                            @NonNull ObservableEmitter<List<FileInfo>> emitter) {
        // First pass to filter out hidden files
        ArrayList<FileInfo> newValues = new ArrayList<>();
        final String prefixString;
        if (constraint == null || constraint.length() == 0) {
            prefixString = null;
        } else {
            prefixString = constraint.toString().toLowerCase();
        }
        for (FileInfo item : originalFiles) {
            if (isCancelled(emitter)) {
                return Collections.emptyList();
            }
            if (prefixString == null) {
                if (!item.isHidden()) {
                    newValues.add(item);
                }
            } else if (item.getFileName().toLowerCase().contains(prefixString)) {
                // if searching for a constraint then doesn't matter if it is hidden or not
                newValues.add(item);
            }
        }
        // Now filter file types
        if (mFilterState.shouldFilter()) {
            ArrayList<FileInfo> filteredValues = new ArrayList<>();
            for (FileInfo item : newValues) {
                if (isCancelled(emitter)) {
                    return Collections.emptyList();
                }
                String fileName = item.getFileName();
                if (item.isDirectory()) { // keep directories
                    filteredValues.add(item);
                } else if (mFilterState.shouldFilterPdf() && FilenameUtils.isExtension(
                    fileName.toLowerCase(), mPDFExtensions)) {
                    filteredValues.add(item);
                } else if (mFilterState.shouldFilterDoc() && FilenameUtils.isExtension(
                    fileName.toLowerCase(), mDocExtensions)) {
                    filteredValues.add(item);
                } else if (mFilterState.shouldFilterImage() && FilenameUtils.isExtension(
                    fileName.toLowerCase(), mImageExtensions)) {
                    filteredValues.add(item);
                }
            }
            return filteredValues;
        }
        return newValues;
    }

    Observable<List<FileInfo>> from(@NonNull final List<FileInfo> items) {
        return Observable.create(new ObservableOnSubscribe<List<FileInfo>>() {
            @Override
            public void subscribe(ObservableEmitter<List<FileInfo>> emitter) throws Exception {
                MiscUtils.throwIfOnMainThread();
                emitter.onNext(performFiltering(items, mFilterState.mSearchQuery, emitter));
                emitter.onComplete();
            }
        });
    }

    private boolean isCancelled(@NonNull ObservableEmitter<List<FileInfo>> emitter) {
        return emitter.isDisposed();
    }

    interface OnFilterStateChangedListener {
        void onFilterStateChanged(boolean isFilteringPdf, boolean isFilteringDoc,
                                  boolean isFilteringImage, boolean isFiltering);
    }

    private class FilterState {
        private boolean mShouldFilterPdf = false;
        private boolean mShouldFilterDoc = false;
        private boolean mShouldFilterImage = false;
        // False if mShouldFilterPdf, mShouldFilterDoc, and mShouldFilterImage
        // are all false
        private boolean mShouldFilter = false;
        @NonNull
        private String mSearchQuery = "";

        FilterState() {
        }

        boolean shouldFilterPdf() {
            return mShouldFilterPdf;
        }

        boolean shouldFilterDoc() {
            return mShouldFilterDoc;
        }

        boolean shouldFilterImage() {
            return mShouldFilterImage;
        }

        boolean shouldFilter() {
            return mShouldFilter;
        }

        void setShouldFilterPdf(boolean shouldFilterPdf) {
            updateState(shouldFilterPdf, mShouldFilterDoc, mShouldFilterImage);
        }

        void stopFiltering() {
            updateState(false, false, false);
        }

        void setShouldFilterDoc(boolean shouldFilterDoc) {
            updateState(mShouldFilterPdf, shouldFilterDoc, mShouldFilterImage);
        }

        void setShouldFilterImage(boolean shouldFilterImage) {
            updateState(mShouldFilterPdf, mShouldFilterDoc, shouldFilterImage);
        }

        private void updateState(boolean shouldFilterPdf, boolean shouldFilterDoc,
                                 boolean shouldFilterImage) {
            mShouldFilter = shouldFilterPdf || shouldFilterDoc || shouldFilterImage;
            mShouldFilterPdf = shouldFilterPdf;
            mShouldFilterDoc = shouldFilterDoc;
            mShouldFilterImage = shouldFilterImage;
            // Notify listener of filter state changes
            if (mListener != null) {
                mListener.onFilterStateChanged(mShouldFilterPdf, mShouldFilterDoc,
                    mShouldFilterImage, mShouldFilter);
            }
        }
    }
}
