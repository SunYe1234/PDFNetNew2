package com.pdftron.collab.ui.viewer;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.pdftron.collab.R;
import com.pdftron.collab.db.entity.LastAnnotationEntity;
import com.pdftron.collab.ui.annotlist.component.AnnotationListViewModel;
import com.pdftron.collab.ui.annotlist.model.list.item.AnnotationListContent;
import com.pdftron.collab.ui.reply.bottomsheet.BottomSheetReplyFragment;
import com.pdftron.collab.ui.reply.bottomsheet.ReplyFragmentBuilder;
import com.pdftron.collab.ui.reply.component.ReplyUIViewModel;
import com.pdftron.collab.ui.reply.model.ReplyHeader;
import com.pdftron.collab.ui.reply.model.ReplyInput;
import com.pdftron.collab.ui.reply.model.ReplyMessage;
import com.pdftron.collab.ui.reply.model.ReplyMessages;
import com.pdftron.collab.ui.reply.model.User;
import com.pdftron.collab.ui.view.NotificationThumbnailSlider;
import com.pdftron.collab.viewmodel.AnnotationViewModel;
import com.pdftron.collab.viewmodel.DocumentViewModel;
import com.pdftron.common.PDFNetException;
import com.pdftron.pdf.Annot;
import com.pdftron.pdf.config.ToolConfig;
import com.pdftron.pdf.controls.AnnotStyleDialogFragment;
import com.pdftron.pdf.controls.PdfViewCtrlTabFragment;
import com.pdftron.pdf.tools.AnnotManager;
import com.pdftron.pdf.tools.QuickMenu;
import com.pdftron.pdf.tools.QuickMenuItem;
import com.pdftron.pdf.tools.Tool;
import com.pdftron.pdf.tools.ToolManager;
import com.pdftron.pdf.utils.AnalyticsHandlerAdapter;
import com.pdftron.pdf.utils.AnnotUtils;
import com.pdftron.pdf.utils.CommonToast;
import com.pdftron.pdf.utils.Logger;
import com.pdftron.pdf.utils.Utils;
import com.pdftron.pdf.utils.ViewerUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

import io.reactivex.disposables.CompositeDisposable;

/**
 * A {@link PdfViewCtrlTabFragment} that has real-time annotation collaboration functionality.
 * Implements a bottom sheet reply fragment that allows for real-time comments on annotations
 */
public class CollabViewerTabFragment extends PdfViewCtrlTabFragment {

    private static final String TAG = CollabViewerTabFragment.class.getName();

    public static final String BUNDLE_REPLY_THEME = "bundle_tab_reply_style";

    @StyleRes
    protected int mReplyTheme;
    protected String mDocumentId;

    @Nullable
    protected DocumentViewModel mDocumentViewModel;
    @Nullable
    protected AnnotationViewModel mAnnotationViewModel;
    @Nullable
    protected ReplyUIViewModel mReplyUiViewModel;

    private NotificationThumbnailSlider mThumbnailSlider;
    private boolean mInitialAnnotsMerged = false;

    @Nullable
    private OpenAnnotationListListener mOpenAnnotListListener;

    protected final CompositeDisposable mDisposables = new CompositeDisposable();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBookmarkDialogCurrentTab = 2;
        Bundle bundle = getArguments();
        if (bundle == null) {
            throw new NullPointerException("bundle cannot be null");
        }

        mContentLayout = bundle.getInt(BUNDLE_TAB_CONTENT_LAYOUT, getContentLayout());

        FragmentActivity activity = getActivity();
        if (activity == null) {
            return;
        }

        setUpReply(activity);
        setUpAnnotList(activity);
        mReplyTheme = bundle.getInt(BUNDLE_REPLY_THEME);

