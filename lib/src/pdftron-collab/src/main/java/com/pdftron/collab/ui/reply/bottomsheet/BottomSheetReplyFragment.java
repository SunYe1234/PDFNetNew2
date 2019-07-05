package com.pdftron.collab.ui.reply.bottomsheet;

import android.app.Activity;
import android.app.Dialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.pdftron.collab.R;
import com.pdftron.collab.ui.reply.bottomsheet.view.AvatarAdapter;
import com.pdftron.collab.ui.reply.component.ReplyUIViewModel;
import com.pdftron.pdf.utils.AnalyticsHandlerAdapter;

import io.reactivex.disposables.CompositeDisposable;

/**
 * A {@link android.support.design.widget.BottomSheetDialogFragment} that contains a
 * {@link ReplyFragment}.
 * <p>
 * Recommended to use {@link BottomSheetReplyFragmentBuilder} to create this Fragment. However if you're
 * instantiating using the constructor, you will need to pass in the following arguments:
 * <ul>
 * <li>Valid document ID with key {@link ReplyFragment#BUNDLE_ANNOTATION_ID_KEY}</li>
 * <li>Valid annotation ID with key {@link ReplyFragment#BUNDLE_ANNOTATION_ID_KEY}</li>
 * <li>Valid user ID with key {@link ReplyFragment#BUNDLE_USER_ID_KEY}</li>
 * </ul>
 */
public class BottomSheetReplyFragment extends DialogFragment {

    public static final String TAG = BottomSheetReplyFragment.class.getName();

    @StyleRes
    private int mBottomSheetTheme;
    @Nullable
    private String mAnnotationId;
    @Nullable
    private String mDocumentId;
    @Nullable
    private String mUserId;
    @Nullable
    private AvatarAdapter mAvatarAdapter;

    private CompositeDisposable mDisposables = new CompositeDisposable();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            mAnnotationId = args.getString(ReplyFragment.BUNDLE_ANNOTATION_ID_KEY);
            mDocumentId = args.getString(ReplyFragment.BUNDLE_DOCUMENT_ID_KEY);
            mUserId = args.getString(ReplyFragment.BUNDLE_USER_ID_KEY);
            mBottomSheetTheme = args.getInt(ReplyFragment.BUNDLE_THEME_ID_KEY);
            mAvatarAdapter = args.getParcelable(ReplyFragment.BUNDLE_AVATAR_ADAPTER_KEY);
        }

        if (args == null || mAnnotationId == null || mDocumentId == null || mUserId == null || mBottomSheetTheme == 0) {
            throw new IllegalStateException("BottomSheetReplyFragment requires annotation set in the bundle");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return LayoutInflater.from(new ContextThemeWrapper(getContext(), mBottomSheetTheme))
                .inflate(R.layout.fragment_collab_bottom_sheet_reply, container, false);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Activity activity = getActivity();
        assert (activity != null);
        NonDimmedBottomSheetDialog dialog = new NonDimmedBottomSheetDialog(activity, R.style.BottomSheetReplyStyle);
        dialog.setOnShowListener(dialogInterface -> {
            // Frame layout from bottom sheet dialog layout
            FrameLayout bottomSheet = dialog.findViewById(android.support.design.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                bottomSheet.setBackgroundColor(Color.TRANSPARENT);
                bottomSheet.setClipChildren(false);
                // Customize the peek height of this bottom sheet behavior
                BottomSheetBehavior behavior = BottomSheetBehavior.from(bottomSheet);
                behavior.setPeekHeight(bottomSheet.getHeight());
                behavior.setHideable(false);
            }
        });
        return dialog;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FragmentActivity activity = getActivity();

        if (activity == null || mAnnotationId == null) {
            return;
        }

        // Set up the view model for this component
        ReplyUIViewModel viewModel = ViewModelProviders.of(activity).get(ReplyUIViewModel.class);
        //
        // Listen to UI events, and react to user interactions when close button is pressed.
        //
        mDisposables.add(
                viewModel.getHeaderObservable().subscribe(headerEvent -> {
                    switch (headerEvent.getEventType()) {
                        case LIST_CLICKED:
                        case CLOSE_CLICKED: {
                            this.dismiss();
                            break;
                        }
                    }
                }, throwable -> {
                    throw new RuntimeException(throwable);
                })
        );

        // Inflate the reply fragment
        if (mAnnotationId != null && mDocumentId != null && mUserId != null) {
            ReplyFragment fragment = ReplyFragmentBuilder.withAnnot(mDocumentId, mAnnotationId, mUserId)
                    .usingTheme(mBottomSheetTheme)
                    .usingAdapter(mAvatarAdapter)
                    .build(activity);

            getChildFragmentManager()
                    .beginTransaction()
                    .replace(R.id.bottom_sheet_reply_container, fragment)
                    .commit();
        } else {
            AnalyticsHandlerAdapter.getInstance()
                    .sendException(new IllegalStateException("BottomSheetReplyFragment requires " +
                            "annotation id, document id, and user id set in the bundle"));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mDisposables.clear();
    }

    private static class NonDimmedBottomSheetDialog extends BottomSheetDialog {

        NonDimmedBottomSheetDialog(@NonNull Context context, int theme) {
            super(context, theme);
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if (getWindow() != null) {
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            }
        }
    }
}
