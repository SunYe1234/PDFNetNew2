package com.pdftron.collab.ui.reply.component.messages;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.pdftron.collab.R;
import com.pdftron.collab.ui.base.component.BaseUIView;
import com.pdftron.collab.ui.reply.bottomsheet.view.ReplyMessagesUIView;
import com.pdftron.collab.ui.reply.model.ReplyMessage;
import com.pdftron.collab.ui.reply.model.ReplyMessages;

import java.util.List;

import io.reactivex.subjects.PublishSubject;

/**
 * {@hide}
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public abstract class BaseMessagesUIView extends BaseUIView<MessageEvent> {

    @Nullable
    protected BaseMessagesAdapter mBaseMessageAdapter;

    public BaseMessagesUIView(@NonNull ViewGroup parent) {
        super(parent);
    }

    public void setMessagesAdapter(@NonNull BaseMessagesAdapter adapter) {
        mBaseMessageAdapter = adapter;
    }

    public abstract void setMessages(@NonNull ReplyMessages replyMessages);

    /**
     * A {@link RecyclerView.Adapter< ReplyMessagesUIView.MessageViewHolder>} that contains information
     * on inflating and initializing views in the
     */
    public static abstract class BaseMessagesAdapter extends RecyclerView.Adapter<ReplyMessagesUIView.MessageViewHolder> {

        @NonNull
        private final PublishSubject<MessageEvent> mEventObservable;

        protected BaseMessagesAdapter(@NonNull PublishSubject<MessageEvent> eventObservable) {
            mEventObservable = eventObservable;
        }

        public abstract void setMessages(List<ReplyMessage> messages);

        protected void onMessageDeleteClicked(@Nullable ReplyMessage data) {
            mEventObservable.onNext(new MessageEvent(MessageEvent.Type.MESSAGE_DELETE_CLICKED, data));
        }

        protected void onMessageEditClicked(@Nullable ReplyMessage data) {
            mEventObservable.onNext(new MessageEvent(MessageEvent.Type.MESSAGE_EDIT_CLICKED, data));
        }
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {

        private final FrameLayout mAvatarContainer;
        private final TextView mUsername;
        private final TextView mTimestamp;
        private final TextView mMessage;
        private final ImageView mMore;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            mAvatarContainer = itemView.findViewById(R.id.user_avatar_container);
            mUsername = itemView.findViewById(R.id.user_name);
            mTimestamp = itemView.findViewById(R.id.time_stamp);
            mMessage = itemView.findViewById(R.id.message);
            mMore = itemView.findViewById(R.id.more);
        }

        public FrameLayout getAvatarContainer() {
            return mAvatarContainer;
        }

        public TextView getUsername() {
            return mUsername;
        }

        public TextView getTimestamp() {
            return mTimestamp;
        }

        public TextView getMessage() {
            return mMessage;
        }

        public ImageView getMoreButton() {
            return mMore;
        }
    }
}
