//---------------------------------------------------------------------------------------
// Copyright (c) 2001-2019 by PDFTron Systems Inc. All Rights Reserved.
// Consult legal.txt regarding legal and license information.
//---------------------------------------------------------------------------------------

package com.pdftron.pdf.controls;

import android.annotation.SuppressLint;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.clans.fab.FloatingActionButton;
import com.pdftron.pdf.Annot;
import com.pdftron.pdf.PDFDoc;
import com.pdftron.pdf.PDFViewCtrl;
import com.pdftron.pdf.Page;
import com.pdftron.pdf.Print;
import com.pdftron.pdf.Rect;
import com.pdftron.pdf.dialog.annotlist.AnnotationListSortOrder;
import com.pdftron.pdf.dialog.annotlist.AnnotationListSorter;
import com.pdftron.pdf.dialog.annotlist.AnnotationListUtil;
import com.pdftron.pdf.dialog.annotlist.BaseAnnotationListSorter;
import com.pdftron.pdf.dialog.annotlist.BaseAnnotationSortOrder;
import com.pdftron.pdf.tools.R;
import com.pdftron.pdf.tools.ToolManager;
import com.pdftron.pdf.utils.AnalyticsHandlerAdapter;
import com.pdftron.pdf.utils.AnalyticsParam;
import com.pdftron.pdf.utils.AnnotUtils;
import com.pdftron.pdf.utils.PdfViewCtrlSettingsManager;
import com.pdftron.pdf.utils.Utils;
import com.pdftron.pdf.utils.ViewerUtils;
import com.pdftron.pdf.widget.recyclerview.ItemClickHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * The AnnotationDialogFragment shows a list of all the annotations in a
 * document being viewed by a {@link com.pdftron.pdf.PDFViewCtrl}. The list will
 * contain any comments that have been added to the annotations and clicking on
 * an annotation will show it in the PDFViewCtrl.
 */
public class AnnotationDialogFragment extends NavigationListDialogFragment {

    /**
     * Bundle key to specify whether the document is read only or not
     */
    public static final String BUNDLE_IS_READ_ONLY = "is_read_only";
    public static final String BUNDLE_IS_RTL = "is_right-to-left";
    public static final String BUNDLE_KEY_SORT_MODE = "sort_mode_as_int";

    private static final int CONTEXT_MENU_DELETE_ITEM = 0;
    private static final int CONTEXT_MENU_DELETE_ITEM_ON_PAGE = 1;
    private static final int CONTEXT_MENU_DELETE_ALL = 2;

    protected boolean mIsReadOnly;
    protected boolean mIsRtl;
    protected BaseAnnotationSortOrder mAnnotationListSortOrder;

    private ArrayList<AnnotationInfo> mAnnotation;
    private AnnotationsAdapter mAnnotationsAdapter;
    private RecyclerView mRecyclerView;
    private TextView mEmptyTextView;
    protected PDFViewCtrl mPdfViewCtrl;
    private FloatingActionButton mFab;
    protected AnnotationDialogListener mAnnotationDialogListener;
    private ProgressBar mProgressBarView;
    protected BaseAnnotationListSorter mSorter;

    private Observable<List<AnnotationInfo>> mAnnotListObservable;
    private final CompositeDisposable mDisposables = new CompositeDisposable();

    /**
     * Callback interface to be invoked when an interaction is needed.
     */
    public interface AnnotationDialogListener {
        /**
         * Called when an annotation has been clicked.
         *
         * @param annotation The annotation
         * @param pageNum    The page number that holds the annotation
         */
        void onAnnotationClicked(Annot annotation, int pageNum);

        /**
         * Called when document annotations have been exported.
         *
         * @param outputDoc The PDFDoc containing the exported annotations
         */
        void onExportAnnotations(PDFDoc outputDoc);
    }

    /**
     * Creates a default instance of {@link AnnotationDialogFragment}.
     *
     * @return a new default instance of this class
     */
    public static AnnotationDialogFragment newInstance() {
        return new AnnotationDialogFragment();
    }

