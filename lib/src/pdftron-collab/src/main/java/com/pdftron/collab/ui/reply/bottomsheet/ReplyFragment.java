package com.pdftron.collab.ui.reply.bottomsheet;

import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.pdftron.collab.R;
import com.pdftron.collab.ui.reply.bottomsheet.view.AvatarAdapter;
import com.pdftron.collab.ui.reply.bottomsheet.view.ReplyMessagesUIView;
import com.pdftron.collab.ui.reply.component.ReplyEntityMapper;
import com.pdftron.collab.ui.reply.component.ReplyHeaderUIComponent;
import com.pdftron.collab.ui.reply.component.ReplyInputUIComponent;
import com.pdftron.collab.ui.reply.component.ReplyMessagesUIComponent;
import com.pdftron.collab.ui.reply.component.ReplyUIViewModel;
import com.pdftron.collab.ui.reply.component.messages.BaseMessagesUIView;
import com.pdftron.collab.ui.reply.component.messages.MessageEvent;
import com.pdftron.collab.utils.date.ReplyDateFormat;
import com.pdftron.collab.viewmodel.DocumentViewModel;
import com.pdftron.collab.viewmodel.ReplyViewModel;
import com.pdftron.pdf.utils.AnalyticsHandlerAdapter;
import com.pdftron.pdf.utils.Logger;

import java.util.Objects;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

/**
 * A Fragment that displays the specified annotations comments and annotation contents if
 * available. Also allows the current user to create replies, or edit and delete their own comments.
 * <p>
 * Recommended to use {@link ReplyFragmentBuilder} to create this Fragment. However if you're
 * instantiating using the constructor, you will need to pass in the following arguments:
 * <ul>
 * <li>Valid document ID with key {@link ReplyFragment#BUNDLE_ANNOTATION_ID_KEY}</li>
 * <li>Valid annotation ID with key {@link ReplyFragment#BUNDLE_ANNOTATION_ID_KEY}</li>
 * <li>Valid user ID with key {@link ReplyFragment#BUNDLE_USER_ID_KEY}</li>
 * </ul>
 */
public class ReplyFragment extends Fragment {

    public static final String TAG = ReplyFragment.class.getName();
    public static final String BUNDLE_DOCUMENT_ID_KEY = "ReplyFragment_Document_id";
    public static final String BUNDLE_ANNOTATION_ID_KEY = "ReplyFragment_Annotation_id";
    public static final String BUNDLE_USER_ID_KEY = "ReplyFragment_User_id";
    public static final String BUNDLE_THEME_ID_KEY = "ReplyFragment_theme_id";
    public static final String BUNDLE_AVATAR_ADAPTER_KEY = "ReplyFragment_avatar_adapter_id";

    @Nullable
    private DocumentViewModel mDocumentViewModel;

    @Nullable
    private String mAnnotationId;
    @Nullable
    private String mDocumentId;
    @Nullable
    private String mUserId;
    @StyleRes
    private int mBottomSheetTheme;
    @Nullable
    private AvatarAdapter mAvatarAdapter;

