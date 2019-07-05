package com.pdftron.collab.ui.reply.component;

import android.arch.lifecycle.LifecycleOwner;
import android.support.annotation.NonNull;
import android.view.ViewGroup;

import com.pdftron.collab.ui.base.component.BaseUIComponent;
import com.pdftron.collab.ui.reply.bottomsheet.view.ReplyHeaderUIView;
import com.pdftron.collab.ui.reply.component.header.BaseHeaderUIView;
import com.pdftron.collab.ui.reply.component.header.HeaderEvent;
import com.pdftron.pdf.utils.Utils;

import io.reactivex.subjects.PublishSubject;
/**
 * A {@link BaseUIComponent} representing the reply header. Responsible for updating remote
 * changes from {@link ReplyUIViewModel} to the {@link BaseHeaderUIView}. Currently only the
 * notification icon updated due to remote changes as the LiveData is only updated
 * for incoming notifications.
 */
public class ReplyHeaderUIComponent extends BaseUIComponent<BaseHeaderUIView, HeaderEvent, ReplyUIViewModel> {

    public ReplyHeaderUIComponent(@NonNull ViewGroup parent,
            @NonNull LifecycleOwner lifecycleOwner,
            @NonNull ReplyUIViewModel model,
            @NonNull PublishSubject<HeaderEvent> headerObservable) {
        super(parent, lifecycleOwner, model, headerObservable);
        setupComponent(lifecycleOwner, model);
    }

    @NonNull
    @Override
    protected BaseHeaderUIView inflateUIView(@NonNull ViewGroup parent) {
        return new ReplyHeaderUIView(parent);
    }

    private void setupComponent(@NonNull LifecycleOwner lifecycleOwner,
            @NonNull ReplyUIViewModel model) {
        // This initializes the view state, and also updates the view on data changes
        model.getHeaderLiveData().observe(lifecycleOwner, replyHeader -> {
            if (replyHeader != null) {
                mView.setHeaderTitle(replyHeader.getTitle());
                mView.setNotificationIcon(replyHeader.hasUnreadReplies());
                if (!Utils.isNullOrEmpty(replyHeader.getPreviewContent())) {
                    mView.showPreviewHeader();
                    mView.setAnnotationPreviewText(replyHeader.getPreviewContent());
                    mView.setAnnotationPreviewIcon(replyHeader.getPreviewIcon(),
                            replyHeader.getPreviewIconColor(),
                            replyHeader.getPreviewIconOpacity());
                } else {
                    mView.hidePreviewHeader();
                }
            }
        });
    }
}
