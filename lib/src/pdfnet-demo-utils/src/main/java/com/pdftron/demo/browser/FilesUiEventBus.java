package com.pdftron.demo.browser;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.OnLifecycleEvent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.pdftron.pdf.model.FileInfo;

import java.util.Comparator;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

/**
 * Event bus for all {@link FilesComponent}
 */
public class FilesUiEventBus implements LifecycleObserver {

    private final CompositeDisposable mDisposables = new CompositeDisposable();
    @Nullable
    private Subject<Boolean> mRefreshDataObservable;
    @Nullable
    private Subject<FilesComponent.UserFilterEvent> mFilterDataObservable;
    @Nullable
    private Subject<String> mSearchObservable;
    @Nullable
    private Subject<Comparator<FileInfo>> mSortDataObservable;
    @Nullable
    private Subject<FilesComponent.UiUpdateEvent> mRecyclerViewEvents;

    public FilesUiEventBus(@NonNull LifecycleOwner lifecycleOwner) {
        lifecycleOwner.getLifecycle().addObserver(this);

        // Init observables
        mRefreshDataObservable = PublishSubject.<Boolean>create().toSerialized();
        mFilterDataObservable = PublishSubject.<FilesComponent.UserFilterEvent>create().toSerialized();
        mSearchObservable = PublishSubject.<String>create().toSerialized();
        mSortDataObservable = PublishSubject.<Comparator<FileInfo>>create().toSerialized();
        mRecyclerViewEvents = PublishSubject.<FilesComponent.UiUpdateEvent>create().toSerialized();
    }

    @Nullable
    Observable<Boolean> getRefreshEventObservable() {
        return mRefreshDataObservable;
    }

    @Nullable
    Observable<FilesComponent.UserFilterEvent> getFilterEventObservable() {
        return mFilterDataObservable;
    }

    @Nullable
    Observable<String> getSearchQueryObservable() {
        return mSearchObservable;
    }

    @Nullable
    Observable<Comparator<FileInfo>> getSortEventObservable() {
        return mSortDataObservable;
    }

    void emitFileLoadEvent(FilesComponent.UiUpdateEvent event) {
        if (mRecyclerViewEvents != null) {
            mRecyclerViewEvents.onNext(event);
        }
    }

    public void observeRecyclerViewUpdates(
        DisposableObserver<FilesComponent.UiUpdateEvent> observer) {
        if (mRecyclerViewEvents != null) {
            mDisposables.add(
                mRecyclerViewEvents.observeOn(AndroidSchedulers.mainThread())
                    .subscribeWith(observer)

            );
        }
    }

    public void emitRefetchEvent() {
        if (mRefreshDataObservable != null) {
            mRefreshDataObservable.onNext(Boolean.TRUE);
        }
    }

    public void emitRefreshEvent() {
        if (mRefreshDataObservable != null) {
            mRefreshDataObservable.onNext(Boolean.FALSE);
        }
    }

    public void emitFilterEvent(@NonNull FilesComponent.UserFilterEvent event) {
        if (mFilterDataObservable != null) {
            mFilterDataObservable.onNext(event);
        }
    }

    public void emitStringSearchQuery(@NonNull String query) {
        if (mSearchObservable != null) {
            mSearchObservable.onNext(query);
        }
    }

    public void emitSortEvent(@NonNull Comparator<FileInfo> event) {
        if (mSortDataObservable != null) {
            mSortDataObservable.onNext(event);
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    void cleanUp() {
        mDisposables.clear();
        mRefreshDataObservable = null;
        mFilterDataObservable = null;
        mSearchObservable = null;
        mSortDataObservable = null;
        mRecyclerViewEvents = null;
    }
}
