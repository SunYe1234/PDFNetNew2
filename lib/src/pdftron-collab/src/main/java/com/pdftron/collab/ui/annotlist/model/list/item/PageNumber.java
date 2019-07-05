package com.pdftron.collab.ui.annotlist.model.list.item;

import android.support.annotation.NonNull;

/**
 * Page number header data, that is held by a {@link AnnotationListHeader}.
 */
public class PageNumber implements Comparable<PageNumber>, AnnotationListHeader.HeaderData {
    private final int page;
    private final String format;

    public PageNumber(int page, @NonNull String format) {
        this.page = page;
        this.format = format;
    }

    @Override
    public String getHeaderTitle() {
        return String.format(format, page);
    }

    @Override
    public int compareTo(PageNumber that) {
        return Integer.compare(this.page, that.page);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PageNumber that = (PageNumber) o;
        return page == that.page;
    }

    @Override
    public int hashCode() {
        return page;
    }
}
