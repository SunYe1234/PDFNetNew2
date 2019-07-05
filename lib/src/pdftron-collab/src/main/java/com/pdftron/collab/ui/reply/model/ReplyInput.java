package com.pdftron.collab.ui.reply.model;

import android.support.annotation.NonNull;

import java.util.Objects;

/**
 * View state model representing the content of a reply input field.
 */
public class ReplyInput {

    private final ReplyMessage message;

    public ReplyInput(@NonNull ReplyMessage message) {
        this.message = message;
    }

    @NonNull
    public ReplyMessage getMessage() {
        return message;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReplyInput replyInput = (ReplyInput) o;
        return Objects.equals(message, replyInput.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(message);
    }
}
