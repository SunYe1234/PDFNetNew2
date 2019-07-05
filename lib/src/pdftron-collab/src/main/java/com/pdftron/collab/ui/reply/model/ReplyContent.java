package com.pdftron.collab.ui.reply.model;
/**
 * View state model representing a content within a reply message.
 */
public interface ReplyContent {

    /**
     * @return the string contents of this reply content
     */
    String getContentString();

    /**
     * @return the content type of this reply content
     */
    ContentType getContentType();

    public enum ContentType {
        STRING_MESSAGE
    }
}
