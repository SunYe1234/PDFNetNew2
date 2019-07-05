package com.pdftron.pdf.dialog.annotlist;

import android.support.annotation.RestrictTo;

import com.pdftron.pdf.controls.AnnotationDialogFragment;

/**
 * Sort order used in {@link AnnotationDialogFragment}.
 */
public enum AnnotationListSortOrder implements BaseAnnotationSortOrder {
    POSITION_ASCENDING(1),
    DATE_ASCENDING(2);

    public final int value;

    AnnotationListSortOrder(int i) {
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
    public static AnnotationListSortOrder fromValue(int value) {
        for (AnnotationListSortOrder annotationListSortOrder : AnnotationListSortOrder.values()) {
            if (annotationListSortOrder.value == value)
                return annotationListSortOrder;
        }
        return DATE_ASCENDING; // default sort mode if it doesn't exist
    }
}
