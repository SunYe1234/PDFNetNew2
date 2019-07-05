package com.pdftron.collab.ui.annotlist;

import android.support.annotation.RestrictTo;

import com.pdftron.pdf.dialog.annotlist.AnnotationListSortOrder;
import com.pdftron.pdf.dialog.annotlist.BaseAnnotationSortOrder;

/**
 * Defines supported sort ordering for the annotation list
 */
public enum CollabAnnotationListSortOrder implements BaseAnnotationSortOrder {
    DATE_DESCENDING(1),     // sort by date created
    POSITION_ASCENDING(2),  // sort by y-position per page
    LAST_ACTIVITY(3);       // sort by most recent activity

    public final int value;

    CollabAnnotationListSortOrder(int i) {
        value = i;
    }

    @Override
    public int getValue() {
        return value;
    }

    @Override
    public String getType() {
        return AnnotationListSortOrder.class.getName();
    }

    @Override
    public String toString() {
        return Integer.toString(value);
    }

    /**
     * @hide
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public static CollabAnnotationListSortOrder fromValue(int value) {
        for (CollabAnnotationListSortOrder annotationListSortOrder : CollabAnnotationListSortOrder.values()) {
            if (annotationListSortOrder.value == value)
                return annotationListSortOrder;
        }
        return LAST_ACTIVITY; // default sort mode if it doesn't exist
    }
}
