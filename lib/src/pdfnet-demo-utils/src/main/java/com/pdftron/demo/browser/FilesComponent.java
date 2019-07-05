package com.pdftron.demo.browser;

import android.annotation.SuppressLint;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.pdftron.pdf.utils.Logger;
import com.pdftron.pdf.model.FileInfo;
import com.pdftron.pdf.utils.AnalyticsHandlerAdapter;
import com.pdftron.pdf.utils.PdfViewCtrlSettingsManager;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

@SuppressWarnings({"RedundantThrows"})
@SuppressLint("CheckResult")
public class FilesComponent implements LifecycleObserver {
    private static final String TAG = FilesComponent.class.toString();

    private final FilesViewModel mFilesViewModel;
    private final FileFilter mFilter;
    private final FileSorter mSorter;
    private final Subject<UserUiUpdateEvent> mUserEventObservable;
    private final CompositeDisposable mDisposables = new CompositeDisposable();

    private static final List<String> mEventList = new ArrayList<>(100);

    @SuppressLint("CheckResult")
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public FilesComponent(@Nullable Context context, @NonNull FilesViewModel viewModel,
                          @NonNull final FilesUiEventBus eventBus, @NonNull final Comparator<FileInfo> initSortMode) {

        // Initialize the filter and sorter state from cache
        mFilter = new FileFilter(context, PdfViewCtrlSettingsManager.KEY_PREF_SUFFIX_LOCAL_FILES);
        mSorter = new FileSorter(initSortMode);
        mFilesViewModel = viewModel;

        // Setup the observers
        mUserEventObservable = PublishSubject.<UserUiUpdateEvent>create().toSerialized();
        observeUserEvents(mUserEventObservable, eventBus);
        listenForRefresh(eventBus);
        listenForFilter(eventBus);
        listenForSearch(eventBus);
        listenForSort(eventBus);
    }

    /**
     * Add a file to the view model
     *
     * @param fileInfo of file to add
     */
    public void addFile(@NonNull FileInfo fileInfo) {
        mFilesViewModel.addFile(fileInfo);
    }

    /**
     * Remove file with specified path from the view model
     *
     * @param fileInfo of file to remove
     */
    public void deleteFile(@NonNull FileInfo fileInfo) {
        mFilesViewModel.deleteFile(fileInfo);
    }

    public void subscribe(@NonNull DisposableObserver<FilesViewModel.FileTuple> observer) {
        mFilesViewModel.subscribeToDataChanges(observer);
    }

    public void setGridMode(boolean gridModeEnabled) {
        mFilesViewModel.setShouldFlattenFiles(gridModeEnabled);
    }

    public void enableGridMode() {
        mFilesViewModel.setShouldFlattenFiles(true);
    }

    public void enableListMode() {
        mFilesViewModel.setShouldFlattenFiles(false);
    }

