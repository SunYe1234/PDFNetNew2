package com.pdftron.collab.model;

import java.util.Date;

/**
 * Interface for an annotation reply
 */
public interface Reply {
    String getId();
    String getAuthorId();
    String getAuthorName();
    String getInReplyTo();
    String getContents();
    Date getCreationDate();
    Date getDate();
    int getPage();
}
