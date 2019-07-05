package com.pdftron.collab.ui.annotlist.model.list;

import android.support.annotation.RestrictTo;

import com.pdftron.collab.ui.annotlist.model.list.item.AnnotationListItem;

/**
 * Base class representing an list of Annotations.
 * {@hide}
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public abstract class AnnotationList {

    /**
     * Returns the {@link AnnotationListItem} at the specified position.
     *
     * @param position position to grab the {@link AnnotationListItem}
     * @return the {@link AnnotationListItem}
     */
    public abstract AnnotationListItem get(int position);

    /**
     * @return size of the list of annotations.
     */
    public abstract int size();
}
