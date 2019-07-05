package com.pdftron.collab.ui.annotlist.component;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;

import com.pdftron.collab.ui.annotlist.component.view.AnnotationListEvent;
import com.pdftron.collab.ui.annotlist.model.list.AnnotationList;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

/**
 * A {@link ViewModel} that contains data for the annotation list UI. Contains the
 * observable that will emit {@link AnnotationListEvent}s from user UI interactions, which is usually
 * used by components, fragments, or activities to listen UI events.
 */
public class AnnotationListViewModel extends ViewModel {

    @NonNull
    private final MutableLiveData<AnnotationList> mAnnotListLiveData = new MutableLiveData<>();

    @NonNull
    private final PublishSubject<AnnotationListEvent> mAnnotListObservable = PublishSubject.create();

    PublishSubject<AnnotationListEvent> getAnnotationListSubject() {
        return mAnnotListObservable;
    }

    /**
     * @return the observable that emits user interaction events with the Annotation List UI
     */
    @NonNull
    public Observable<AnnotationListEvent> getAnnotationListObservable() {
        return mAnnotListObservable;
    }

    LiveData<AnnotationList> getAnnotationListLiveData() {
        return mAnnotListLiveData;
    }

    public void setAnnotationList(AnnotationList annotItems) {
        mAnnotListLiveData.setValue(annotItems);
    }
}