    private final CompositeDisposable mDisposables = new CompositeDisposable();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            mAnnotationId = args.getString(BUNDLE_ANNOTATION_ID_KEY);
            mDocumentId = args.getString(BUNDLE_DOCUMENT_ID_KEY);
            mUserId = args.getString(BUNDLE_USER_ID_KEY);
            mBottomSheetTheme = args.getInt(BUNDLE_THEME_ID_KEY, R.style.ReplyBaseTheme_DayNight);
            mAvatarAdapter = args.getParcelable(BUNDLE_AVATAR_ADAPTER_KEY);
        }

        if (args == null || mAnnotationId == null || mDocumentId == null || mUserId == null) {
            throw new IllegalStateException("BottomSheetReplyFragment requires annotation set in the bundle");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return LayoutInflater.from(new ContextThemeWrapper(getContext(), mBottomSheetTheme))
                .inflate(R.layout.fragment_collab_reply, container, false);
    }

    @SuppressWarnings("unused")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FragmentActivity activity = getActivity();

        if (activity == null || mAnnotationId == null) {
            return;
        }

        // Set up the view model for this component
        ReplyUIViewModel viewModel = ViewModelProviders.of(activity).get(ReplyUIViewModel.class);

        // Observe the reply view model to update messages
        LifecycleOwner lifecycleOwner = getViewLifecycleOwner();
        setupReply(activity, viewModel, lifecycleOwner, mAnnotationId);

        // Observe whether there are unread annotations in the document view model
        setupNotifications(viewModel, this);

        // Set up the UI components
        LinearLayout headerContainer = view.findViewById(R.id.header_container);
        FrameLayout messagesContainer = view.findViewById(R.id.messages_container);
        FrameLayout editorContainer = view.findViewById(R.id.editor_container);

        // Attach views to linear layout in the order they appear
        ReplyHeaderUIComponent headerComp =
                getHeaderUIComponent(viewModel,
                        lifecycleOwner,
                        headerContainer
                );

        ReplyMessagesUIComponent messagesComp =
                getMessagesUIComponent(viewModel,
                        lifecycleOwner,
                        messagesContainer,
                        getReplyMessageAdapter(activity,
                                viewModel.getMessagesObservable()
                        )
                );

        ReplyInputUIComponent inputComp =
                getInputUIComponent(viewModel,
                        lifecycleOwner,
                        editorContainer
                );

        updateUnread();
    }

    @NonNull
    private ReplyInputUIComponent getInputUIComponent(@NonNull ReplyUIViewModel viewModel,
            @NonNull LifecycleOwner lifecycleOwner,
            @NonNull FrameLayout editorContainer) {
        return new ReplyInputUIComponent(
                editorContainer,
                lifecycleOwner,
                viewModel,
                viewModel.getWriteMessageObservable()
        );
    }

    @NonNull
    private ReplyMessagesUIComponent getMessagesUIComponent(@NonNull ReplyUIViewModel viewModel,
            @NonNull LifecycleOwner lifecycleOwner,
            @NonNull FrameLayout messagesContainer,
            @NonNull BaseMessagesUIView.BaseMessagesAdapter messagesAdapter) {
        return new ReplyMessagesUIComponent(
                messagesContainer,
                lifecycleOwner,
                viewModel,
                viewModel.getMessagesObservable(),
                messagesAdapter
        );
    }

    @NonNull
    private ReplyHeaderUIComponent getHeaderUIComponent(@NonNull ReplyUIViewModel viewModel,
            @NonNull LifecycleOwner lifecycleOwner,
            @NonNull LinearLayout headerContainer) {
        return new ReplyHeaderUIComponent(
                headerContainer,
                lifecycleOwner,
                viewModel,
                viewModel.getHeaderObservable()
        );
    }

    @NonNull
    protected BaseMessagesUIView.BaseMessagesAdapter getReplyMessageAdapter(
            @NonNull Context context,
            @NonNull PublishSubject<MessageEvent> messageEventSubject) {
        return new ReplyMessagesUIView.MessageAdapter(
                ReplyDateFormat.newInstance(context),
                messageEventSubject,
                Objects.requireNonNull(mAvatarAdapter)
        );
    }

    @Override
    public void onStop() {
        if (mDocumentViewModel != null) {
            setActiveReply(mDocumentViewModel, null); // need to do this before the bottom sheet is actually dismissed
        }
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mDocumentViewModel != null) {
            setActiveReply(mDocumentViewModel, null);
        }
        mDisposables.clear();
    }

    private void setupNotifications(@NonNull ReplyUIViewModel uiViewModel, @NonNull LifecycleOwner lifecycleOwner) {
        DocumentViewModel documentViewModel = ViewModelProviders.of(this).get(DocumentViewModel.class);
        mDisposables.add(
                documentViewModel.getUnreadObservable(lifecycleOwner)
                        .subscribe(uiViewModel::setHasUnreadReplies,
                                throwable -> AnalyticsHandlerAdapter.getInstance().sendException(new RuntimeException(throwable)))
        );
    }

    private void updateUnread() {
        mDocumentViewModel = ViewModelProviders.of(this).get(DocumentViewModel.class);
        // update last read
        mDisposables.add(mDocumentViewModel.updateUnreadAnnotations(mDocumentId, mAnnotationId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                            // success no op?
                        }, throwable ->
                                AnalyticsHandlerAdapter.getInstance()
                                        .sendException(new RuntimeException(throwable))
                )
        );
        setActiveReply(mDocumentViewModel, mAnnotationId);
    }

    private void setActiveReply(@NonNull DocumentViewModel viewModel, @Nullable String which) {
        mDisposables.add(
                viewModel.updateActiveAnnotation(mUserId, which)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(() -> {
                                    // success no op?
                                }, throwable -> AnalyticsHandlerAdapter.getInstance()
                                        .sendException(new RuntimeException(throwable))
                        )
        );
    }

    private void setupReply(@NonNull FragmentActivity activity,
            @NonNull final ReplyUIViewModel uiViewModel,
            @NonNull LifecycleOwner lifecycleOwner,
            @NonNull String annotationId) {
        // Get the reply view model
        ReplyViewModel replyViewModel = ViewModelProviders.of(ReplyFragment.this,
                new ReplyViewModel.Factory(
                        activity.getApplication(),
                        annotationId
                )
        ).get(ReplyViewModel.class);

        // Observe for reply list changes
        replyViewModel.getReplies().observe(lifecycleOwner, replyEntities -> {
            if (replyEntities != null) {

                Logger.INSTANCE.LogD(TAG, "replies: " + replyEntities.size());
                if (replyEntities.size() > 0) {
                    Logger.INSTANCE.LogD(TAG, "last reply:" + replyEntities.get(replyEntities.size() - 1).getContents());
                }
                uiViewModel.setMessages(ReplyEntityMapper.fromEntities(replyEntities, uiViewModel.getUser()));
            }
        });
    }
}
