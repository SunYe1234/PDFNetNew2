package com.pdftron.collab.ui.annotlist;

import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.pdftron.collab.R;
import com.pdftron.collab.db.entity.AnnotationEntity;
import com.pdftron.collab.ui.annotlist.component.AnnotationListUIComponent;
import com.pdftron.collab.ui.annotlist.component.AnnotationListViewModel;
import com.pdftron.collab.ui.annotlist.model.list.item.AnnotationListContent;
import com.pdftron.collab.viewmodel.AnnotationViewModel;
import com.pdftron.pdf.Annot;
import com.pdftron.pdf.controls.AnnotationDialogFragment;
import com.pdftron.pdf.dialog.annotlist.BaseAnnotationListSorter;
import com.pdftron.pdf.dialog.annotlist.BaseAnnotationSortOrder;
import com.pdftron.pdf.utils.AnalyticsHandlerAdapter;
import com.pdftron.pdf.utils.PdfViewCtrlSettingsManager;

import java.util.List;

import io.reactivex.disposables.CompositeDisposable;

public class CollabAnnotationListFragment extends AnnotationDialogFragment {

    private static final String TAG = CollabAnnotationListFragment.class.getName();
    private static final String DOCUMENT_ID_KEY = "CollabAnnotationListFragment_document_id";

    private AnnotationViewModel mAnnotationViewModel;
    private AnnotationListViewModel mListViewModel;
    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();

    @SuppressWarnings("NullableProblems")
    @NonNull
    private String mDocumentId;

    public static CollabAnnotationListFragment newInstance() {
        return new CollabAnnotationListFragment();
    }

    /**
     * Creates a bundle containing arguments for {@link AnnotationDialogFragment}
     *
     * @param documentId              unique identifier for the collaborative document
     * @return arguments for {@link AnnotationDialogFragment}
     */
    public static Bundle newBundle(@NonNull String documentId) {
        Bundle bundle = new Bundle();
        bundle.putString(DOCUMENT_ID_KEY, documentId);
        return bundle;
    }

    /**
     * Creates a bundle containing arguments for {@link AnnotationDialogFragment}
     *
     * @param documentId              unique identifier for the collaborative document
     * @param isReadOnly              true if the annotation list should be read only (default false)
     * @param isRtl                   true if the the annotations are displayed right-to-left (default false)
     * @param annotationListSortOrder sorting order of the annotations
     * @return arguments for {@link AnnotationDialogFragment}
     */
    public static Bundle newBundle(@NonNull String documentId, boolean isReadOnly, boolean isRtl,
            @NonNull CollabAnnotationListSortOrder annotationListSortOrder) {
        Bundle args = new Bundle();
        args.putBoolean(BUNDLE_IS_READ_ONLY, isReadOnly);
        args.putBoolean(BUNDLE_IS_RTL, isRtl);
        args.putInt(BUNDLE_KEY_SORT_MODE, annotationListSortOrder.value);
        args.putString(DOCUMENT_ID_KEY, documentId);
        return args;
    }

    @NonNull
    @Override
    protected BaseAnnotationSortOrder getSortOrder(Bundle args) {
        return args != null && args.containsKey(BUNDLE_KEY_SORT_MODE) ?
                CollabAnnotationListSortOrder.fromValue(
                        args.getInt(BUNDLE_KEY_SORT_MODE, CollabAnnotationListSortOrder.LAST_ACTIVITY.value)
                ) :
                CollabAnnotationListSortOrder.LAST_ACTIVITY; // default sort by date
    }

    @NonNull
    @Override
    protected BaseAnnotationListSorter getSorter() {
        return ViewModelProviders.of(this,
                new CollabAnnotationListSorter.Factory(mAnnotationListSortOrder))
                .get(CollabAnnotationListSorter.class);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_collab_annotlist_sort, menu);
        final MenuItem sortByReplyItem = menu.findItem(R.id.menu_annotlist_sort_by_reply_date);
        final MenuItem sortByDateItem = menu.findItem(R.id.menu_annotlist_sort_by_date);
        final MenuItem sortByPosItem = menu.findItem(R.id.menu_annotlist_sort_by_position);