        if (mReplyTheme == 0) {// if not set from bundle...
            TypedArray a = null;
            try { // ...then try to find a styles attribute in the activity theme
                a = activity.obtainStyledAttributes(R.styleable.BaseCollabViewer);
                mReplyTheme = a.getResourceId(R.styleable.BaseCollabViewer_replyTheme,
                        R.style.ReplyBaseTheme_DayNight); // ...otherwise, use default theme
            } catch (Resources.NotFoundException e) {
                mReplyTheme = R.style.ReplyBaseTheme_DayNight;
            } finally {
                if (a != null) {
                    a.recycle();
                }
            }
        }
    }

    @LayoutRes
    protected int getContentLayout() {
        return R.layout.controls_collab_fragment_tabbed_pdfviewctrl_tab_content;
    }

    /**
     * Called when the reply fragment needs to be shown for a specific
     * annotation. Only called if the Fragment's lifecycle state is at least
     * {@link Lifecycle.State#STARTED}.
     *
     * @param selectedAnnotId      id of the selected annotation
     * @param authorId             author of the selected annotation
     * @param selectedAnnotPageNum page number of the selected annotation
     */
    @SuppressWarnings("unused")
    protected void showReplyFragment(@NonNull String selectedAnnotId,
            @NonNull String authorId, int selectedAnnotPageNum) {

        FragmentActivity activity = getActivity();
        if (activity != null) {
            // Show bottom sheet
            BottomSheetReplyFragment fragment =
                    ReplyFragmentBuilder
                            .withAnnot(mDocumentId, selectedAnnotId, authorId)
                            .usingTheme(mReplyTheme)
                            .asBottomSheet()
                            .build(activity, BottomSheetReplyFragment.class);

            // Reselect the annotation when bottom sheet is dismissed
            fragment.getViewLifecycleOwnerLiveData()
                    .observe(this, new Observer<LifecycleOwner>() {
                        @Override
                        public void onChanged(@Nullable LifecycleOwner lifecycleOwner) {
                            if (lifecycleOwner == null) { // null when bottom sheet is destroyed (i.e. after onDestroyView)
                                getToolManager().reselectAnnot();
                                fragment.getViewLifecycleOwnerLiveData().removeObserver(this); // just to be safe, manually remove this
                            }
                        }
                    });

            fragment.show(activity.getSupportFragmentManager(), BottomSheetReplyFragment.TAG);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = super.onCreateView(inflater, container, savedInstanceState);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mDisposables.clear();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mToolManager.disableToolMode(new ToolManager.ToolMode[]{
                ToolManager.ToolMode.FORM_FILL,
                ToolManager.ToolMode.FORM_CHECKBOX_CREATE,
                ToolManager.ToolMode.FORM_SIGNATURE_CREATE,
                ToolManager.ToolMode.FORM_TEXT_FIELD_CREATE,
                ToolManager.ToolMode.FILE_ATTACHMENT_CREATE,
                ToolManager.ToolMode.SOUND_CREATE,
                ToolManager.ToolMode.RECT_LINK,
                ToolManager.ToolMode.TEXT_LINK_CREATE,
                ToolManager.ToolMode.ANNOT_EDIT_RECT_GROUP
        });
        mToolManager.setCopyAnnotatedTextToNoteEnabled(true);
        mToolManager.setStickyNoteShowPopup(false);

        mThumbnailSlider = view.findViewById(R.id.thumbseekbar);
    }

    @Override
    protected void loadPDFViewCtrlView() {
        super.loadPDFViewCtrlView();
        ToolConfig.getInstance().removeQMHideItem(R.id.qm_note);
    }

    @Override
    public void onDocumentLoaded() {
        initializeCollaboration();
        super.onDocumentLoaded();
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        boolean handled = false;
        mAnnotationSelected = false;
        int x = (int) (e.getX() + 0.5);
        int y = (int) (e.getY() + 0.5);
        // don't show sticky note popup
        boolean shouldUnlockRead = false;
        try {
            mPdfViewCtrl.docLockRead();
            shouldUnlockRead = true;
            Annot annot = mPdfViewCtrl.getAnnotationAt(x, y);
            int page = mPdfViewCtrl.getPageNumberFromScreenPt(x, y);
            if (annot != null && annot.isValid()) {
                mAnnotationSelected = true;
                if (annot.getType() == Annot.e_Text && !AnnotUtils.hasInReplyTo(annot)) {
                    // this is a sticky note
                    handled = true;
                    mToolManager.selectAnnot(annot, page);
                }
            }
        } catch (Exception ex) {
            AnalyticsHandlerAdapter.getInstance().sendException(ex);
        } finally {
            if (shouldUnlockRead) {
                mPdfViewCtrl.docUnlockRead();
            }
        }
        if (!handled) {
            handled = super.onSingleTapConfirmed(e);
        }
        if (!mAnnotationSelected) {
            mToolManager.setQuickMenuJustClosed(false);
        }
        return handled;
    }

    @Override
    public boolean onQuickMenuClicked(QuickMenuItem menuItem) {
        boolean result = super.onQuickMenuClicked(menuItem);
        if (menuItem.getItemId() == R.id.qm_note) {
            if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
                showReplyComponent();
                return true;
            }
        }
        return result;
    }

    @Override
    public boolean onShowQuickMenu(QuickMenu quickMenu, Annot annot) {
        boolean result = super.onShowQuickMenu(quickMenu, annot);
        Context context = getContext();
        if (annot != null && quickMenu != null && context != null) {

            // Remove copy button in quick menu
            QuickMenuItem copyQmItem = new QuickMenuItem(context, R.id.qm_copy, QuickMenuItem.FIRST_ROW_MENU);
            quickMenu.removeMenuEntries(Collections.singletonList(copyQmItem));

            try {
                // Add note button for free text
                if (annot.getType() == Annot.e_FreeText) {
                    QuickMenuItem noteQmItem = new QuickMenuItem(context, R.id.qm_note, QuickMenuItem.FIRST_ROW_MENU);
                    noteQmItem.setTitle(R.string.tools_qm_note);
                    noteQmItem.setIcon(R.drawable.ic_annotation_sticky_note_black_24dp);
                    noteQmItem.setOrder(QuickMenuItem.ORDER_START);
                    quickMenu.addMenuEntries(Collections.singletonList(noteQmItem));
                }
            } catch (PDFNetException e) {
                AnalyticsHandlerAdapter.getInstance().sendException(e);
            }
        }
        return result;
    }

    /**
     * Set listener for opening annotation list. Should be implemented by
     * {@link CollabViewerTabHostFragment}
     *
     * @param listener with callbacks for opening the annotation list
     */
    public void setOpenAnnotationListListener(OpenAnnotationListListener listener) {
        mOpenAnnotListListener = listener;
    }

    private void setUpReply(@NonNull FragmentActivity activity) {

        mReplyUiViewModel = ViewModelProviders.of(activity).get(ReplyUIViewModel.class);

        // Observe header UI events
        mDisposables.add(
                mReplyUiViewModel.getHeaderObservable()
                        .subscribe(headerEvent -> {
                            switch (headerEvent.getEventType()) {
                                case LIST_CLICKED: {
                                    if (mOpenAnnotListListener != null) {
                                        mOpenAnnotListListener.openAnnotationList();
                                    }
                                    break;
                                }
                            }
                        }, throwable -> AnalyticsHandlerAdapter.getInstance().sendException(new RuntimeException(throwable)))
        );

        // Observe message list UI events
        mDisposables.add(
                mReplyUiViewModel.getMessagesObservable()
                        .subscribe(messageEvent -> {
                            switch (messageEvent.getEventType()) {
                                case MESSAGE_DELETE_CLICKED: {
                                    ReplyMessage data = messageEvent.getData();
                                    String replyId = data.getReplyId();
                                    int page = data.getPage();
                                    removeReply(replyId, page);
                                    break;
                                }
                            }
                        }, throwable -> AnalyticsHandlerAdapter.getInstance().sendException(new RuntimeException(throwable)))
        );

        // Observe send message events
        mDisposables.add(
                mReplyUiViewModel.getWriteMessageObservable()
                        .subscribe(inputEvent -> {
                            switch (inputEvent.getEventType()) {
                                case MESSAGE_WRITE_FINISHED: {
                                    ReplyInput replyInput = mReplyUiViewModel.getWriteMessageLiveData().getValue();
                                    if (replyInput != null) {
                                        sendReply(replyInput.getMessage().getContent().getContentString());
                                    }
                                    break;
                                }
                                case MESSAGE_EDIT_FINISHED: {
                                    ReplyInput replyInput = mReplyUiViewModel.getEditMessageLiveData().getValue();
                                    if (replyInput != null) {
                                        ReplyMessage message = replyInput.getMessage();
                                        String replyId = message.getReplyId();
                                        String newMessage = message.getContent().getContentString();
                                        int pageNum = message.getPage();
                                        editReply(replyId, pageNum, newMessage);
                                    }
                                    break;
                                }
                            }
                        }, throwable -> AnalyticsHandlerAdapter.getInstance().sendException(new RuntimeException(throwable)))
        );
    }

    private void setUpAnnotList(@NonNull FragmentActivity activity) {
        // Handle annotation list click events by jumping to the selected annotation
        // and toggling the reply bottom sheet
        AnnotationListViewModel annotListViewModel = ViewModelProviders.of(activity).get(AnnotationListViewModel.class);
        mDisposables.add(
                annotListViewModel.getAnnotationListObservable()
                        .subscribe(annotationListEvent -> {
                            switch (annotationListEvent.getEventType()) {
                                case ANNOTATION_ITEM_CLICKED: {
                                    AnnotationListContent annotContent = annotationListEvent.getData();
                                    Annot annot = annotContent.getAnnotation();
                                    int pageNum = annotContent.getPageNum();
                                    ToolManager toolManager = getToolManager();

                                    if (annot != null && mPdfViewCtrl != null && toolManager != null) {
                                        // Might be selected, when annot list button is clicked from reply fragment,
                                        // so deselect all first
                                        toolManager.deselectAll();
                                        // Show annotation jump animation
                                        ViewerUtils.jumpToAnnotation(mPdfViewCtrl, annot, pageNum);

                                        // Then show the bottom sheet for that annotation
                                        toolManager.selectAnnot(annot, pageNum);
                                        ((Tool) toolManager.getTool()).closeQuickMenu();
                                        if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
                                            showReplyComponent();
                                        }
                                    }
                                    break;
                                }
                            }
                        }, throwable -> AnalyticsHandlerAdapter.getInstance().sendException(new RuntimeException(throwable)))
        );
    }

    private void showReplyComponent() {
        // Get the parameters to initialize the bottom sheet state
        FragmentActivity activity = getActivity();
        int selectedAnnotPageNum = getToolManager().getSelectedAnnotPageNum();
        String selectedAnnotId = getToolManager().getSelectedAnnotId();
        Annot selectedAnnot = ViewerUtils.getAnnotById(mPdfViewCtrl,
                selectedAnnotId,
                selectedAnnotPageNum
        );
        if (activity != null) {
            if (selectedAnnot != null && mReplyUiViewModel != null) {
                // Set up the view model for UI
                String authorId = getToolManager().getAuthorId();
                String authorName = getToolManager().getAuthorName();
                ReplyHeader initialHeader = new ReplyHeader(activity, selectedAnnot, false);
                ReplyMessages initialreplyMessages = new ReplyMessages();
                User user = new User(authorId, authorName);
                mReplyUiViewModel.set(initialHeader, initialreplyMessages, user, selectedAnnotPageNum);

                if (mDocumentId == null) {
                    Logger.INSTANCE.LogD(TAG, "Document is not ready for collab.");
                    return;
                }

                showReplyFragment(selectedAnnotId, authorId, selectedAnnotPageNum);
            } else {
                CommonToast.showText(activity, getString(R.string.toast_no_selected_annot), Toast.LENGTH_SHORT);
                Logger.INSTANCE.LogD(TAG, "Could not get selected annotation");
            }
        }
    }

    private void sendReply(String contents) {
        if (Utils.isNullOrEmpty(contents)) {
            return;
        }
        try {
            Annot reply = AnnotUtils.createAnnotationReply(
                    getToolManager().getSelectedAnnotId(),
                    getToolManager().getSelectedAnnotPageNum(),
                    getPDFViewCtrl(),
                    getToolManager().getAuthorId(),
                    contents
            );
            HashMap<Annot, Integer> annots = new HashMap<>(1);
            annots.put(reply, getToolManager().getSelectedAnnotPageNum());
            getToolManager().raiseAnnotationsAddedEvent(annots);
        } catch (Exception ex) {
            AnalyticsHandlerAdapter.getInstance().sendException(ex);
        }
    }

    private void editReply(String replyId, int page, String contents) {
        try {
            AnnotUtils.updateAnnotationReply(replyId, page, getPDFViewCtrl(), getToolManager(), contents);
        } catch (Exception ex) {
            AnalyticsHandlerAdapter.getInstance().sendException(ex);
        }
    }

    private void removeReply(String replyId, int page) {
        try {
            AnnotUtils.deleteAnnotationReply(replyId, page, getPDFViewCtrl(), getToolManager());
        } catch (Exception ex) {
            AnalyticsHandlerAdapter.getInstance().sendException(ex);
        }
    }

    @Nullable
    String getDocumentId() {
        return mDocumentId;
    }

    /**
     * Called when the document is downloaded and need to initialize for collaboration.
     */
    protected void initializeCollaboration() {
        FragmentActivity activity = getActivity();
        if (activity == null) {
            return;
        }
        if (mDocumentViewModel != null) {
            return;
        }
        LifecycleOwner lifecycleOwner = getViewLifecycleOwner();

        // Setup view models
        mDocumentViewModel = ViewModelProviders.of(this).get(DocumentViewModel.class);
        mDocumentViewModel.getUser().observe(
                lifecycleOwner,
                userEntity -> {
                    if (userEntity != null) {
                        enableCollab(userEntity.getId(), userEntity.getName());
                    }
                }
        );

        mDocumentViewModel.getDocument().observe(
                lifecycleOwner,
                documentEntity -> {
                    if (documentEntity != null) {
                        if (!documentEntity.getId().equals(mDocumentId)) {
                            mDocumentId = documentEntity.getId();
                            mAnnotationViewModel =
                                    ViewModelProviders.of(CollabViewerTabFragment.this,
                                            new AnnotationViewModel.Factory(
                                                    getActivity().getApplication(),
                                                    documentEntity.getId()
                                            )
                                    ).get(AnnotationViewModel.class);
                            setupAnnotation(lifecycleOwner);
                        }
                    }
                }
        );

        mDisposables.add(
                mDocumentViewModel.getUnreadObservable(lifecycleOwner)
                        .subscribe(hasUnread -> mThumbnailSlider.setIsNotificationShown(hasUnread),
                                throwable ->
                                        AnalyticsHandlerAdapter.getInstance()
                                                .sendException(new RuntimeException(throwable))
                        )
        );
    }

    private void setupAnnotation(LifecycleOwner lifecycleOwner) {
        Objects.requireNonNull(mAnnotationViewModel);
        mAnnotationViewModel.getLastAnnotations().observe(lifecycleOwner, lastAnnotationEntities -> {
            if (lastAnnotationEntities != null) {
                if (lastAnnotationEntities.size() > 0) {
                    Logger.INSTANCE.LogD(TAG, "lastAnnots: " + lastAnnotationEntities.size());
                    for (LastAnnotationEntity item : lastAnnotationEntities) {
                        if (mInitialAnnotsMerged) {
                            Logger.INSTANCE.LogD(TAG, "lastAnnot: " + item.getXfdf());
                        }
                        safeOnRemoteChange(item.getXfdf());
                    }
                    if (!mInitialAnnotsMerged) {
                        try {
                            mPdfViewCtrl.update(true);
                        } catch (Exception ignored) {}
                    }
                    mInitialAnnotsMerged = true;
                }
            }
        });
    }

    private void safeOnRemoteChange(String xfdfString) {
        ToolManager toolManager = getToolManager();
        AnnotManager annotManager = toolManager != null ? getToolManager().getAnnotManager() : null;
        if (annotManager != null) {
            String selectedId = toolManager.getSelectedAnnotId();
            boolean modifySelected = selectedId != null && xfdfString.contains(selectedId);
            if (xfdfString.contains("<delete>") && modifySelected && mInitialAnnotsMerged) {
                toolManager.deselectAll();
            }
            toolManager.getAnnotManager().onRemoteChange(xfdfString);
            Logger.INSTANCE.LogD(TAG, "done lastAnnot merge");
            if (xfdfString.contains("<modify>") && modifySelected && mInitialAnnotsMerged) {
                FragmentActivity activity = getActivity();
                boolean canShow = true;
                if (activity != null) {
                    Fragment annotStyleDialog = activity.getSupportFragmentManager().findFragmentByTag(AnnotStyleDialogFragment.TAG);
                    canShow = annotStyleDialog == null;
                }
                if (canShow) {
                    // re-select is not needed for style changes, only position/size changes
                    toolManager.reselectAnnot();
                }
            }
        }
    }

    private void enableCollab(String userId, String userName) {
        ToolManager toolManager = getToolManager();
        if (toolManager != null) {
            if (toolManager.getAnnotManager() != null) {
                return;
            }
            toolManager.enableAnnotManager(
                    userId,
                    userName,
                    (action, xfdfCommand, xfdfJSON) -> {
                        if (mAnnotationViewModel != null) {
                            mAnnotationViewModel.sendAnnotation(action, xfdfJSON, userName);
                        }
                    }
            );
            toolManager.setExternalAnnotationManagerListener(() -> UUID.randomUUID().toString());
        }
    }
}
