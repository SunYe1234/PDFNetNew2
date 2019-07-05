package com.pdftron.demo.navigation.adapter;

import android.support.annotation.Nullable;
import com.google.gson.reflect.TypeToken;
import com.pdftron.demo.model.FileHeader;
import com.pdftron.demo.utils.CacheUtils;
import com.pdftron.demo.utils.FileInfoComparator;
import com.pdftron.pdf.utils.Logger;
import com.pdftron.pdf.model.BaseFileInfo;
import com.pdftron.pdf.model.FileInfo;
import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;

import java.io.File;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;

@SuppressWarnings("RedundantThrows")
class AllFilesAdapterHelper {
    private static final String TAG = AllFilesAdapterHelper.class.getName();

    // This map contains the number of files for under each header
    private final Object mHeaderCountLock = new Object();
    private final Object mCachedHeaderLock = new Object();
    private final HashMap<String, Integer> mHeaderCount = new HashMap<>();      // header path, number of files under header
    private final HashMap<String, FileHeader> mCachedHeaders = new HashMap<>(); // header path, header
    @Nullable private final NotifyAdapterListener mAdapterListener;

    AllFilesAdapterHelper(@Nullable NotifyAdapterListener mAdapterListener) {
        this.mAdapterListener = mAdapterListener;
        loadHeaders();
    }

    //
    // Public methods for updating helper
    //

    void clearFiles(List<FileInfo> myFiles) {
        int oldSize = myFiles.size();
        myFiles.clear();
        clearHeaders();
        notifyItemRangeRemoved(0, oldSize);
    }

    // Adds an item to the myFiles, if we are in grid mode then just add it normally.
    // Otherwise we are in list mode and will need to deal with headers
    final void addFile(List<FileInfo> myFiles, FileInfo fileInfo, Comparator<FileInfo> sortMode,
            int mSpanCount) {
        if (mSpanCount > 0) {
            addItemWithoutHeader(myFiles, fileInfo, sortMode, mSpanCount);
        } else {
            addItemWithHeader(myFiles, fileInfo, sortMode, mSpanCount);
        }
    }

    // Add new files to adapter, assumes this list of files is new and does not exist in myFiles
    final void addGroupedFiles(List<FileInfo> myFiles, List<FileInfo> newFiles, int spanCount) {
        if (myFiles != null && !newFiles.isEmpty()) {
            if (spanCount > 0) { // grid mode, so just add the file since we have no headers
                int idx = myFiles.size();
                myFiles.addAll(newFiles);
                notifyItemRangeInserted(idx, newFiles.size());
            } else { // otherwise add the header and then the files
                int position = myFiles.size();
                FileHeader header = new FileHeader(BaseFileInfo.FILE_TYPE_FOLDER,
                        new File(newFiles.get(0).getParentDirectoryPath()));
                header.setHeader(true);

                // Add header to view if there is none
                if (!containsFileHeader(header)) {
                    synchronized (mCachedHeaderLock) {
                        String headerKey = header.getAbsolutePath();
                        if (mCachedHeaders.containsKey(header.getAbsolutePath())) {
                            FileHeader cachedHeader = mCachedHeaders.get(headerKey);
                            boolean isCollapsed = cachedHeader != null && cachedHeader.getCollapsed();

                            if (isCollapsed) {
                                // Is collapsed, so only add the header
                                header.setCollapsed(true);
                                saveHeaders(header, newFiles.size());
                                myFiles.add(header);
                                notifyItemInserted(position);
                                // Then also save the files for later
                                addChildrenToMap(header.getAbsolutePath(), newFiles);
                            } else {
                                // Expanded, so just add everything normally
                                // Save the header to reference later
                                saveHeaders(header, newFiles.size());
                                myFiles.add(header);
                                myFiles.addAll(newFiles);
                                notifyItemRangeInserted(position, newFiles.size() + 1);
                            }
                        } else {
                            // Save the header to reference later
                            saveHeaders(header, newFiles.size());
                            myFiles.add(header);
                            myFiles.addAll(newFiles);
                            notifyItemRangeInserted(position, newFiles.size() + 1);
                        }
                    }
                } else {
                    Logger.INSTANCE.LogE(TAG, "Found duplicated headers. This should not happen!");
                }
            }
        }
    }

    public final void deleteFile(List<FileInfo> myFiles, FileInfo fileInfo, int spanCount) {
        if (spanCount > 0) {
            deleteItemWithoutHeader(myFiles, fileInfo);
        } else {
            deleteItemWithHeader(myFiles, fileInfo);
        }
    }

    //
    // Private helper methods
    //

