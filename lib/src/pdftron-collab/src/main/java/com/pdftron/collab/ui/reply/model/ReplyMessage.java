package com.pdftron.collab.ui.reply.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Date;
import java.util.Objects;

/**
 * View state model representing a single reply message in the reply message list.
 */
public class ReplyMessage {

    @Nullable
    private final String replyId; // null reply id means that this is a new reply message
    @NonNull
    private final String icon;
    @NonNull
    private final User user;
    @NonNull
    private final ReplyContent replyContent;
    @NonNull
    private final Date timestamp;

    private final int page;

    private final boolean isEditable;

    public ReplyMessage(@Nullable String replyId,
            @NonNull User user,
            @NonNull ReplyContent reply,
            @NonNull Date timestamp,
            @NonNull String icon,
            int page,
            boolean isEditable) {
        this.replyId = replyId;
        this.user = user;
        this.replyContent = reply;
        this.timestamp = timestamp;
        this.icon = icon;
        this.page = page;
        this.isEditable = isEditable;
    }

    public boolean isEditable() {
        return isEditable;
    }

    public int getPage() {
        return page;
    }

    @Nullable
    public String getReplyId() {
        return replyId;
    }

    @NonNull
    public User getUser() {
        return user;
    }

    @NonNull
    public ReplyContent getContent() {
        return replyContent;
    }

    @NonNull
    public Date getTimestamp() {
        return timestamp;
    }

    @NonNull
    public String getIcon() {
        return icon;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReplyMessage that = (ReplyMessage) o;
        return page == that.page &&
                isEditable == that.isEditable &&
                Objects.equals(replyId, that.replyId) &&
                icon.equals(that.icon) &&
                user.equals(that.user) &&
                replyContent.equals(that.replyContent) &&
                timestamp.equals(that.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(replyId, icon, user, replyContent, timestamp, page, isEditable);
    }
}