    /**
     * Creates an instance of {@link AnnotationDialogFragment}, with specified settings.
     *
     * @param isReadOnly              true if the annotation list should be read only (default false)
     * @param isRtl                   true if the the annotations are displayed right-to-left (default false)
     * @param annotationListSortOrder sorting order of the annotations
     * @return a new instance of this class with specified settings.
     */
    public static AnnotationDialogFragment newInstance(boolean isReadOnly, boolean isRtl, @NonNull AnnotationListSortOrder annotationListSortOrder) {
        Bundle args = newBundle(isReadOnly, isRtl, annotationListSortOrder);

        AnnotationDialogFragment fragment = new AnnotationDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Creates a bundle containing arguments for {@link AnnotationDialogFragment}
     *
     * @param isReadOnly              true if the annotation list should be read only (default false)
     * @param isRtl                   true if the the annotations are displayed right-to-left (default false)
     * @param annotationListSortOrder sorting order of the annotations
     * @return arguments for {@link AnnotationDialogFragment}
     */
    public static Bundle newBundle(boolean isReadOnly, boolean isRtl, @NonNull AnnotationListSortOrder annotationListSortOrder) {
        Bundle args = new Bundle();
        args.putBoolean(BUNDLE_IS_READ_ONLY, isReadOnly);
        args.putBoolean(BUNDLE_IS_RTL, isRtl);
        args.putInt(BUNDLE_KEY_SORT_MODE, annotationListSortOrder.value);
        return args;
    }

    /**
     * Sets the {@link PDFViewCtrl}
     *
     * @param pdfViewCtrl The {@link PDFViewCtrl}
     * @return This class
     */
    public AnnotationDialogFragment setPdfViewCtrl(@NonNull PDFViewCtrl pdfViewCtrl) {
        mPdfViewCtrl = pdfViewCtrl;
        return this;
    }

    /**
     * Sets if the document is read only
     *
     * @param isReadOnly True if the document is read only
     * @return This class
     */
    @SuppressWarnings("unused")
    public AnnotationDialogFragment setReadOnly(boolean isReadOnly) {
        Bundle args = getArguments();
        if (args == null) {
            args = new Bundle();
        }
        args.putBoolean(BUNDLE_IS_READ_ONLY, isReadOnly);
        setArguments(args);

        return this;
    }

    /**
     * Sets if the document is right-to-left
     *
     * @param isRtl True if the document is right-to-left
     * @return This class
     */
    @SuppressWarnings("unused")
    public AnnotationDialogFragment setRtlMode(boolean isRtl) {
        Bundle args = getArguments();
        if (args == null) {
            args = new Bundle();
        }
        args.putBoolean(BUNDLE_IS_RTL, isRtl);
        setArguments(args);

        return this;
    }

    /**
     * Sets the listener to {@link AnnotationDialogListener}
     *
     * @param listener The listener
     */
    public void setAnnotationDialogListener(AnnotationDialogListener listener) {
        mAnnotationDialogListener = listener;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_annotlist_sort, menu);
        final MenuItem sortByDateItem = menu.findItem(R.id.menu_annotlist_sort_by_date);
        final MenuItem sortByPosItem = menu.findItem(R.id.menu_annotlist_sort_by_position);
        mSorter.observeSortOrderChanges(getViewLifecycleOwner(), new Observer<BaseAnnotationSortOrder>() {

            // Helper method to update annotation list sorting order in shared prefs
            private void updateSharedPrefs(AnnotationListSortOrder sortOrder) {
                Context context = getContext();
                if (context != null) {
                    PdfViewCtrlSettingsManager.updateAnnotListSortOrder(context, sortOrder);
                }
            }

            @Override
            public void onChanged(@Nullable BaseAnnotationSortOrder sortOrder) {
                if (sortOrder instanceof AnnotationListSortOrder)
                    switch (((AnnotationListSortOrder) sortOrder)) {
                        case DATE_ASCENDING:
                            updateSharedPrefs(AnnotationListSortOrder.DATE_ASCENDING);
                            sortByDateItem.setChecked(true);
                            break;
                        case POSITION_ASCENDING:
                            updateSharedPrefs(AnnotationListSortOrder.POSITION_ASCENDING);
                            sortByPosItem.setChecked(true);
                            break;
                    }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_annotlist_sort_by_date) {
            mSorter.publishSortOrderChange(AnnotationListSortOrder.DATE_ASCENDING);
        } else if (id == R.id.menu_annotlist_sort_by_position) {
            mSorter.publishSortOrderChange(AnnotationListSortOrder.POSITION_ASCENDING);
        } else {
            return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        Bundle args = getArguments();
        if (args != null) {
            mIsReadOnly = args.getBoolean(BUNDLE_IS_READ_ONLY);
            mIsRtl = args.getBoolean(BUNDLE_IS_RTL);
        }
        mAnnotationListSortOrder = getSortOrder(args);
        mAnnotation = new ArrayList<>();
        mAnnotListObservable = AnnotationListUtil.from(mPdfViewCtrl).cache();
        mSorter = getSorter();
    }

    @NonNull
    protected BaseAnnotationSortOrder getSortOrder(@Nullable Bundle args) {
        return args != null && args.containsKey(BUNDLE_KEY_SORT_MODE) ?
                AnnotationListSortOrder.fromValue(
                        args.getInt(BUNDLE_KEY_SORT_MODE, AnnotationListSortOrder.DATE_ASCENDING.value)
                ) :
                AnnotationListSortOrder.DATE_ASCENDING; // default sort by date
    }

    @NonNull
    protected BaseAnnotationListSorter getSorter() {
        return ViewModelProviders.of(this,
                new AnnotationListSorter.Factory(mAnnotationListSortOrder))
                .get(AnnotationListSorter.class);
    }

    /**
     * The overloaded implementation of {@link DialogFragment#onCreateView(LayoutInflater, ViewGroup, Bundle)}
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        @SuppressLint("InflateParams")
        View view = inflater.inflate(R.layout.controls_fragment_annotation_dialog, null);

        // Get reference to controls
        mRecyclerView = view.findViewById(R.id.recyclerview_control_annotation);
        mEmptyTextView = view.findViewById(R.id.control_annotation_textview_empty);
        mProgressBarView = view.findViewById(R.id.progress_bar_view);

        mFab = view.findViewById(R.id.export_annotations_button);
        //mFab=null;
        mFab.setVisibility(View.INVISIBLE);
        if (mIsReadOnly) {

            mFab.setVisibility(View.GONE);
        }
        if (mFab!=null) {
            mFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mAnnotationDialogListener != null) {
                        mDisposables.add(prepareAnnotations()
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .doOnSubscribe(new Consumer<Disposable>() {
                                    @Override
                                    public void accept(Disposable disposable) throws Exception {
                                        mProgressBarView.setVisibility(View.VISIBLE);
                                    }
                                })
                                .subscribe(new Consumer<PDFDoc>() {
                                               @Override
                                               public void accept(PDFDoc pdfDoc) throws Exception {
                                                   mProgressBarView.setVisibility(View.GONE);
                                                   if (mAnnotationDialogListener != null) {
                                                       mAnnotationDialogListener.onExportAnnotations(pdfDoc);
                                                   }
                                               }
                                           },
                                        new Consumer<Throwable>() {
                                            @Override
                                            public void accept(Throwable throwable) throws Exception {
                                                mProgressBarView.setVisibility(View.GONE);
                                                AnalyticsHandlerAdapter.getInstance().sendException(new Exception(throwable));
                                            }
                                        }
                                ));
                    }
                    onEventAction();
                    AnalyticsHandlerAdapter.getInstance().sendEvent(AnalyticsHandlerAdapter.EVENT_ANNOTATIONS_LIST,
                            AnalyticsParam.annotationsListActionParam(AnalyticsHandlerAdapter.ANNOTATIONS_LIST_EXPORT));
                }
            });
        }

        // Add click listener to the list
        ItemClickHelper itemClickHelper = new ItemClickHelper();
        itemClickHelper.attachToRecyclerView(mRecyclerView);
        itemClickHelper.setOnItemClickListener(new ItemClickHelper.OnItemClickListener() {
            @Override
            public void onItemClick(RecyclerView recyclerView, View view, int position, long id) {
                onEventAction();
                AnalyticsHandlerAdapter.getInstance().sendEvent(AnalyticsHandlerAdapter.EVENT_VIEWER_NAVIGATE_BY,
                        AnalyticsParam.viewerNavigateByParam(AnalyticsHandlerAdapter.VIEWER_NAVIGATE_BY_ANNOTATIONS_LIST));

                AnnotationInfo annotInfo = mAnnotation.get(position);
                if (mPdfViewCtrl != null) {
                    ViewerUtils.jumpToAnnotation(mPdfViewCtrl, annotInfo.getAnnotation(), annotInfo.getPageNum());
                }

                // Notify listeners
                if (mAnnotationDialogListener != null) {
                    mAnnotationDialogListener.onAnnotationClicked(annotInfo.getAnnotation(), annotInfo.getPageNum());
                }
            }
        });

        return view;
    }

    /**
     * The overloaded implementation of {@link DialogFragment#onViewCreated(View, Bundle)}
     */
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mAnnotationsAdapter = new AnnotationsAdapter(mAnnotation);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        mRecyclerView.setAdapter(mAnnotationsAdapter);

        mEmptyTextView.setText(R.string.controls_annotation_dialog_loading);

        mSorter.observeSortOrderChanges(getViewLifecycleOwner(), new Observer<BaseAnnotationSortOrder>() {
            @Override
            public void onChanged(@Nullable BaseAnnotationSortOrder annotationListSortOrder) {
                if (annotationListSortOrder != null) {
                    mAnnotationsAdapter.clear();
                    mAnnotationsAdapter.notifyDataSetChanged();
                    populateAnnotationList();
                }
            }
        });
    }

    private Single<PDFDoc> prepareAnnotations() {
        return Single.fromCallable(new Callable<PDFDoc>() {
            @Override
            public PDFDoc call() throws Exception {
                return Print.exportAnnotations(mPdfViewCtrl.getDoc(), mIsRtl);
            }
        });
    }

    /**
     * Helper method to populate and re-populate the annotation list with a specified sort order.
     * Will clear the list before populating.
     */
    @SuppressWarnings("RedundantThrows")
    private void populateAnnotationList() {
        // This will populate mAnnotation
        mDisposables.add(
                mAnnotListObservable
                        .map(new Function<List<AnnotationInfo>, List<AnnotationInfo>>() {
                            @Override
                            public List<AnnotationInfo> apply(List<AnnotationInfo> annotationInfos) throws Exception {
                                if (mSorter instanceof AnnotationListSorter) {
                                    //noinspection RedundantCast
                                    ((AnnotationListSorter) mSorter).sort(annotationInfos);
                                    return annotationInfos;
                                } else {
                                    return annotationInfos;
                                }
                            }
                        })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Consumer<List<AnnotationInfo>>() {
                            @Override
                            public void accept(List<AnnotationInfo> annotationInfos) throws Exception {
                                mAnnotationsAdapter.addAll(annotationInfos);
                            }
                        }, new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) throws Exception {
                                AnalyticsHandlerAdapter.getInstance().sendException(new RuntimeException(throwable));
                            }
                        }, new Action() {
                            @Override
                            public void run() throws Exception {
                                if (mFab != null) {
                                    //mFab.setVisibility(mAnnotationsAdapter.getItemCount() > 0 ? View.VISIBLE : View.GONE);
                                    mFab.setVisibility(View.INVISIBLE);

                                    if (mIsReadOnly) {
                                        mFab.setVisibility(View.GONE);
                                    }
                                    mEmptyTextView.setText(R.string.controls_annotation_dialog_empty);
                                    if (mAnnotationsAdapter.getItemCount() == 0) {
                                        mEmptyTextView.setVisibility(View.VISIBLE);
                                        mRecyclerView.setVisibility(View.GONE);
                                    } else {
                                        mEmptyTextView.setVisibility(View.GONE);
                                        mRecyclerView.setVisibility(View.VISIBLE);
                                    }
                                    mProgressBarView.setVisibility(View.GONE);
                                }
                            }
                        })
        );
    }

    /**
     * The overloaded implementation of {@link DialogFragment#onStop()}
     */
    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mDisposables.clear();
    }

    private void deleteOnPage(AnnotationInfo annotationInfo) {
        if (mPdfViewCtrl == null) {
            return;
        }

        int pageNum = annotationInfo.getPageNum();
        boolean hasChange = false;
        boolean shouldUnlock = false;
        try {
            // Locks the document first as accessing annotation/doc information isn't thread
            // safe.
            mPdfViewCtrl.docLock(true);
            shouldUnlock = true;
            ArrayList<AnnotationInfo> items = mAnnotationsAdapter.getItemsOnPage(pageNum);
            Page page = mPdfViewCtrl.getDoc().getPage(pageNum);
            for (AnnotationInfo info : items) {
                if (info.getAnnotation() != null) {
                    page.annotRemove(info.getAnnotation());
                    mAnnotationsAdapter.remove(info);
                }
            }
            mPdfViewCtrl.update(true);
            hasChange = mPdfViewCtrl.getDoc().hasChangesSinceSnapshot();
        } catch (Exception e) {
            AnalyticsHandlerAdapter.getInstance().sendException(e);
        } finally {
            if (shouldUnlock) {
                mPdfViewCtrl.docUnlock();
            }
        }

        if (hasChange) {
            ToolManager toolManager = (ToolManager) mPdfViewCtrl.getToolManager();
            if (toolManager != null) {
                toolManager.raiseAnnotationsRemovedEvent(pageNum);
            }
        }

        mAnnotationsAdapter.notifyDataSetChanged();
    }

    private void deleteAll() {
        if (mPdfViewCtrl == null) {
            return;
        }

        boolean hasChange = false;
        boolean shouldUnlock = false;
        try {
            // Locks the document first as accessing annotation/doc information isn't thread
            // safe.
            mPdfViewCtrl.docLock(true);
            shouldUnlock = true;

            AnnotUtils.safeDeleteAllAnnots(mPdfViewCtrl.getDoc());
            mPdfViewCtrl.update(true);
            hasChange = mPdfViewCtrl.getDoc().hasChangesSinceSnapshot();
        } catch (Exception e) {
            AnalyticsHandlerAdapter.getInstance().sendException(e);
        } finally {
            if (shouldUnlock) {
                mPdfViewCtrl.docUnlock();
            }
        }

        if (hasChange) {
            ToolManager toolManager = (ToolManager) mPdfViewCtrl.getToolManager();
            if (toolManager != null) {
                toolManager.raiseAllAnnotationsRemovedEvent();
            }
        }

        mAnnotationsAdapter.clear();
        mAnnotationsAdapter.notifyDataSetChanged();
    }

    /**
     * Called when a toolbar menu item has been clicked.
     *
     * @param item The menu item that was clicked
     */
    public void onToolbarMenuItemClicked(MenuItem item) {

    }

    /**
     * Annotation Info class. Internal use in {@link AnnotationListUtil}
     */
    public static class AnnotationInfo {
        /**
         * The annotation type is one of the types found in com.pdftron.pdf.Annot.
         */
        private int mType;

        /**
         * Holds the page where this annotation is found.
         */
        private int mPageNum;

        /**
         * The contents of the annotation are used in the list view of the
         * BookmarkDialogFragment.
         */
        private String mContent;

        /**
         * The author for this annotation.
         */
        private String mAuthor;

        private Annot mAnnotation;

        /**
         * This date and time info for this annotation
         */
        private String mDate;

        /**
         * Y-position of of the top edge of the {@link Rect} containing this annotation.
         * Obtained from normalized {@link Rect}'s Y2 field.
         */
        private final double mY2;

        /**
         * Default constructor. Creates an empty annotation entry.
         */
        @SuppressWarnings("unused")
        AnnotationInfo() {
            this(0, 0, "", "", "", null, 0);
        }

        /**
         * Class constructor specifying the type, page and content of the
         * annotation.
         *
         * @param type       the type of the annotation
         * @param pageNum    the page where this annotation lies in
         * @param content    the content of the annotation
         * @param author     the author of this annotation
         * @param date       the date
         * @param annotation the annotation
         * @param yPos       y-position of of the top edge of the {@link Rect} containing this annotation.
         * @see <a href="http://www.pdftron.com/pdfnet/mobile/Javadoc/pdftron/PDF/Annot.html">Class Annot</a>
         */
        public AnnotationInfo(int type,
                int pageNum,
                String content,
                String author,
                String date,
                @Nullable Annot annotation,
                double yPos) {
            this.mType = type;
            this.mPageNum = pageNum;
            this.mContent = content;
            this.mAuthor = author;
            this.mDate = date;
            this.mAnnotation = annotation;
            this.mY2 = yPos;
        }

        /**
         * Returns the type of this annotation.
         *
         * @return The type of the annotation
         * @see com.pdftron.pdf.Annot
         */
        public int getType() {
            return mType;
        }

        /**
         * Sets the type of the annotation.
         *
         * @param mType The type of the annotation
         * @see com.pdftron.pdf.Annot
         */
        public void setType(int mType) {
            this.mType = mType;
        }

        /**
         * @return The page number where the annotation is on
         */
        public int getPageNum() {
            return mPageNum;
        }

        /**
         * Sets he page number where the annotation is on.
         *
         * @param mPageNum The page number
         */
        public void setPageNum(int mPageNum) {
            this.mPageNum = mPageNum;
        }

        /**
         * @return The content
         */
        public String getContent() {
            return mContent;
        }

        /**
         * Sets the content.
         *
         * @param mContent The content
         */
        public void setContent(String mContent) {
            this.mContent = mContent;
        }

        /**
         * @return The author
         */
        public String getAuthor() {
            return mAuthor;
        }

        /**
         * Sets the author.
         *
         * @param author The author
         */
        public void setAuthor(String author) {
            this.mAuthor = author;
        }

        /**
         * @return The annotation
         */
        @Nullable
        public Annot getAnnotation() {
            return mAnnotation;
        }

        /**
         * Get date in string format
         *
         * @return Date of the annotation
         */
        public String getDate() {
            return mDate;
        }

        /**
         * Get the Y-position of of the top edge of the {@link Rect} containing this annotation.
         * Obtained from normalized {@link Rect}'s Y2 field.
         *
         * @return y-position of the top edge of the Rect containing this annotation.
         */
        public double getY2() {
            return mY2;
        }
    }

    private class AnnotationsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private static final int STATE_UNKNOWN = 0;
        private static final int STATE_SECTIONED_CELL = 1;
        private static final int STATE_REGULAR_CELL = 2;
        private ArrayList<AnnotationInfo> mAnnotation;
        private int[] mCellStates;

        private RecyclerView.AdapterDataObserver observer = new RecyclerView.AdapterDataObserver() {
            public void onChanged() {
                mCellStates = mAnnotation == null ? null : new int[mAnnotation.size()];
            }
        };

        AnnotationsAdapter(ArrayList<AnnotationInfo> objects) {
            mAnnotation = objects;

            mCellStates = new int[objects.size()];
            registerAdapterDataObserver(observer);
        }

        public void addAll(List<AnnotationInfo> annotationInfos) {
            mAnnotation.addAll(annotationInfos);
            notifyDataSetChanged();
        }

        AnnotationInfo getItem(int position) {
            if (mAnnotation != null && position >= 0 && position < mAnnotation.size()) {
                return mAnnotation.get(position);
            }
            return null;
        }

        ArrayList<AnnotationInfo> getItemsOnPage(int pageNum) {
            ArrayList<AnnotationInfo> list = new ArrayList<>();
            if (mAnnotation != null) {
                for (AnnotationInfo info : mAnnotation) {
                    if (info.getPageNum() == pageNum) {
                        list.add(info);
                    }
                }
                return list;
            }
            return null;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(getContext()).inflate(
                    R.layout.controls_fragment_annotation_listview_item, parent, false);
            return new ViewHolder(view);
        }

        @NonNull
        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

            Context context = getContext();
            if (context == null) {
                return;
            }

            boolean needSeparator = false;
            AnnotationInfo annotationInfo = mAnnotation.get(position);
            if (position < mCellStates.length) {
                switch (mCellStates[position]) {
                    case STATE_SECTIONED_CELL:
                        needSeparator = true;
                        break;
                    case STATE_REGULAR_CELL:
                        needSeparator = false;
                        break;
                    case STATE_UNKNOWN:
                    default:
                        if (position == 0) {
                            needSeparator = true;
                        } else {
                            AnnotationInfo previousAnnotation = mAnnotation.get(position - 1);
                            if (annotationInfo.getPageNum() != previousAnnotation.getPageNum()) {
                                needSeparator = true;
                            }
                        }

                        // Cache the result
                        mCellStates[position] = needSeparator ? STATE_SECTIONED_CELL : STATE_REGULAR_CELL;
                        break;
                }
            }

            ViewHolder viewHolder = (ViewHolder) holder;

            if (needSeparator) {
                viewHolder.separator.setText(String.format(getString(R.string.controls_annotation_dialog_page), annotationInfo.getPageNum()));
                viewHolder.separator.setVisibility(View.VISIBLE);
            } else {
                viewHolder.separator.setVisibility(View.GONE);
            }
            String content = annotationInfo.getContent();
            if (Utils.isNullOrEmpty(content)) {
                viewHolder.line1.setVisibility(View.GONE);
            } else {
                viewHolder.line1.setText(annotationInfo.getContent());
                viewHolder.line1.setVisibility(View.VISIBLE);
            }

            // Set icon based on the annotation type
            viewHolder.icon.setImageResource(AnnotUtils.getAnnotImageResId(annotationInfo.getType()));

            StringBuilder descBuilder = new StringBuilder();
            if (PdfViewCtrlSettingsManager.getAnnotListShowAuthor(context)) {
                String author = annotationInfo.getAuthor();
                if (!author.isEmpty()) {
                    descBuilder.append(author).append(", ");
                }
            }
            descBuilder.append(annotationInfo.getDate());
            viewHolder.line2.setText(descBuilder.toString());

            // set author and date
            Annot annot = annotationInfo.getAnnotation();

            int color = AnnotUtils.getAnnotColor(annot);
            if (color == -1) {
                color = Color.BLACK;
            }
            viewHolder.icon.setColorFilter(color);
            viewHolder.icon.setAlpha(AnnotUtils.getAnnotOpacity(annot));
        }

        @Override
        public int getItemCount() {
            if (mAnnotation != null) {
                return mAnnotation.size();
            } else {
                return 0;
            }
        }

        public void clear() {
            mAnnotation.clear();
        }

        public boolean remove(AnnotationInfo annotInfo) {
            return mAnnotation.remove(annotInfo);
        }

        private class ViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {
            TextView separator;
            TextView line1;
            TextView line2;
            ImageView icon;

            public ViewHolder(View itemView) {
                super(itemView);
                separator = itemView.findViewById(R.id.textview_annotation_recyclerview_item_separator);
                icon = itemView.findViewById(R.id.imageview_annotation_recyclerview_item);
                line1 = itemView.findViewById(R.id.textview_annotation_recyclerview_item);
                line2 = itemView.findViewById(R.id.textview_desc_recyclerview_item);
                if (!mIsReadOnly) {
                    itemView.setOnCreateContextMenuListener(this);
                }
            }

            @Override
            public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
                final int position = mRecyclerView.getChildAdapterPosition(view);
                AnnotationInfo item = mAnnotationsAdapter.getItem(position);
                if (item != null) {
                    String title = String.format(getString(R.string.controls_annotation_dialog_page), item.getPageNum());
                    String author = item.getAuthor();
                    if (!Utils.isNullOrEmpty(author)) {
                        title = title + " " + getString(R.string.controls_annotation_dialog_author) + " " + author;
                    }
                    menu.setHeaderTitle(title);
                }
                String[] menuItems = getResources().getStringArray(R.array.annotation_dialog_context_menu);
                menu.add(Menu.NONE, CONTEXT_MENU_DELETE_ITEM, CONTEXT_MENU_DELETE_ITEM, menuItems[CONTEXT_MENU_DELETE_ITEM]);
                String deleteOnPage = menuItems[CONTEXT_MENU_DELETE_ITEM_ON_PAGE];
                if (item != null) {
                    deleteOnPage = deleteOnPage + " " + item.getPageNum();
                }
                menu.add(Menu.NONE, CONTEXT_MENU_DELETE_ITEM_ON_PAGE, CONTEXT_MENU_DELETE_ITEM_ON_PAGE, deleteOnPage);
                menu.add(Menu.NONE, CONTEXT_MENU_DELETE_ALL, CONTEXT_MENU_DELETE_ALL, menuItems[CONTEXT_MENU_DELETE_ALL]);
                MenuItem.OnMenuItemClickListener listener = new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        onContextMenuItemClicked(item, position);
                        return true;
                    }
                };
                menu.getItem(CONTEXT_MENU_DELETE_ITEM).setOnMenuItemClickListener(listener);
                menu.getItem(CONTEXT_MENU_DELETE_ITEM_ON_PAGE).setOnMenuItemClickListener(listener);
                menu.getItem(CONTEXT_MENU_DELETE_ALL).setOnMenuItemClickListener(listener);
            }

            void onContextMenuItemClicked(MenuItem item, int position) {
                int menuItemIndex = item.getItemId();
                switch (menuItemIndex) {
                    case CONTEXT_MENU_DELETE_ITEM:
                        AnnotationInfo annotationInfo = mAnnotationsAdapter.getItem(position);
                        if (annotationInfo == null || mPdfViewCtrl == null) {
                            return;
                        }
                        Annot annot = annotationInfo.getAnnotation();
                        if (annot != null) {
                            int annotPageNum = annotationInfo.getPageNum();
                            HashMap<Annot, Integer> annots = new HashMap<>(1);
                            annots.put(annot, annotPageNum);
                            ToolManager toolManager = (ToolManager) mPdfViewCtrl.getToolManager();
                            boolean shouldUnlock = false;
                            try {
                                // Locks the document first as accessing annotation/doc information isn't thread
                                // safe.
                                mPdfViewCtrl.docLock(true);
                                shouldUnlock = true;
                                if (toolManager != null) {
                                    toolManager.raiseAnnotationsPreRemoveEvent(annots);
                                }

                                Page page = mPdfViewCtrl.getDoc().getPage(annotPageNum);
                                page.annotRemove(annot);
                                mPdfViewCtrl.update(annot, annotPageNum);

                                // make sure to raise remove event after mPdfViewCtrl.update
                                if (toolManager != null) {
                                    toolManager.raiseAnnotationsRemovedEvent(annots);
                                }

                                mAnnotationsAdapter.remove(mAnnotationsAdapter.getItem(position));
                                mAnnotationsAdapter.notifyDataSetChanged();
                            } catch (Exception e) {
                                AnalyticsHandlerAdapter.getInstance().sendException(e);
                            } finally {
                                if (shouldUnlock) {
                                    mPdfViewCtrl.docUnlock();
                                }
                            }
                        }
                        onEventAction();
                        AnalyticsHandlerAdapter.getInstance().sendEvent(AnalyticsHandlerAdapter.EVENT_ANNOTATIONS_LIST,
                                AnalyticsParam.annotationsListActionParam(AnalyticsHandlerAdapter.ANNOTATIONS_LIST_DELETE));
                        break;
                    case CONTEXT_MENU_DELETE_ITEM_ON_PAGE:
                        annotationInfo = mAnnotationsAdapter.getItem(position);
                        if (annotationInfo != null && annotationInfo.getAnnotation() != null) {
                            deleteOnPage(annotationInfo);
                        }
                        onEventAction();
                        AnalyticsHandlerAdapter.getInstance().sendEvent(AnalyticsHandlerAdapter.EVENT_ANNOTATIONS_LIST,
                                AnalyticsParam.annotationsListActionParam(AnalyticsHandlerAdapter.ANNOTATIONS_LIST_DELETE_ALL_ON_PAGE));
                        break;
                    case CONTEXT_MENU_DELETE_ALL:
                        deleteAll();
                        onEventAction();
                        AnalyticsHandlerAdapter.getInstance().sendEvent(AnalyticsHandlerAdapter.EVENT_ANNOTATIONS_LIST,
                                AnalyticsParam.annotationsListActionParam(AnalyticsHandlerAdapter.ANNOTATIONS_LIST_DELETE_ALL_IN_DOC));
                        break;
                }
            }
        }
    }
}