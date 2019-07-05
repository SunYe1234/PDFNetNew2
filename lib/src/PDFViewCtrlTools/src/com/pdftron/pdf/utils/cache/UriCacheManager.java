package com.pdftron.pdf.utils.cache;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.RestrictTo;

import java.io.File;

/**
 * @hide
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public class UriCacheManager {

    public static final String BUNDLE_USE_CACHE_FOLDER = "PdfViewCtrlTabFragment_bundle_cache_folder_uri";
    private static final String cacheFolder = "uri_cache";

    public static File getCacheDir(@NonNull Context context) {
           return new File(context.getCacheDir(), cacheFolder);
    }
}
