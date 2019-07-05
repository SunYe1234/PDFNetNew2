package com.pdftron.collab.ui.reply.bottomsheet.view;

import android.content.Context;
import android.os.Parcel;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.pdftron.collab.R;
import com.pdftron.collab.ui.reply.model.ReplyMessage;
import com.pdftron.collab.ui.reply.model.User;

/**
 * A {@link AvatarAdapter} that displays a circle and user initials styled avatar.
 */
public class InitialsAvatarAdapter implements AvatarAdapter {

    @Override
    public void onInflateAvatar(@NonNull FrameLayout avatarContainer) {
        LayoutInflater.from(avatarContainer.getContext()).inflate(R.layout.item_reply_message_avatar, avatarContainer, true);
    }

    @SuppressWarnings("RedundantCast")
    @Override
    public void onBindAvatar(@NonNull FrameLayout avatarContainer, @NonNull ReplyMessage replyMessage) {
        ((TextView) avatarContainer.findViewById(R.id.user_icon_text))
                .setText(replyMessage.getIcon());
        ((ImageView) avatarContainer.findViewById(R.id.user_icon_background))
                .setColorFilter(
                        getBackgroundColor(avatarContainer.getContext(), replyMessage.getUser())
                );
    }

    @ColorInt
    private int getBackgroundColor(@NonNull Context context, @NonNull User user) {
        final String userId = user.getId();
        final int[] colors = context.getResources().getIntArray(R.array.avatar_colors);
        return colors[Math.abs(userId.hashCode()) % colors.length];
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }

    public InitialsAvatarAdapter() {
    }

    /**
     * {@hide}
     */
    @SuppressWarnings("WeakerAccess")
    protected InitialsAvatarAdapter(Parcel in) {
    }

    public static final Creator<InitialsAvatarAdapter> CREATOR = new Creator<InitialsAvatarAdapter>() {
        @Override
        public InitialsAvatarAdapter createFromParcel(Parcel source) {
            return new InitialsAvatarAdapter(source);
        }

        @Override
        public InitialsAvatarAdapter[] newArray(int size) {
            return new InitialsAvatarAdapter[size];
        }
    };
}
