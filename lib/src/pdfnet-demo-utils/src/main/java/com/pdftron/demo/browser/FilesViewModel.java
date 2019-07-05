package com.pdftron.demo.browser;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

import com.pdftron.demo.utils.MiscUtils;
import com.pdftron.pdf.model.FileInfo;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

@SuppressWarnings("RedundantThrows")
public class FilesViewModel extends ViewModel implements FilesRepository.UiUpdateListener {
    private static final String TAG = FilesViewModel.class.toString();
    private final CompositeDisposable mDisposables = new CompositeDisposable();
    private final Subject<FileTuple> mDataChanges = PublishSubject.<FileTuple>create().toSerialized();  // observable that emits data change events, observed by view and emitted by presenter

    // Keep reference to added and delete files to update the data later
    private final FilesRepository mDao;

    private FilesViewModel(@NonNull FilesRepository repo) {
        mDao = repo;
        mDao.setUiUpdateListener(this);
    }

    /**
     * Factory to create a new {@link FilesViewModel}
     */
    @NonNull
    public static FilesViewModel from(@NonNull Fragment fragment,
                                      @NonNull FilesRepository dao) {
        return ViewModelProviders.of(fragment, new ListFilesViewModelFactory(dao))
            .get(FilesViewModel.class);
    }

    void dispose() {
        mDisposables.clear();
        mDao.dispose();
    }

    void setShouldFlattenFiles(boolean b) {
        mDao.setShouldFlattenFiles(b);
    }

    void invalidateData() {
        mDao.setShouldRefetchFiles(true);
    }

    @MainThread
    Disposable observeData(
        Function<List<FileInfo>, Observable<List<FileInfo>>> filter,
        Function<List<FileInfo>, Observable<List<FileInfo>>> sorter,
        DisposableObserver<List<FileInfo>> observer,
        Consumer<Disposable> onSubscribe) {
        return mDao.observeData(filter, sorter, observer, onSubscribe);
    }

    void subscribeToDataChanges(@NonNull DisposableObserver<FileTuple> observer) {
        mDisposables.add(
            mDataChanges.observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(observer)
        );
    }

    @MainThread
    void deleteFile(FileInfo fileInfo) {
        mDao.deleteFile(fileInfo);
    }

    @MainThread
    void addFile(FileInfo fileInfo) {
        mDao.addFile(fileInfo);
    }

    @Override
    public void addGroupFilesToUi(List<FileInfo> files) {
        MiscUtils.throwIfNotOnMainThread();
        // Add files to the UI
        FileTuple event = FileTuple.fromAddGroupEvent(files);
        mDataChanges.onNext(event);
    }

    @Override
    public void setFilesToUi(List<FileInfo> files) {
        MiscUtils.throwIfNotOnMainThread();
        // Add files to the UI
        FileTuple event = FileTuple.fromSetEvent(files);
        mDataChanges.onNext(event);
    }

    @Override
    public void clearUi() {
        MiscUtils.throwIfNotOnMainThread();
        // Clear the files in the UI
        FileTuple event = FileTuple.fromClearEvent();
        mDataChanges.onNext(event);
    }

    private void addFilesToUi(List<FileInfo> files) {
        MiscUtils.throwIfNotOnMainThread();
        // Add files to the UI
        FileTuple event = FileTuple.fromAddFilesEvent(files);
        mDataChanges.onNext(event);
    }

    private void removeFilesFromUi(List<FileInfo> files) {
        MiscUtils.throwIfNotOnMainThread();
        // Remove files from UI
        FileTuple event = FileTuple.fromRemoveFilesEvent(files);
        mDataChanges.onNext(event);
    }

    public enum FetchEvent {
        ADD_GROUPED_FILES,          // Add files to the UI incrementally
        CLEAR_ALL_FILES,            // Clear all files from the UI
        SET_ALL_FILES,              // Set all files to the UI
        UPDATE_ADD_FILES,           // Add files in the UI
        UPDATE_REMOVE_FILES            // Remove files in the UI
    }

    public static class FileTuple {
        public final FetchEvent event;
        public final List<FileInfo> files;

        FileTuple(FetchEvent event, List<FileInfo> files) {
            this.event = event;
            this.files = files;
        }

        static FileTuple fromAddGroupEvent(List<FileInfo> files) {
            return new FileTuple(FetchEvent.ADD_GROUPED_FILES, files);
        }

        static FileTuple fromAddFilesEvent(List<FileInfo> files) {
            return new FileTuple(FetchEvent.UPDATE_ADD_FILES, files);
        }

        static FileTuple fromRemoveFilesEvent(List<FileInfo> files) {
            return new FileTuple(FetchEvent.UPDATE_REMOVE_FILES, files);
        }

        static FileTuple fromSetEvent(List<FileInfo> files) {
            return new FileTuple(FetchEvent.SET_ALL_FILES, files);
        }

        static FileTuple fromClearEvent() {
            return new FileTuple(FetchEvent.CLEAR_ALL_FILES, new ArrayList<FileInfo>());
        }
    }

    static class ListFilesViewModelFactory implements ViewModelProvider.Factory {
        private FilesRepository mDao;

        ListFilesViewModelFactory(FilesRepository dao) {
            mDao = dao;
        }

        @SuppressWarnings("unchecked")
        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(FilesViewModel.class)) {
                return (T) new FilesViewModel(mDao);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}
