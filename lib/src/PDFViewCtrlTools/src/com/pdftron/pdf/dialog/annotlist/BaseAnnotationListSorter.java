package com.pdftron.pdf.dialog.annotlist;

import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;
import android.support.annotation.RestrictTo;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @hide
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public abstract class BaseAnnotationListSorter<T> extends ViewModel {
    protected final MutableLiveData<BaseAnnotationSortOrder> mSortOrder = new MutableLiveData<>();

    public BaseAnnotationListSorter(@NonNull BaseAnnotationSortOrder annotationListSortOrder) {
        mSortOrder.setValue(annotationListSortOrder);
    }

    public void observeSortOrderChanges(@NonNull LifecycleOwner owner,
            @NonNull Observer<BaseAnnotationSortOrder> observer) {
        mSortOrder.observe(owner, observer);
    }

    public void publishSortOrderChange(@NonNull BaseAnnotationSortOrder sortOrder) {
        mSortOrder.setValue(sortOrder);
    }

    public void sort(@NonNull List<T> annotationInfos) {
        Collections.sort(annotationInfos, getComparator());
    }

    @NonNull
    public abstract Comparator<T> getComparator();
}
