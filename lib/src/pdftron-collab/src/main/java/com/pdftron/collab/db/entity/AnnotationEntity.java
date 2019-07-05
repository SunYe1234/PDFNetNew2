package com.pdftron.collab.db.entity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.pdftron.collab.model.Annotation;

import java.util.Date;

/**
 * Immutable model class for an annotation
 */
@Entity(tableName = "annotation_table", primaryKeys = {"id", "document_id"})
public class AnnotationEntity implements Annotation {

    @NonNull
    private String id;
    @NonNull
    @ColumnInfo(name = "document_id")
    private String documentId;
    @NonNull
    @ColumnInfo(name = "author_id")
    private String authorId;
    @Nullable
    @ColumnInfo(name = "author_name")
    private String authorName;
    @Nullable
    private String parent;
    @Nullable
    @ColumnInfo(name = "in_reply_to")
    private String inReplyTo;
    @NonNull
    private String xfdf;
    @Nullable
    private String contents;
    @Nullable
    private String at;
    @NonNull
    @ColumnInfo(name = "creation_date")
    private Date creationDate;
    @NonNull
    private Date date;
    @Nullable
    @ColumnInfo(name = "last_reply_author")
    private String lastReplyAuthor;
    @Nullable
    @ColumnInfo(name = "last_reply_contents")
    private String lastReplyContents;
    @Nullable
    @ColumnInfo(name = "last_reply_date")
    private Date lastReplyDate;
    @ColumnInfo(name = "y_pos")
    private double yPos;
    private int page;
    private int type;
    private int color;
    private float opacity;
    @ColumnInfo(name = "unread_count")
    private int unreadCount;
    @Nullable
    private String unreads;

    /**
     * Sets the unique identifier of the annotation
     */
    public void setId(@NonNull String id) {
        this.id = id;
    }

    /**
     * Sets the unique identifier of the document
     */
    public void setDocumentId(@NonNull String documentId) {
        this.documentId = documentId;
    }

    /**
     * Sets the unique identifier of the author
     */
    public void setAuthorId(@NonNull String authorId) {
        this.authorId = authorId;
    }

    /**
     * Sets the author name
     */
    public void setAuthorName(@Nullable String authorName) {
        this.authorName = authorName;
    }

    public void setParent(@Nullable String parent) {
        this.parent = parent;
    }

    /**
     * Sets the IRT of this annotation
     */
    public void setInReplyTo(@Nullable String inReplyTo) {
        this.inReplyTo = inReplyTo;
    }

    /**
     * Sets the XFDF of this annotation
     */
    public void setXfdf(@NonNull String xfdf) {
        this.xfdf = xfdf;
    }

    /**
     * Sets the contents
     */
    public void setContents(@Nullable String contents) {
        this.contents = contents;
    }

    public void setAt(@Nullable String at) {
        this.at = at;
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
     * Sets the last reply author
     */
    public void setLastReplyAuthor(@Nullable String lastReplyAuthor) {
        this.lastReplyAuthor = lastReplyAuthor;
    }

    /**
     * Sets the last reply contents
     */
    public void setLastReplyContents(@Nullable String lastReplyContents) {
        this.lastReplyContents = lastReplyContents;
    }

    /**
     * Sets the last reply date
     */
    public void setLastReplyDate(@Nullable Date lastReplyDate) {
        this.lastReplyDate = lastReplyDate;
    }

    /**
     * Sets the y position
     */
    public void setYPos(double yPos) {
        this.yPos = yPos;
    }

    /**
     * Sets the page the annotation is on
     */
    public void setPage(int page) {
        this.page = page;
    }

    /**
     * Sets the annotation type
     */
    public void setType(int type) {
        this.type = type;
    }

    /**
     * Sets the tint color to be shown in annotation list
     */
    public void setColor(int color) {
        this.color = color;
    }

    /**
     * Sets the opacity to be shown in annotation list
     */
    public void setOpacity(float opacity) {
        this.opacity = opacity;
    }

    /**
     * Sets the unread reply count
     */
    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }

    /**
     * Sets the unread reply IDs
     */
    public void setUnreads(String unreads) {
        this.unreads = unreads;
    }

    /**
     * Gets the unique identifier of the annotation
     */
    @Override
    public String getId() {
        return this.id;
    }

    /**
     * Gets the unique identifier of the document
     */
    @Override
    public String getDocumentId() {
        return this.documentId;
    }

    /**
     * Gets the unique identifier of the author
     */
    @Override
    public String getAuthorId() {
        return this.authorId;
    }

    /**
     * Gets the author name
     */
    @Override
    public String getAuthorName() {
        return this.authorName;
    }

    @Override
    public String getParent() {
        return this.parent;
    }

    /**
     * Gets the IRT of this annotation
     */
    @Override
    public String getInReplyTo() {
        return this.inReplyTo;
    }

    /**
     * Gets the XFDF of this annotation
     */
    @Override
    public String getXfdf() {
        return this.xfdf;
    }

    /**
     * Gets the contents
     */
    @Override
    public String getContents() {
        return contents;
    }

    @Override
    public String getAt() {
        return this.at;
    }

    /**
     * Gets the creation date
     */
    @Override
    public Date getCreationDate() {
        return this.creationDate;
    }

    /**
     * Gets the date
     */
    @Override
    public Date getDate() {
        return this.date;
    }

    /**
     * Gets the last reply date
     */
    @Nullable
    @Override
    public Date getLastReplyDate() {
        return this.lastReplyDate;
    }

    /**
     * Gets the unread reply count
     */
    @Override
    public int getUnreadCount() {
        return this.unreadCount;
    }

    /**
     * Gets the unread reply IDs
     */
    @Nullable
    @Override
    public String getUnreads() {
        return this.unreads;
    }

    /**
     * Gets the y position
     */
    @Override
    public double getYPos() {
        return this.yPos;
    }

    /**
     * Gets the page the annotation is on
     */
    @Override
    public int getPage() {
        return this.page;
    }

    /**
     * Gets the annotation type
     */
    @Override
    public int getType() {
        return this.type;
    }

    /**
     * Gets the tint color to be shown in annotation list
     */
    @Override
    public int getColor() {
        return this.color;
    }

    /**
     * Gets the opacity to be shown in annotation list
     */
    @Override
    public float getOpacity() {
        return this.opacity;
    }

    /**
     * Gets the last reply author
     */
    @Nullable
    @Override
    public String getLastReplyAuthor() {
        return this.lastReplyAuthor;
    }

    /**
     * Gets the last reply contents
     */
    @Nullable
    @Override
    public String getLastReplyContents() {
        return this.lastReplyContents;
    }

    @NonNull
    @Override
    public String toString() {
        return "getId:" + getId() + "\n" +
                "getDocumentId:" + getDocumentId() + "\n" +
                "getAuthorId:" + getAuthorId() + "\n" +
                "getAuthorName:" + getAuthorName() + "\n" +
                "getParent:" + getParent() + "\n" +
                "getInReplyTo:" + getInReplyTo() + "\n" +
                "getCreationDate:" + getCreationDate() + "\n" +
                "getDate:" + getDate() + "\n" +
                "getYPos:" + getYPos() + "\n" +
                "getColor:" + getColor() + "\n" +
                "getOpacity:" + getOpacity() + "\n" +
                "getPage:" + getPage() + "\n" +
                "getType:" + getType() + "\n";
    }
}
