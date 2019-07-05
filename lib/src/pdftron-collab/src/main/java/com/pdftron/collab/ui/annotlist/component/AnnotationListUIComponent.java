package com.pdftron.collab.ui.annotlist.component;

import android.arch.lifecycle.LifecycleOwner;
import android.support.annotation.NonNull;
import android.view.ViewGroup;

import com.pdftron.collab.ui.annotlist.CollabAnnotationListSorter;
import com.pdftron.collab.ui.annotlist.component.view.AnnotationListEvent;
import com.pdftron.collab.ui.annotlist.component.view.AnnotationListUIView;
import com.pdftron.collab.ui.base.component.BaseUIComponent;
import com.pdftron.collab.viewmodel.AnnotationViewModel;
import com.pdftron.pdf.PDFViewCtrl;
import com.pdftron.pdf.utils.Logger;

/**
 * A {@link BaseUIComponent} representing the annotation list. Responsible for updating
 * changes from {@link AnnotationViewModel} to the {@link AnnotationListViewModel}. Also in charge of
 * updating the {@link AnnotationListViewModel} when the data order is changes via sorting.
 */
public class AnnotationListUIComponent extends
        BaseUIComponent<AnnotationListUIView, AnnotationListEvent, AnnotationListViewModel> {

    public static final String TAG = AnnotationListUIComponent.class.getName();

    public AnnotationListUIComponent(@NonNull ViewGroup parent,
            @NonNull LifecycleOwner lifecycleOwner,
            @NonNull AnnotationListViewModel uiViewModel,
            @NonNull AnnotationViewModel annotViewModel,
            @NonNull PDFViewCtrl mPdfViewCtrl,
            @NonNull CollabAnnotationListSorter sorter) {

        super(parent, lifecycleOwner, uiViewModel, uiViewModel.getAnnotationListSubject());

        annotViewModel.getAnnotations()
                .observe(lifecycleOwner, annotationEntities -> {
                    if (annotationEntities != null) {
                        Logger.INSTANCE.LogD(TAG, "annotations: " + annotationEntities.size());
                        uiViewModel.setAnnotationList(sorter.getAnnotationList(parent.getContext(), mPdfViewCtrl, annotationEntities));
                    }
                });

        // This initializes the view state, and also updates the view on data changes
        uiViewModel.getAnnotationListLiveData().observe(lifecycleOwner, annotList -> {
            if (annotList != null) {
                mView.setAnnotList(annotList);
            }
        });
    }

    @NonNull
    @Override
    protected AnnotationListUIView inflateUIView(@NonNull ViewGroup parent) {
        return new AnnotationListUIView(parent);
    }
}
