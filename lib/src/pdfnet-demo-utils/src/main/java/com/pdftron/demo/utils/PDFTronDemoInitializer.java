package com.pdftron.demo.utils;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import android.util.Log;

import com.pdftron.common.PDFNetException;
import com.pdftron.pdfnet.PDFNetInitializer;

/**
 * Internal class to initialize PDFNet demo package.
 *
 * @hide
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public class PDFTronDemoInitializer extends ContentProvider {
    private static final String TAG = "PDFTronDemoInitializer";

    public boolean onCreate() {
        Context applicationContext = getContext();
        String key = PDFNetInitializer.getLicenseKey(applicationContext);
        if (key != null && applicationContext != null) { // null if it's not defined in gradle.properties
            try {
                AppUtils.initializePDFNetApplication(applicationContext, key);
            } catch (PDFNetException e) {
                e.printStackTrace();
            }
        } else {
            Log.w(TAG, PDFNetInitializer.MSG);
        }
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        return null;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }
}
