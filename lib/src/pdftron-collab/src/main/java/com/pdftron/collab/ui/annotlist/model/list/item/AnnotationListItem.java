package com.pdftron.collab.ui.annotlist.model.list.item;

/**
 * An annotation list item can either be a header or some annotation content.
 */
public interface AnnotationListItem {
    int LAYOUT_HEADER = 0;
    int LAYOUT_CONTENT = 1;

    /**
     * @return true if this annotation list item is a header
     */
    boolean isHeader();
}
