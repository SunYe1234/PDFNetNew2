package com.pdftron.demo.browser;

import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;
import com.pdftron.demo.utils.MiscUtils;
import com.pdftron.pdf.model.FileInfo;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @hide
 */
@SuppressWarnings("RedundantThrows")
class FileSorter {

    private Comparator<FileInfo> mSortMode;

    FileSorter(Comparator<FileInfo> initSortMode) {
        mSortMode = initSortMode;
    }

    void setSortMode(@NonNull Comparator<FileInfo> sortMode) {
        mSortMode = sortMode;
    }

    Observable<List<FileInfo>> from(@NonNull final List<FileInfo> items) {
        return Observable.create(new ObservableOnSubscribe<List<FileInfo>>() {
            @Override
            public void subscribe(ObservableEmitter<List<FileInfo>> emitter) throws Exception {
                MiscUtils.throwIfOnMainThread();
                emitter.onNext(performSorting(items, emitter));
                emitter.onComplete();
            }
        });
    }

    @WorkerThread
    private List<FileInfo> performSorting(
        @NonNull List<FileInfo> files,
        @NonNull ObservableEmitter<List<FileInfo>> emitter) {

        ArrayList<FileInfo> list = new ArrayList<>(files);

        if (isCancelled(emitter)) {
            return Collections.emptyList();
        }

        MiscUtils.sortFileInfoList(list, mSortMode);

        if (isCancelled(emitter)) {
            return Collections.emptyList();
        }

        return list;
    }

    private boolean isCancelled(@NonNull ObservableEmitter<List<FileInfo>> emitter) {
        return emitter.isDisposed();
    }
}
