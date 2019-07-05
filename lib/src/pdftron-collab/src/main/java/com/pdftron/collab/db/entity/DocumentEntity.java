package com.pdftron.collab.db.entity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.pdftron.collab.model.Document;

/**
 * Immutable model class for a document
 */
@Entity(tableName = "document_table")
public class DocumentEntity implements Document {

    @PrimaryKey
    @NonNull
    private String id;
    @Nullable
    @ColumnInfo(name = "share_id")
    private String shareId;
    @NonNull
    private Long date;
    @Nullable
    private String unreads;

    public DocumentEntity(@NonNull String id, @Nullable String shareId,
            @NonNull Long date, @Nullable String unreads) {
        this.id = id;
        this.shareId = shareId;
        this.date = date;
        this.unreads = unreads;
    }

    /**
     * Gets the unique identifier of the document
     */
    @NonNull
    @Override
    public String getId() {
        return this.id;
    }

    /**
     * Gets the share id of the document (only relevant for WebViewer Server)
     */
    @Nullable
    @Override
    public String getShareId() {
        return this.shareId;
    }

    @NonNull
    @Override
    public Long getDate() {
        return this.date;
    }

    /**
     * Gets list of unread annotation ids
     */
    @Nullable
    @Override
    public String getUnreads() {
        return this.unreads;
    }

    @NonNull
    @Override
    public String toString() {
        return "getId:" + getId() + "\n" +
                "getShareId:" + getShareId() + "\n" +
                "getDate:" + getDate() + "\n" +
                "getUnreads:" + getUnreads() + "\n";
    }
}
