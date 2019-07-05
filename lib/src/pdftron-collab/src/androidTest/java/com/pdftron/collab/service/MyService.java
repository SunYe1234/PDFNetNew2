package com.pdftron.collab.service;

import android.support.annotation.Nullable;

import com.pdftron.collab.db.entity.AnnotationEntity;

import java.util.ArrayList;

public class MyService implements CustomService {

    @Override
    public void sendAnnotation(String action, ArrayList<AnnotationEntity> annotations, String documentId, @Nullable String userName) {
        // ignore for test
    }
}