        // Observe sort order to update menu UI
        mSorter.observeSortOrderChanges(getViewLifecycleOwner(), new Observer<BaseAnnotationSortOrder>() {

            // Helper method to update annotation list sorting order in shared prefs
            private void updateSharedPrefs(CollabAnnotationListSortOrder sortOrder) {
                Context context = getContext();
                if (context != null) {
                    PdfViewCtrlSettingsManager.updateAnnotListSortOrder(context,
                            sortOrder);
                }
            }

            @Override
            public void onChanged(@Nullable BaseAnnotationSortOrder sortOrder) {
                if (sortOrder != null) {
                    if (sortOrder instanceof CollabAnnotationListSortOrder)
                        switch (((CollabAnnotationListSortOrder) sortOrder)) {
                            case DATE_DESCENDING:
                                updateSharedPrefs(CollabAnnotationListSortOrder.DATE_DESCENDING);
                                sortByDateItem.setChecked(true);
                                break;
                            case POSITION_ASCENDING:
                                updateSharedPrefs(CollabAnnotationListSortOrder.POSITION_ASCENDING);
                                sortByPosItem.setChecked(true);
                                break;
                            case LAST_ACTIVITY:
                                updateSharedPrefs(CollabAnnotationListSortOrder.LAST_ACTIVITY);
                                sortByReplyItem.setChecked(true);
                                break;
                        }
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_annotlist_sort_by_date) {
            mSorter.publishSortOrderChange(CollabAnnotationListSortOrder.DATE_DESCENDING);
        } else if (id == R.id.menu_annotlist_sort_by_position) {
            mSorter.publishSortOrderChange(CollabAnnotationListSortOrder.POSITION_ASCENDING);
        } else if (id == R.id.menu_annotlist_sort_by_reply_date) {
            mSorter.publishSortOrderChange(CollabAnnotationListSortOrder.LAST_ACTIVITY);
        } else {
            return false;
        }
        return true;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            String docId = args.getString(DOCUMENT_ID_KEY);
            if (docId == null) {
                throw new IllegalArgumentException();
            } else {
                mDocumentId = docId;
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_collab_annot_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        // Setup view models
        LifecycleOwner lifecycleOwner = getViewLifecycleOwner();
        FrameLayout listContainer = view.findViewById(R.id.list_container);
        FragmentActivity activity = getActivity();
        mListViewModel = ViewModelProviders.of(activity).get(AnnotationListViewModel.class);
        // must be scoped to this fragment as we reference pdfviewCtrl. We probably want to move this to the parent fragment?
        mAnnotationViewModel = ViewModelProviders.of(this,
                new AnnotationViewModel.Factory(
                        activity.getApplication(),
                        mDocumentId
                )
        ).get(AnnotationViewModel.class);

        // Initialize components
        final AnnotationListUIComponent component =
                new AnnotationListUIComponent(listContainer,
                        lifecycleOwner,
                        mListViewModel,
                        mAnnotationViewModel,
                        mPdfViewCtrl,
                        (CollabAnnotationListSorter) mSorter
                );

        mCompositeDisposable.add(
                component.getObservable()
                        .subscribe(annotationListEvent -> {
                            switch (annotationListEvent.getEventType()) {
                                case ANNOTATION_ITEM_CLICKED: {
                                    AnnotationListContent annotContent = annotationListEvent.getData();
                                    if (annotContent != null) {
                                        Annot annot = annotContent.getAnnotation();
                                        int pageNum = annotContent.getPageNum();
                                        // Notify listeners
                                        if (mAnnotationDialogListener != null) {
                                            mAnnotationDialogListener.onAnnotationClicked(annot, pageNum);
                                        }
                                        dismiss();
                                    }
                                    break;
                                }
                            }
                        }, throwable -> AnalyticsHandlerAdapter.getInstance()
                                .sendException(new RuntimeException(throwable)))
        );

        mSorter.observeSortOrderChanges(getViewLifecycleOwner(), sortOrder -> {
            List<AnnotationEntity> entities = mAnnotationViewModel.getAnnotations().getValue();
            if (entities != null && sortOrder != null && mSorter instanceof CollabAnnotationListSorter) {
                mListViewModel.setAnnotationList(
                        ((CollabAnnotationListSorter) mSorter).getAnnotationList(
                                activity,
                                mPdfViewCtrl,
                                entities
                        )
                );
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mCompositeDisposable.clear();
    }
}
