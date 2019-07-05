package com.pdftron.collab.ui.reply.component;

import android.arch.lifecycle.LifecycleOwner;
import android.support.annotation.NonNull;
import android.view.ViewGroup;

import com.pdftron.collab.ui.base.component.BaseUIComponent;
import com.pdftron.collab.ui.reply.bottomsheet.view.ReplyMessagesUIView;
import com.pdftron.collab.ui.reply.component.messages.BaseMessagesUIView;
import com.pdftron.collab.ui.reply.component.messages.MessageEvent;

import io.reactivex.subjects.PublishSubject;

public class ReplyMessagesUIComponent extends BaseUIComponent<BaseMessagesUIView, MessageEvent, ReplyUIViewModel> {

    public ReplyMessagesUIComponent(@NonNull ViewGroup parent,
            @NonNull LifecycleOwner lifecycleOwner,
            @NonNull ReplyUIViewModel model,
            @NonNull PublishSubject<MessageEvent> observable,
            @NonNull BaseMessagesUIView.BaseMessagesAdapter adapter) {
        super(parent, lifecycleOwner, model, observable);
        mView.setMessagesAdapter(adapter);
        setupComponent(lifecycleOwner, model);
    }

    @NonNull
    @Override
    protected BaseMessagesUIView inflateUIView(@NonNull ViewGroup parent) {
        return new ReplyMessagesUIView(parent);
    }

    private void setupComponent(@NonNull LifecycleOwner lifecycleOwner,
            @NonNull ReplyUIViewModel model) {

        // This initializes the view state, and also updates the view on data changes
        model.getMessagesLiveData().observe(lifecycleOwner, replyMessages -> {
            if (replyMessages != null) {
                mView.setMessages(replyMessages);
            }
        });
    }
}