    private void addItemWithoutHeader(List<FileInfo> myFiles, FileInfo fileInfo,
            Comparator<FileInfo> sortMode, int spanCount) {
        // Look through all the files and find the spot to add it
        for (int i = 0, count = myFiles.size(); i < count; ++i) {
            FileInfo f = myFiles.get(i);
            if (compare(f, fileInfo, sortMode, spanCount) > 0) {
                // Only add the file to the list
                myFiles.add(i, fileInfo);
                notifyItemInserted(i);
                break;
            }
        }
    }

    private void addItemWithHeader(List<FileInfo> myFiles, FileInfo fileInfo,
            Comparator<FileInfo> sortMode, int spanCount) {
        // add the new file to the right position based on sort method
        FileHeader newHeader = new FileHeader(BaseFileInfo.FILE_TYPE_FOLDER,
                new File(fileInfo.getParentDirectoryPath()));
        newHeader.setHeader(true);

        // If there's no header, that means we also currently also don't have a file under that folder
        if (!containsFileHeader(newHeader)) {
            int i = 0; // if we add a header, start at this index when we add the new file
            int size = myFiles.size();
            for (; i < size; ++i) {
                FileInfo f = myFiles.get(i);
                if (f.isHeader() && compare(f, newHeader, sortMode, spanCount) > 0) {
                    break;
                }
            }
            // Add the header then the file
            myFiles.add(i, newHeader);
            myFiles.add(i + 1, fileInfo);
            notifyItemRangeInserted(i, 2);
        } else { // we have a header for this file
            int idx = 0;
            int size = myFiles.size();
            for (; idx < size; ++idx) {
                FileInfo f = myFiles.get(idx);
                if (f.isHeader() && f.equals(newHeader)) {
                    ++idx;
                    break;
                }
            }
            for (; idx < size; ++idx) {
                FileInfo f = myFiles.get(idx);
                if (f.isHeader()) {
                    break;
                }

                int compare = compare(f, fileInfo, sortMode, spanCount);
                if (compare > 0) {
                    break;
                }
            }
            myFiles.add(idx, fileInfo);
            notifyItemRangeInserted(idx, 1);
        }
        // Save a new header, or increment the header count if we already have it in memory
        saveHeader(newHeader);
    }

    private void deleteItemWithHeader(List<FileInfo> myFiles, FileInfo fileInfo) {
        for (int i = 0, count = myFiles.size(); i < count; ++i) {
            FileInfo f = myFiles.get(i);
            if (f.getAbsolutePath().equals(fileInfo.getAbsolutePath())) {
                // Remove header if its the last item in the list
                FileHeader header = new FileHeader(BaseFileInfo.FILE_TYPE_FOLDER,
                        new File(fileInfo.getParentDirectoryPath()));
                header.setHeader(true);
                if (isLastFileUnderHeader(header)) {
                    if (i > 0) {
                        Logger.INSTANCE.LogD(TAG,
                                "Deleted item and its header in list mode at index = " + i);
                        myFiles.remove(i); // item index
                        myFiles.remove(
                                i - 1); // header index, assumes header is always populated in list mode
                        notifyItemRangeRemoved(i - 1, 2);
                    } else { // This shouldn't happen but just in case. Fallback to just remove the item
                        Logger.INSTANCE.LogE(TAG,
                                "Tried to delete item header in list mode at index = " + i);
                        myFiles.remove(i);
                        notifyItemRemoved(i);
                    }
                } else {
                    Logger.INSTANCE.LogD(TAG, "Deleted item only in list mode at index = " + i);
                    myFiles.remove(i);
                    notifyItemRemoved(i);
                }
                removeFileHeader(header);
                break;
            }
        }
    }

    private void deleteItemWithoutHeader(List<FileInfo> myFiles, FileInfo fileInfo) {
        for (int i = 0, count = myFiles.size(); i < count; ++i) {
            FileInfo f = myFiles.get(i);
            if (f.getAbsolutePath().equals(fileInfo.getAbsolutePath())) {
                Logger.INSTANCE.LogD(TAG, "Deleted item only in grid mode at index = " + i);
                myFiles.remove(i);
                notifyItemRemoved(i);
                break;
            }
        }
    }

    /**
     * Header related methods
     */

    // adds a new file header, if one already exists then we increment the count
    private void saveHeader(FileHeader header) {
        Integer oldVal = getHeader(header);
        int newVal;
        if (oldVal == null) {
            newVal = 1;
        } else {
            newVal = oldVal + 1;
        }
        putHeader(header, newVal);
    }

    // adds a new file header for multiple files at once, used by addGroupedFiles
    private void saveHeaders(FileHeader header, Integer numberOfFiles) {
        putHeader(header, numberOfFiles);
    }

    // Decrements the file header, if the count is 1 then just remove it
    private void removeFileHeader(FileHeader header) {
        Integer oldVal = getHeader(header);
        if (oldVal == null) {
            return;
        }
        if (oldVal == 1) {
            removeHeader(header);
        } else {
            putHeader(header, oldVal - 1);
        }
    }

