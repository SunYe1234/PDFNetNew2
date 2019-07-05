package com.pdftron.pdf.widget.signature;

import android.graphics.Path;

class InkDrawInfo {
    public final int left, right, top, bottom;
    public final Path path;

    InkDrawInfo(int left, int right, int top, int bottom, Path path) {
        this.left = left;
        this.right = right;
        this.top = top;
        this.bottom = bottom;
        this.path = path;
    }
}
