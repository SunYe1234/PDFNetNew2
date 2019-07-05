package com.pdftron.collab.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.pdftron.collab.DataRepository;
import com.pdftron.collab.db.entity.DocumentEntity;
import com.pdftron.collab.db.entity.UserEntity;
import com.pdftron.collab.service.CustomService;
import com.pdftron.collab.utils.JsonUtils;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

/**
 * ViewModel for document
 */
public class DocumentViewModel extends AndroidViewModel {

    @Nullable
    private DataRepository mRepository;

    private final LiveData<DocumentEntity> mDocument;
    private final LiveData<UserEntity> mUser;

    public DocumentViewModel(@NonNull Application application) {
        super(application);

        mRepository = DataRepository.getInstance(application);
        mDocument = mRepository.getDocument();
        mUser = mRepository.getUser();
    }

    @Override
    protected void onCleared() {
        if (mRepository != null) {
            mRepository.close();
        }
        mRepository = null;
    }

    public void setCustomConnection(CustomService connection) {
        if (null == mRepository) {
            return;
        }
        mRepository.setCustomService(connection);
    }

    public Completable updateUnreadAnnotations(String docId, String annotId) {
        return Completable.mergeArray(
                removeUnread(docId, annotId),
                updateLastRead(annotId)
        );
    }

    public Completable removeUnread(String docId, String annotId) {
        if (null == mRepository) {
            return Completable.fromAction(() -> {
            });
        }
        return mRepository.removeDocumentUnread(docId, annotId);
    }

    public Completable updateLastRead(String annotId) {
        if (null == mRepository) {
            return Completable.fromAction(() -> {
            });
        }
        return mRepository.updateAnnotationUnreads(annotId);
    }

    public Completable updateActiveAnnotation(String userId, String annotId) {
        if (null == mRepository) {
            return Completable.fromAction(() -> {
            });
        }
        return mRepository.updateActiveAnnotation(userId, annotId);
    }

    public LiveData<DocumentEntity> getDocument() {
        return mDocument;
    }

    /**
     * Hot observable that emits true if there are unread annotations, emits false otherwise.
     *
     * @param owner lifecycle owner that observes unread annotation notifications
     * @return observable that emits whether there are notifications
     */
    public Observable<Boolean> getUnreadObservable(@NonNull LifecycleOwner owner) {
        PublishSubject<Boolean> observable = PublishSubject.create();
        mDocument.observe(owner,
                documentEntity -> {
                    if (documentEntity != null) {
                        observable.onNext(hasUnreadReplies(documentEntity));
                    }
                });

        return observable.serialize();
    }

    public boolean hasUnreadReplies() {
        DocumentEntity documentEntity = mDocument.getValue();
        if (documentEntity != null) {
            return hasUnreadReplies(documentEntity);
        } else {
            return false;
        }
    }

    private boolean hasUnreadReplies(@NonNull DocumentEntity documentEntity) {
        String unreads = documentEntity.getUnreads();
        return unreads != null && JsonUtils.safeGetJsonArrayLength(unreads) > 0;
    }

    public LiveData<UserEntity> getUser() {
        return mUser;
    }
}
