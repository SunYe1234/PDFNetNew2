package com.pdftron.demo.browser;

import android.content.Context;
import android.support.annotation.MainThread;
import android.support.annotation.Nullable;

import com.pdftron.pdf.utils.Logger;
import com.pdftron.demo.utils.MiscUtils;
import com.pdftron.pdf.model.FileInfo;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

@SuppressWarnings("RedundantThrows")
public class FilesRepository {
    private static final String TAG = FilesRepository.class.toString();

    // Data sources, either recursively fetched from disk, from cache, or from memory
    private final RecursiveFetchedFiles mDataSource;
    private final CachedFiles mCachedFiles = new CachedFiles();
    private final GroupedFiles mGroupedFiles = new GroupedFiles();

    private boolean mShouldRefetchFiles = true;
    private boolean mShouldFlattenFiles = false;
    private boolean mDataHasChanged = false;

    @Nullable
    private UiUpdateListener mUiUpdateListener;

    @Nullable
    private Disposable mFetchSubscription = null;
    @Nullable
    private Disposable mUpdateSubscription = null;

    public FilesRepository(@Nullable Context context) {
        mDataSource = new RecursiveFetchedFiles(context);
    }

    void setUiUpdateListener(UiUpdateListener listener) {
        mUiUpdateListener = listener;
    }

    void setShouldFlattenFiles(boolean b) {
        MiscUtils.throwIfNotOnMainThread();
        mShouldFlattenFiles = b;
    }

    void setShouldRefetchFiles(boolean b) {
        MiscUtils.throwIfNotOnMainThread();
        mShouldRefetchFiles = b;
    }

    @MainThread
    Disposable observeData(Function<List<FileInfo>, Observable<List<FileInfo>>> filter,
                           Function<List<FileInfo>, Observable<List<FileInfo>>> sorter,
                           DisposableObserver<List<FileInfo>> observer,
                           Consumer<Disposable> onSubscribe) {

        MiscUtils.throwIfNotOnMainThread();

        // Dispose the currently running data fetch
        if (mFetchSubscription != null) {
            mFetchSubscription.dispose();
            Logger.INSTANCE.LogD(TAG, "Refetch FetchSubscription disposed");
            // Clear old ui so we can replace it with the updated one
            clearUi();
        }
        // Also stop all data updates
        if (mUpdateSubscription != null) {
            mUpdateSubscription.dispose();
        }

        mFetchSubscription = getFiles(filter, sorter, onSubscribe)
            .subscribeWith(observer);
        Logger.INSTANCE.LogD(TAG, "FetchSubscription added");

        return mFetchSubscription;
    }

    private Observable<List<FileInfo>> getFiles(Function<List<FileInfo>, Observable<List<FileInfo>>> filter,
                                                Function<List<FileInfo>, Observable<List<FileInfo>>> sorter,
                                                Consumer<Disposable> onSubscribe) {
        if (mShouldFlattenFiles) {
            return flattenStream(getGroupedDataSource())
                .concatMap(filter)
                .concatMap(sorter)
                .doOnSubscribe(onSubscribe)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(new Consumer<List<FileInfo>>() {
                    @Override
                    public void accept(List<FileInfo> fileInfos) throws Exception {
                        // Send files to the UI
                        setFilesToUi(fileInfos);
                    }
                });
        } else {
            return getGroupedDataSource()
                .concatMap(filter)
                .concatMap(sorter)
                .doOnSubscribe(onSubscribe)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(new Consumer<List<FileInfo>>() {
                    @Override
                    public void accept(List<FileInfo> fileInfos) throws Exception {
                        // Send files to the UI
                        addGroupFilesToUi(fileInfos);
                    }
                });
        }
    }

