//---------------------------------------------------------------------------------------
// Copyright (c) 2001-2019 by PDFTron Systems Inc. All Rights Reserved.
// Consult legal.txt regarding legal and license information.
//---------------------------------------------------------------------------------------

package com.pdftron.demo.utils;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.OnLifecycleEvent;
import android.os.FileObserver;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.pdftron.demo.navigation.callbacks.FileManagementListener;
import com.pdftron.pdf.utils.Logger;

/**
 * @hide
 */
public class RecursiveFileObserver extends FileObserver implements LifecycleObserver {
    private static final String TAG = RecursiveFileObserver.class.getName();
    public static int CHANGES_ONLY = FileObserver.CREATE |
            FileObserver.DELETE | FileObserver.MOVED_FROM | FileObserver.MOVED_TO;

    @Nullable
    private FileManagementListener mListener;
    private String mAbsolutePath;
    private boolean mEnabled = false;

    public RecursiveFileObserver(@NonNull String path, int mask,
                                 @NonNull FileManagementListener listener, @NonNull LifecycleOwner owner) {
        super(path, mask);
        this.mListener = listener;
        mAbsolutePath = path;
        observe(owner);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    public void pauseFileObserver() {
        Logger.INSTANCE.LogD(TAG, "RecursiveFileObserver paused");
        mEnabled = false;
        stopWatching();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    public void resumeFileObserver() {
        Logger.INSTANCE.LogD(TAG, "RecursiveFileObserver resumed");
        mEnabled = true;
        startWatching();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    private void destroyFileObserver() {
        Logger.INSTANCE.LogD(TAG, "RecursiveFileObserver destroyed");
        this.mListener = null;
        stopWatching();
    }

    /**
     * Start observing lifecycle events from the specified {@link LifecycleOwner}
     *
     * @param owner {@link LifecycleOwner} to observe
     */
    private void observe(LifecycleOwner owner) {
        owner.getLifecycle().addObserver(this);
    }

    /**
     * Removes this file observer from the {@link LifecycleOwner}. It will also clean up this
     * file observer, so you should not reuse the file observer.
     *
     * @param owner {@link LifecycleOwner} remove observer
     */
    public void stopObserving(LifecycleOwner owner) {
        destroyFileObserver();
        owner.getLifecycle().removeObserver(this);
    }

    @Override
    public void onEvent(int event, String path) {
        // Only emit events if file observer is watching
        if (mEnabled) {
            if (mAbsolutePath != null && path != null) {
                path = mAbsolutePath.concat("/").concat(path);
                String[] dir = path.split("/");
                if (dir[dir.length - 1].equals("null")) {
                    return;
                }
            }
            if (mListener != null) {
                mListener.onFileChanged(path, event);
            }
        }
    }

}
