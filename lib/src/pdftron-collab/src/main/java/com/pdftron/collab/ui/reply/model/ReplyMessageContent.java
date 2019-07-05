package com.pdftron.collab.ui.reply.model;

import android.support.annotation.NonNull;

import java.util.Objects;

/**
 * View state model representing the content of each reply comment/message.
 */
public class ReplyMessageContent implements ReplyContent {

    private final String content;

    public ReplyMessageContent(@NonNull String content) {
        this.content = content;
    }

    @NonNull
    @Override
    public String getContentString() {
        return content;
    }

    @Override
    public ContentType getContentType() {
        return ContentType.STRING_MESSAGE;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReplyMessageContent that = (ReplyMessageContent) o;
        return Objects.equals(content, that.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(content);
    }
}
