package com.pdftron.collab.ui.annotlist.model.list.item;

import android.support.annotation.NonNull;

/**
 * Immutable data class representing a list header item displayed in the annotation list.
 */
public class AnnotationListHeader<T extends AnnotationListHeader.HeaderData> implements AnnotationListItem {

    protected final T mHeaderKey;

    public AnnotationListHeader(@NonNull T headerKey) {
        mHeaderKey = headerKey;
    }

    public T getHeaderKey() {
        return mHeaderKey;
    }

    @Override
    public boolean isHeader() {
        return true;
    }

    public String getHeaderString() {
        return mHeaderKey.getHeaderTitle();
    }

    public interface HeaderData {
        String getHeaderTitle();
    }
}