    public void setSortMode(@NonNull Comparator<FileInfo> sortMode) {
        mSorter.setSortMode(sortMode);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    private void initResources() {
        mEventList.add("Initialized FilesComponent");
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    private void freeResources() {
        try {
            mEventList.clear();
        } catch (Exception e) {
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    private void cleanUp() {
        Logger.INSTANCE.LogD(TAG, "Clean up is called");
        mDisposables.clear();
        mFilesViewModel.dispose();
    }

    private void observeUserEvents(@NonNull final Subject<UserUiUpdateEvent> userEventObservable,
                                   @NonNull final FilesUiEventBus eventBus) {
        mDisposables.add(
            userEventObservable
                .observeOn(AndroidSchedulers.mainThread())
                .map(new Function<UserUiUpdateEvent, Disposable>() {
                    @Override
                    public Disposable apply(UserUiUpdateEvent userUiEvents) throws Exception {
                        switch (userUiEvents) {
                            case REFETCH:
                                Logger.INSTANCE.LogD(TAG, "REFETCH RECURSIVELY");
                                return refreshData(eventBus, true);
                            case REFRESH:
                                Logger.INSTANCE.LogD(TAG, "REFRESHING");
                                return refreshData(eventBus, false);
                            case FILTER:
                                Logger.INSTANCE.LogD(TAG, "FILTER");
                                return filterData(eventBus);
                            case SEARCH:
                                Logger.INSTANCE.LogD(TAG, "SEARCH");
                                return searchData(eventBus);
                            case SORT:
                                Logger.INSTANCE.LogD(TAG, "SORT");
                                return sortData(eventBus);
                            default:
                                throw new RuntimeException("UI Event is not handled!");
                        }
                    }
                })
                .subscribe()
        );
    }

    private void listenForRefresh(@NonNull final FilesUiEventBus eventBus) {
        // Observe UI events so we can handle UI events that change the list data
        Observable<Boolean> refreshObservable = eventBus.getRefreshEventObservable();
        if (refreshObservable != null) {
            mDisposables.add(refreshObservable
                // Throttle spammed refresh events
                .throttleLast(300, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean refreshEvents) throws Exception {
                        // If we get true, then send a refetch event, otherwise send an invalidate event
                        if (refreshEvents) {
                            mUserEventObservable.onNext(UserUiUpdateEvent.REFETCH);
                        } else {
                            mUserEventObservable.onNext(UserUiUpdateEvent.REFRESH);
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Logger.INSTANCE.LogE(TAG, "Error when getRefreshEventObservable emitted: " + throwable);
                    }
                })
            );
        } else {
            Logger.INSTANCE.LogE(TAG, "RefreshObservable is null, listenForRefresh called when destroyed.");
        }
    }

    private void listenForFilter(@NonNull final FilesUiEventBus eventBus) {
        Observable<UserFilterEvent> filterObservable = eventBus.getFilterEventObservable();
        if (filterObservable != null) {
            mDisposables.add(filterObservable
                // Update filter state for every event
                .doOnNext(new Consumer<UserFilterEvent>() {
                    @Override
                    public void accept(UserFilterEvent filterUiEvents) throws Exception {
                        mFilter.setSearchQuery("");
                        mFilter.updateFromEvent(filterUiEvents);
                    }
                })
                // However, do not process all events and debounce spammed events
                .debounce(300, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<UserFilterEvent>() {
                    @Override
                    public void accept(UserFilterEvent filterUiEvents) throws Exception {
                        // Clear the search query and set the update state
                        mUserEventObservable.onNext(UserUiUpdateEvent.FILTER);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Logger.INSTANCE.LogE(TAG, "Error when getFilterEventObservable emitted: " + throwable);
                    }
                })
            );
        } else {
            Logger.INSTANCE.LogE(TAG,
                "FilterObservable is null, listenForFilter called when destroyed.");
        }
    }

    private void listenForSearch(@NonNull final FilesUiEventBus eventBus) {
        Observable<String> searchObservable = eventBus.getSearchQueryObservable();
        if (searchObservable != null) {
            mDisposables.add(searchObservable
                // Update filter state for every event
                .doOnNext(new Consumer<String>() {
                    @Override
                    public void accept(String query) throws Exception {
                        // Update the filter state based on search query only
                        mFilter.setSearchQuery(query);
                    }
                })
                // However, do not process all events and debounce spammed events
                .debounce(300, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String query) throws Exception {
                        mUserEventObservable.onNext(UserUiUpdateEvent.SEARCH);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Logger.INSTANCE.LogE(TAG, "Error when getSearchQueryObservable emitted: " + throwable);
                    }
                })
            );
        } else {
            Logger.INSTANCE.LogE(TAG, "SearchObservable is null, listenForSearch called when destroyed.");
        }
    }

    private void listenForSort(@NonNull final FilesUiEventBus eventBus) {
        Observable<Comparator<FileInfo>> sortObservable = eventBus.getSortEventObservable();
        if (sortObservable != null) {
            mDisposables.add(sortObservable
                // Update sort state for every event
                .doOnNext(new Consumer<Comparator<FileInfo>>() {
                    @Override
                    public void accept(Comparator<FileInfo> sortMode) throws Exception {
                        mSorter.setSortMode(sortMode);
                    }
                })
                // However, do not process all events and debounce spammed events
                .debounce(300, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Comparator<FileInfo>>() {
                    @Override
                    public void accept(Comparator<FileInfo> sortMode) throws Exception {
                        mUserEventObservable.onNext(UserUiUpdateEvent.SORT);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Logger.INSTANCE.LogE(TAG, "Error when getSortEventObservable emitted: " + throwable);
                    }
                })
            );
        } else {
            Logger.INSTANCE.LogE(TAG, "SortObservable is null, listenForSort called when destroyed.");
        }
    }

    /**
     * Helpers for building observables
     */
    private Function<List<FileInfo>, Observable<List<FileInfo>>> getFilterFunction() {
        return new Function<List<FileInfo>, Observable<List<FileInfo>>>() {
            @Override
            public Observable<List<FileInfo>> apply(List<FileInfo> fileInfos) {
                return mFilter.from(fileInfos)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread());
            }
        };
    }

