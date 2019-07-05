package com.pdftron.collab.model;

import java.util.Date;

/**
 * Interface for an annotation
 */
public interface Annotation {
    String getId();
    String getDocumentId();
    String getAuthorId();
    String getAuthorName();
    String getParent();
    String getInReplyTo();
    String getXfdf();
    String getContents();
    String getAt();
    Date getCreationDate();
    Date getDate();
    double getYPos();
    int getPage();
    int getType();
    int getColor();
    float getOpacity();
    String getLastReplyAuthor();
    String getLastReplyContents();
    Date getLastReplyDate();
    int getUnreadCount();
    String getUnreads();
}
