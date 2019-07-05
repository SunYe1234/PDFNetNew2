package com.pdftron.collab.db.entity;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import com.pdftron.collab.model.LastAnnotation;

/**
 * Immutable model class for a last annotation, this table only contains a single entry with key "xfdf"
 */
@Entity(tableName = "last_annotation_table")
public class LastAnnotationEntity implements LastAnnotation {

    @PrimaryKey
    @NonNull
    private String id;
    @NonNull
    private String xfdf;

    public LastAnnotationEntity(@NonNull String id, @NonNull String xfdf) {
        this.id = id;
        this.xfdf = xfdf;
    }

    /**
     * Gets the key which is "xfdf"
     */
    @Override
    public String getId() {
        return this.id;
    }

    /**
     * Gets the last annotation XFDF
     */
    @Override
    public String getXfdf() {
        return this.xfdf;
    }
}