    private Function<List<FileInfo>, Observable<List<FileInfo>>> getSortFunction() {
        return new Function<List<FileInfo>, Observable<List<FileInfo>>>() {
            @Override
            public Observable<List<FileInfo>> apply(List<FileInfo> fileInfos) {
                return mSorter.from(fileInfos)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread());
            }
        };
    }

    private Disposable refreshData(@NonNull final FilesUiEventBus eventBus, final boolean invalidateData) {
        if (invalidateData) {
            mFilesViewModel.invalidateData();
        }
        return mFilesViewModel.observeData(getFilterFunction(), getSortFunction(),
            new DisposableObserver<List<FileInfo>>() {
                private boolean isEmpty = true;

                @Override
                public void onNext(List<FileInfo> fileInfos) {
                    // this is handled in the view model
                    if (isEmpty && !fileInfos.isEmpty()) {
                        isEmpty = false;
                    }
                }

                @Override
                public void onError(Throwable throwable) {
                    Logger.INSTANCE.LogE(TAG, "Error loading local files: " + throwable);
                    eventBus.emitFileLoadEvent(UiUpdateEvent.LOADING_ERRORED);
                }

                @Override
                public void onComplete() {
                    dispose();
                    if (isEmpty) {
                        eventBus.emitFileLoadEvent(UiUpdateEvent.EMPTY_LIST);
                    } else {
                        eventBus.emitFileLoadEvent(UiUpdateEvent.LOADING_FINISHED);
                    }
                    pushEvent(invalidateData ? "Refetch Fin" : "Refresh Fin");
                    sendEventLog();
                }
            }, new Consumer<Disposable>() {
                @Override
                public void accept(Disposable disposable) throws Exception {
                    if (!disposable.isDisposed()) {
                        eventBus.emitFileLoadEvent(UiUpdateEvent.LOADING_STARTED);
                    }
                    pushEvent(invalidateData ? "Refetch Start" : "Refresh Start");
                }
            }
        );
    }

    private Disposable filterData(@NonNull final FilesUiEventBus eventBus) {
        return mFilesViewModel.observeData(getFilterFunction(), getSortFunction(),
            new DisposableObserver<List<FileInfo>>() {
                private boolean isEmpty = true;

                @Override
                public void onNext(List<FileInfo> fileInfos) {
                    // this is handled in the view model
                    if (isEmpty && !fileInfos.isEmpty()) {
                        isEmpty = false;
                    }
                }

                @Override
                public void onError(Throwable throwable) {
                    Logger.INSTANCE.LogE(TAG, "Error filtering files: " + throwable);
                    eventBus.emitFileLoadEvent(UiUpdateEvent.LOADING_ERRORED);
                }

                @Override
                public void onComplete() {
                    dispose();
                    if (isEmpty) {
                        eventBus.emitFileLoadEvent(UiUpdateEvent.FILTER_NO_MATCHES);
                    } else {
                        eventBus.emitFileLoadEvent(UiUpdateEvent.FILTER_FINISHED);
                    }
                    pushEvent("Filter Fin");
                    sendEventLog();
                }
            }, new Consumer<Disposable>() {
                @Override
                public void accept(Disposable disposable) throws Exception {
                    if (!disposable.isDisposed()) {
                        eventBus.emitFileLoadEvent(UiUpdateEvent.FILTER_STARTED);
                        pushEvent("Filter Start");
                    }
                }
            }
        );
    }

    private Disposable searchData(@NonNull final FilesUiEventBus eventBus) {
        return mFilesViewModel.observeData(getFilterFunction(), getSortFunction(),
            new DisposableObserver<List<FileInfo>>() {
                private boolean isEmpty = true;

                @Override
                public void onNext(List<FileInfo> fileInfos) {
                    // this is handled in the view model
                    if (isEmpty && !fileInfos.isEmpty()) {
                        isEmpty = false;
                    }
                }

                @Override
                public void onError(Throwable throwable) {
                    Logger.INSTANCE.LogE(TAG, "Error searching local files: " + throwable);
                    eventBus.emitFileLoadEvent(UiUpdateEvent.LOADING_ERRORED);
                }

                @Override
                public void onComplete() {
                    dispose();
                    if (isEmpty) {
                        eventBus.emitFileLoadEvent(UiUpdateEvent.SEARCH_NO_MATCHES);
                    } else {
                        eventBus.emitFileLoadEvent(UiUpdateEvent.SEARCH_FINISHED);
                    }
                    pushEvent("Search Fin");
                    sendEventLog();
                }
            }, new Consumer<Disposable>() {
                @Override
                public void accept(Disposable disposable) throws Exception {
                    if (!disposable.isDisposed()) {
                        eventBus.emitFileLoadEvent(UiUpdateEvent.SEARCH_STARTED);
                        pushEvent("Search Start");
                    }
                }
            }
        );
    }

    private Disposable sortData(@NonNull final FilesUiEventBus eventBus) {
        return mFilesViewModel.observeData(getFilterFunction(), getSortFunction(),
            new DisposableObserver<List<FileInfo>>() {
                @Override
                public void onNext(List<FileInfo> fileInfos) {
                    // this is handled in the view model
                }

                @Override
                public void onError(Throwable throwable) {
                    Logger.INSTANCE.LogE(TAG, "Error sorting local files: " + throwable);
                    eventBus.emitFileLoadEvent(UiUpdateEvent.LOADING_ERRORED);
                }

                @Override
                public void onComplete() {
                    dispose();
                    eventBus.emitFileLoadEvent(UiUpdateEvent.SORT_FINISHED);
                    pushEvent("Sort Fin");
                    sendEventLog();
                }
            }, new Consumer<Disposable>() {
                @Override
                public void accept(Disposable disposable) throws Exception {
                    if (!disposable.isDisposed()) {
                        eventBus.emitFileLoadEvent(UiUpdateEvent.SORT_STARTED);
                        pushEvent("Sort Start");
                    }
                }
            }
        );
    }

    private void pushEvent(String eventStr) {
        try {
            mEventList.add(eventStr);
        } catch (Exception e) {
        }
    }

    private void sendEventLog() {
        try {
            String value = mEventList.toString();
            AnalyticsHandlerAdapter.getInstance()
                    .setString(AnalyticsHandlerAdapter.CustomKeys.ALL_FILE_BROWSER_EVENTS, value);
        } catch (Exception e) {
        }
    }

    /**
     * Generic user UI events handled internally by FilesComponent
     */
    private enum UserUiUpdateEvent {
        // Refetch data event, invalidates data causing a new recursive search for files
        REFETCH,
        // Refresh UI event, similar to refetch but we do not invalid the data so we grab files from memory
        REFRESH,
        // Filter event
        FILTER,
        // Search event
        SEARCH,
        // Sorting event
        SORT,
    }

    /**
     * Filter events sent by the user via the UI.
     */
    public enum UserFilterEvent {
        // Filtering events
        ON_FILTER_PDF, OFF_FILTER_PDF,
        ON_FILTER_OFFICE, OFF_FILTER_OFFICE,
        ON_FILTER_IMAGES, OFF_FILTER_IMAGES,
        FILTER_NONE,
    }

    /**
     * UI update events handled by the view/UI.
     */
    public enum UiUpdateEvent {
        // Recycler View loading events
        LOADING_FINISHED, LOADING_STARTED, LOADING_ERRORED, EMPTY_LIST,
        // Filtering events
        FILTER_NO_MATCHES, FILTER_STARTED, FILTER_FINISHED,
        // Search events
        SEARCH_NO_MATCHES, SEARCH_STARTED, SEARCH_FINISHED,
        // Sort events
        SORT_STARTED, SORT_FINISHED,
    }
}
