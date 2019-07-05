package com.pdftron.collab.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import com.pdftron.collab.DataRepository;
import com.pdftron.collab.db.entity.ReplyEntity;

import java.util.List;

/**
 * ViewModel for annotation reply
 */
public class ReplyViewModel extends AndroidViewModel {

    private DataRepository mRepository;

    private LiveData<List<ReplyEntity>> mReplies;

    private String mAnnotationId;

    public ReplyViewModel(@NonNull Application application, String annotationId) {
        super(application);

        mAnnotationId = annotationId;

        mRepository = DataRepository.getInstance(application);

        mReplies = mRepository.getReplies(mAnnotationId);
    }

    public LiveData<List<ReplyEntity>> getReplies() {
        return mReplies;
    }

    public static class Factory extends ViewModelProvider.NewInstanceFactory {

        @NonNull
        private final Application mApplication;

        private final String mAnnotationId;

        public Factory(@NonNull Application application, String annotationId) {
            mApplication = application;
            mAnnotationId = annotationId;
        }

        @Override
        public <T extends ViewModel> T create(Class<T> modelClass) {
            //noinspection unchecked
            return (T) new ReplyViewModel(mApplication, mAnnotationId);
        }
    }
}
