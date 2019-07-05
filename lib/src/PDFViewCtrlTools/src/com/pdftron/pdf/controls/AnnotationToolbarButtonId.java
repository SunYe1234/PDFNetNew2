package com.pdftron.pdf.controls;


import android.util.SparseIntArray;
import android.view.View;

import com.pdftron.pdf.tools.R;

/**
 * Id that references a specific button on the {@link AnnotationToolbar}
 */
public enum AnnotationToolbarButtonId {
    PAN(R.id.controls_annotation_toolbar_tool_pan),
    CLOSE(R.id.controls_annotation_toolbar_btn_close),
    OVERFLOW(R.id.controls_annotation_toolbar_btn_more);

    public final int id;

    AnnotationToolbarButtonId(int id) {
        this.id = id;
    }

    /**
     * Returns a sparse array of toolbar button ids and the associated
     * button visibility. Default all buttons are visible.
     */
    static SparseIntArray getButtonVisibilityArray() {
        SparseIntArray array = new SparseIntArray();
        array.put(R.id.controls_annotation_toolbar_tool_pan, View.VISIBLE);
        array.put(R.id.controls_annotation_toolbar_btn_close, View.VISIBLE);
        array.put(R.id.controls_annotation_toolbar_btn_more, View.VISIBLE);
        return array;
    }
}
