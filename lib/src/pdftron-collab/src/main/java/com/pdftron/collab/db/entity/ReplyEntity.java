package com.pdftron.collab.db.entity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.pdftron.collab.model.Reply;

import java.util.Date;

/**
 * Immutable model class for an annotation reply
 */
@Entity(tableName = "reply_table", primaryKeys = {"id", "in_reply_to"})
public class ReplyEntity implements Reply {

    @NonNull
    private String id;
    @NonNull
    @ColumnInfo(name = "author_id")
    private String authorId;
    @Nullable
    @ColumnInfo(name = "author_name")
    private String authorName;
    @NonNull
    @ColumnInfo(name = "in_reply_to")
    private String inReplyTo;
    @NonNull
    private String contents;
    @NonNull
    @ColumnInfo(name = "creation_date")
    private Date creationDate;
    @NonNull
    private Date date;
    private int page;

    /**
     * Sets the unique identifier of the reply
     */
    public void setId(@NonNull String id) {
        this.id = id;
    }

    /**
     * Sets the author id of the reply
     */
    public void setAuthorId(@NonNull String authorId) {
        this.authorId = authorId;
    }

    /**
     * Sets the author name of the reply
     */
    public void setAuthorName(@Nullable String authorName) {
        this.authorName = authorName;
    }

    /**
     * Sets the annotation id that the reply is associated with
     */
    public void setInReplyTo(@NonNull String inReplyTo) {
        this.inReplyTo = inReplyTo;
    }

    /**
     * Sets the reply contents
     */
    public void setContents(@NonNull String contents) {
        this.contents = contents;
    }

    /**
     * Sets the creation date
     */
    public void setCreationDate(@NonNull Date creationDate) {
        this.creationDate = creationDate;
    }

    /**
     * Sets the date
     */
    public void setDate(@NonNull Date date) {
        this.date = date;
    }

    /**
     * Sets the page
     */
    public void setPage(int page) {
        this.page = page;
    }

    /**
     * Gets the unique identifier of the reply
     */
    @NonNull
    @Override
    public String getId() {
        return this.id;
    }

    /**
     * Gets the author id of the reply
     */
    @NonNull
    @Override
    public String getAuthorId() {
        return this.authorId;
    }

    /**
     * Gets the author name of the reply
     */
    @Override
    public String getAuthorName() {
        return this.authorName;
    }

    /**
     * Gets the annotation id that the reply is associated with
     */
    @NonNull
    @Override
    public String getInReplyTo() {
        return this.inReplyTo;
    }

    /**
     * Gets the contents
     */
    @NonNull
    @Override
    public String getContents() {
        return this.contents;
    }

    /**
     * Gets the creation date
     */
    @NonNull
    @Override
    public Date getCreationDate() {
        return this.creationDate;
    }

    /**
     * Gets the date
     */
    @NonNull
    @Override
    public Date getDate() {
        return this.date;
    }

    /**
     * Gets the page
     */
    @Override
    public int getPage() {
        return page;
    }
}