    private boolean isLastFileUnderHeader(FileHeader header) {
        Integer numFilesWithHeader = getHeader(header);
        if (numFilesWithHeader == null) {
            return false;
        } else {
            return numFilesWithHeader == 1;
        }
    }

    private void clearHeaders() {
        synchronized (mHeaderCountLock) {
            mHeaderCount.clear();
        }
    }

    private int compare(FileInfo file1, FileInfo file2, Comparator<FileInfo> sortMode,
            int spanCount) {
        if (sortMode != null) {
            return sortMode.compare(file1, file2);
        }
        if (spanCount > 0) {
            return FileInfoComparator.fileNameOrder().compare(file1, file2);
        }
        return FileInfoComparator.absolutePathOrder().compare(file1, file2);
    }

    private void putHeader(FileHeader header, int idx) {
        synchronized (mHeaderCountLock) {
            mHeaderCount.put(header.getAbsolutePath(), idx);
        }
        synchronized (mCachedHeaders) {
            mCachedHeaders.put(header.getAbsolutePath(), header);
        }
    }

    private void removeHeader(FileHeader header) {
        synchronized (mHeaderCountLock) {
            mHeaderCount.remove(header.getAbsolutePath());
        }
    }

    private Integer getHeader(FileHeader header) {
        synchronized (mHeaderCountLock) {
            return mHeaderCount.get(header.getAbsolutePath());
        }
    }

    // check if we have already stored the header
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean containsFileHeader(FileHeader header) {
        synchronized (mHeaderCountLock) {
            return mHeaderCount.containsKey(header.getAbsolutePath());
        }
    }

    /**
     * Callbacks for adapter updates
     */

    private void notifyItemInserted(int idx) {
        if (mAdapterListener != null) {
            mAdapterListener.notifyItemInserted(idx);
        }
    }

    private void notifyItemRangeInserted(int idx, int size) {
        if (mAdapterListener != null) {
            mAdapterListener.notifyItemRangeInserted(idx, size);
        }
    }

    private void notifyItemRemoved(int idx) {
        if (mAdapterListener != null) {
            mAdapterListener.notifyItemRemoved(idx);
        }
    }

    private void notifyItemRangeRemoved(int idx, int size) {
        if (mAdapterListener != null) {
            mAdapterListener.notifyItemRangeRemoved(idx, size);
        }
    }

    private void addChildrenToMap(String header, List<FileInfo> children) {
        if (mAdapterListener != null) {
            mAdapterListener.addChildrenToMap(header, children);
        }
    }

    void saveHeaders() {
        Completable.fromAction(new Action() {
            @Override
            public void run() throws Exception {
                synchronized (mHeaderCountLock) {
                    CacheUtils.writeObjectFile(CacheUtils.CACHE_HEADER_LIST_OBJECT, mCachedHeaders);
                }
            }
        }).subscribe(new CompletableObserver() {
            @Override public void onSubscribe(Disposable d) {
                Logger.INSTANCE.LogD(TAG, "Saving headers");
            }

            @Override public void onComplete() {
                Logger.INSTANCE.LogD(TAG, "Finished saving headers");
            }

            @Override public void onError(Throwable e) {
                Logger.INSTANCE.LogE(TAG, "Error saving headers");
            }
        });
    }

    private void loadHeaders() {
        Single.fromCallable(new Callable<HashMap<String, FileHeader>>() {
            @Override public HashMap<String, FileHeader> call() throws Exception {
                return CacheUtils.readObjectFile(CacheUtils.CACHE_HEADER_LIST_OBJECT,
                        new TypeToken<HashMap<String, FileHeader>>() {
                        }.getType());
            }
        }).subscribe(new SingleObserver<HashMap<String, FileHeader>>() {
            @Override public void onSubscribe(Disposable d) {
                Logger.INSTANCE.LogD(TAG, "loading headers");
            }

            @Override public void onSuccess(HashMap<String, FileHeader> cachedFileHeaders) {
                Logger.INSTANCE.LogD(TAG, "Finished loading headers");
                synchronized (mCachedHeaderLock) {
                    mCachedHeaders.clear();
                    mCachedHeaders.putAll(cachedFileHeaders);
                }
            }

            @Override public void onError(Throwable e) {
                Logger.INSTANCE.LogE(TAG, "Error loading headers: " + e);
                synchronized (mCachedHeaderLock) {
                    mCachedHeaders.clear();
                }
            }
        });
    }

    /**
     * Listener for file list changes in the adapter.
     */
    interface NotifyAdapterListener {

        void notifyItemInserted(int idx);

        void notifyItemRangeInserted(int idx, int size);

        void notifyItemRemoved(int idx);

        void notifyItemRangeRemoved(int idx, int size);

        void addChildrenToMap(String header, List<FileInfo> children);
    }
}
