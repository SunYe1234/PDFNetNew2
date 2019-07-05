package com.pdftron.collab.ui.reply.model;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * View state model representing reply messages/comments.
 */
public class ReplyMessages {

    // Assumes that the oldest message first and the newest message is last.
    // We could possibly sort the reply messages according to date, in order to keep this invariant.
    @NonNull
    private final List<ReplyMessage> mMessages;

    /**
     * Creates a {@link ReplyMessages} object with existing messages.
     *
     * @param messages used to initialize this object
     */
    public ReplyMessages(@NonNull List<ReplyMessage> messages) {
        mMessages = Collections.unmodifiableList(messages);
    }

    /**
     * Creates a {@link ReplyMessages} object with no messages.
     */
    public ReplyMessages() {
        mMessages = new ArrayList<>();
    }

    /**
     * @return an unmodifiable list containing the reply messages
     */
    @NonNull
    public List<ReplyMessage> getMessages() {
        return mMessages;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReplyMessages that = (ReplyMessages) o;
        return Objects.equals(mMessages, that.mMessages);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mMessages);
    }
}
