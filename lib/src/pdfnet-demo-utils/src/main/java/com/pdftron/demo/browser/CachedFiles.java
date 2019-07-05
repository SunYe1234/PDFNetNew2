package com.pdftron.demo.browser;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.google.gson.reflect.TypeToken;
import com.pdftron.demo.utils.CacheUtils;
import com.pdftron.pdf.utils.Logger;
import com.pdftron.demo.utils.MiscUtils;
import com.pdftron.pdf.model.FileInfo;
import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.CompletableOnSubscribe;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.observers.DisposableCompletableObserver;
import io.reactivex.schedulers.Schedulers;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@SuppressWarnings("RedundantThrows")
class CachedFiles {
    private static final String TAG = CachedFiles.class.toString();
    private static final String CACHED_ALL_FILE_LIST = "CachedFiles_all_files_cache";

    @Nullable
    private Disposable mCacheSubscription = null;
    private final AtomicBoolean mHasCache = new AtomicBoolean(false);

    CachedFiles() {
        mHasCache.set(CacheUtils.hasCache(CACHED_ALL_FILE_LIST));
    }

    AtomicBoolean hasCache() {
        return mHasCache;
    }

    // cache is valid if less than one day old
    boolean isValid() {
        Date dateToCheck = CacheUtils.lastModified(CACHED_ALL_FILE_LIST);
        if (dateToCheck == null) {
            return false;
        } else {
            return !isOneDayOld(dateToCheck);
        }
    }

    static boolean isOneDayOld(@NonNull Date dateToCheck) {
        Calendar currentTime = Calendar.getInstance();
        currentTime.setTime(new Date());
        currentTime.add(Calendar.HOUR, -24);
        Date expiredDate = currentTime.getTime();
        return dateToCheck.before(expiredDate);
    }

    Observable<List<FileInfo>> fromCache() {
        return Observable.create(
            new ObservableOnSubscribe<List<FileInfo>>() {
                @Override
                public void subscribe(ObservableEmitter<List<FileInfo>> emitter) throws Exception {
                    MiscUtils.throwIfOnMainThread();
                    List<List<FileInfo>> cachedFiles =
                        CacheUtils.readObjectFile(CACHED_ALL_FILE_LIST,
                            new TypeToken<List<List<FileInfo>>>() {
                            }.getType()
                        );
                    for (List<FileInfo> group : cachedFiles) {
                        if (emitter.isDisposed()) {
                            break;
                        }
                        emitter.onNext(group);
                    }
                    emitter.onComplete();
                }
            })
            .doOnError(new Consumer<Throwable>() {
                @Override
                public void accept(Throwable throwable) throws Exception {
                    Logger.INSTANCE.LogE(TAG, "Something went wrong from getting the cache, ditching the cache");
                    mHasCache.set(false);
                    CacheUtils.deleteFile(CACHED_ALL_FILE_LIST);
                }
            });
    }

    void cacheFiles(final GroupedFiles groupedFiles) {
        if (mCacheSubscription != null) {
            mCacheSubscription.dispose();
        }
        mCacheSubscription = Completable.create(
            new CompletableOnSubscribe() {
                @Override
                public void subscribe(CompletableEmitter emitter) throws Exception {
                    MiscUtils.throwIfOnMainThread();
                    if (emitter.isDisposed()) {
                        emitter.onComplete();
                        return;
                    }
                    CacheUtils.writeObjectFile(CACHED_ALL_FILE_LIST, groupedFiles.getGroupedList());
                    emitter.onComplete();
                }
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe(new Consumer<Disposable>() {
                @Override
                public void accept(Disposable disposable) throws Exception {
                    Logger.INSTANCE.LogD(TAG, "Caching files");
                }
            })
            .subscribeWith(new DisposableCompletableObserver() {
                @Override
                public void onComplete() {
                    // Explicitly call this, might have to check dispose state of mCacheSubscription
                    mCacheSubscription.dispose();
                    mHasCache.set(true);
                    Logger.INSTANCE.LogD(TAG, "Finished caching files");
                }

                @Override
                public void onError(Throwable e) {
                    mHasCache.set(false);
                    Logger.INSTANCE.LogE(TAG, "Error occurred when caching files: " + e);
                }
            });
    }
}
