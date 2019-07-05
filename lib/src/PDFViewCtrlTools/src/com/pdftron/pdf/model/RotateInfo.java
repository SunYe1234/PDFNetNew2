package com.pdftron.pdf.model;

import android.graphics.PointF;

public class RotateInfo {
    public RotateInfo(float degree,  PointF pivot) {
        this.degree = degree;
        this.pivot = pivot;
    }

    public float getDegree() {
        return degree;
    }

    public void setDegree(float degreePDF) {
        this.degree = degreePDF;
    }

    public PointF getPivot() {
        return pivot;
    }

    public void setPivot(PointF pivot) {
        this.pivot = pivot;
    }

    private float degree;
    private PointF pivot;
}