    // Used for grid layout mode. In this mode, we don't have headers so just emit the entire list of files.
    private Observable<List<FileInfo>> flattenStream(final Observable<List<FileInfo>> dataSource) {
        return dataSource.toList()
            .observeOn(Schedulers.io())
            .map(new Function<List<List<FileInfo>>, List<FileInfo>>() {
                @Override
                public List<FileInfo> apply(List<List<FileInfo>> groups) throws Exception {
                    MiscUtils.throwIfOnMainThread();
                    List<FileInfo> completeList = new ArrayList<>();
                    for (List<FileInfo> group : groups) {
                        completeList.addAll(group);
                    }
                    return completeList;
                }
            })
            .observeOn(AndroidSchedulers.mainThread())
            .toObservable();
    }

    private Observable<List<FileInfo>> getGroupedDataSource() {
        MiscUtils.throwIfNotOnMainThread();

        if (mGroupedFiles.hasFiles() && !mShouldRefetchFiles) {
            // Use files we have in memory if they exist
            Logger.INSTANCE.LogD(TAG, "Using memory files");
            return mGroupedFiles.fromIterable();
        } else if (!mShouldRefetchFiles && mCachedFiles.isValid() && mCachedFiles.hasCache().get()) {
            // Use files in cache if they exist
            Logger.INSTANCE.LogD(TAG, "Using cached files");
            return mCachedFiles.fromCache();
        } else { // Otherwise, no choice but to get search for files on disk
            Logger.INSTANCE.LogD(TAG, "Recursively searching for files");
            return fromRecursiveSearch();
        }
    }

    private Observable<List<FileInfo>> fromRecursiveSearch() {
        return mDataSource.getFiles()
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe(new Consumer<Disposable>() {
                @Override
                public void accept(Disposable disposable) throws Exception {
                    // Clear the memory cached files before doing a disk fetch
                    mGroupedFiles.clearGroupedData();
                }
            })
            .filter(new Predicate<List<FileInfo>>() {
                @Override
                public boolean test(List<FileInfo> fileInfos) throws Exception {
                    return mGroupedFiles.addGroupedData(fileInfos);
                }
            })
            .doOnComplete(new Action() {
                @Override
                public void run() throws Exception {
                    mShouldRefetchFiles = false;
                    mCachedFiles.cacheFiles(mGroupedFiles);
                }
            });
    }

    // Clear all the data, and on-going processing being done by this object
    void releaseMemory() {
        if (mFetchSubscription != null) {
            mFetchSubscription.dispose();
        }

        if (mUpdateSubscription != null) {
            mUpdateSubscription.dispose();
        }
        mGroupedFiles.releaseMemory();
    }

    void dispose() {
        if (mFetchSubscription != null) {
            mFetchSubscription.dispose();
        }

        if (mUpdateSubscription != null) {
            mUpdateSubscription.dispose();
        }
    }

    /**
     * Helper methods to update the UI when data changes
     */

    private void addGroupFilesToUi(List<FileInfo> fileInfos) {
        if (mUiUpdateListener != null) {
            mUiUpdateListener.addGroupFilesToUi(fileInfos);
        }
    }

    private void setFilesToUi(List<FileInfo> fileInfos) {
        if (mUiUpdateListener != null) {
            mUiUpdateListener.setFilesToUi(fileInfos);
        }
    }

    private void clearUi() {
        if (mUiUpdateListener != null) {
            mUiUpdateListener.clearUi();
        }
    }

    void deleteFile(FileInfo fileInfo) {
        MiscUtils.throwIfNotOnMainThread();
        mDataHasChanged = true;
        mGroupedFiles.deleteFile(fileInfo);
    }

    void addFile(FileInfo fileInfo) {
        MiscUtils.throwIfNotOnMainThread();
        mDataHasChanged = true;
        mGroupedFiles.addFile(fileInfo);
    }

    /**
     * Callback used to update the list adapter.
     */
    interface UiUpdateListener {
        void addGroupFilesToUi(List<FileInfo> fileInfos);

        void setFilesToUi(List<FileInfo> fileInfos);

        void clearUi();
    }
}
