package com.pdftron.collab.ui.reply.bottomsheet.view;

import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.widget.FrameLayout;

import com.pdftron.collab.ui.reply.model.ReplyMessage;

/**
 * Adapter to inflate and bind the avatar icon in the messages list.
 */
public interface AvatarAdapter extends Parcelable {
    /**
     * Inflates a avatar icon into the containing {@link FrameLayout}
     *
     * @param avatarContainer {@link FrameLayout} to inflate avatar icon view.
     */
    void onInflateAvatar(@NonNull FrameLayout avatarContainer);

    /**
     * Binds data to the avatar icon views.
     *
     * @param avatarContainer root view group containing the previously inflated avatar icon view.
     * @param replyMessage      data for the message item
     */
    void onBindAvatar(@NonNull FrameLayout avatarContainer, @NonNull ReplyMessage replyMessage);
}
