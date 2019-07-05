package com.pdftron.collab.model;

/**
 * Interface for a document
 */
public interface Document {
    String getId();
    String getShareId();
    Long getDate();
    String getUnreads();
}
