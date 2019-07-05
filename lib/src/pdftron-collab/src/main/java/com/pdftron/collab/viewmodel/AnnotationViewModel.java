package com.pdftron.collab.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import com.pdftron.collab.DataRepository;
import com.pdftron.collab.db.entity.AnnotationEntity;
import com.pdftron.collab.db.entity.LastAnnotationEntity;

import java.util.List;

/**
 * ViewModel for annotation
 */
public class AnnotationViewModel extends AndroidViewModel {

    private DataRepository mRepository;

    private LiveData<List<LastAnnotationEntity>> mLastAnnotations;
    private LiveData<List<AnnotationEntity>> mAnnotations;

    private String mDocumentId;

    public AnnotationViewModel(@NonNull Application application, final String documentId) {
        super(application);

        mDocumentId = documentId;

        mRepository = DataRepository.getInstance(application);

        mLastAnnotations = mRepository.getLastAnnotations();
        mAnnotations = mRepository.getAnnotations(mDocumentId);
    }

    public LiveData<List<LastAnnotationEntity>> getLastAnnotations() {
        return mLastAnnotations;
    }

    public LiveData<List<AnnotationEntity>> getAnnotations() {
        return mAnnotations;
    }

    public void sendAnnotation(String action, String xfdfJSON, String userName) {
        mRepository.sendAnnotation(action, xfdfJSON, mDocumentId, userName);
    }

    public static class Factory extends ViewModelProvider.NewInstanceFactory {

        @NonNull
        private final Application mApplication;

        private final String mDocumentId;

        public Factory(@NonNull Application application, String documentId) {
            mApplication = application;
            mDocumentId = documentId;
        }

        @Override
        public <T extends ViewModel> T create(Class<T> modelClass) {
            //noinspection unchecked
            return (T) new AnnotationViewModel(mApplication, mDocumentId);
        }
    }
}
