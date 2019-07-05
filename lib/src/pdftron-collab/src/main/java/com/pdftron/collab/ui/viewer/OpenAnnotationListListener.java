package com.pdftron.collab.ui.viewer;

import android.support.annotation.RestrictTo;

/**
 * Listener used to open the annotation list from a {@link CollabViewerTabFragment}
 * {@hide}
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public interface OpenAnnotationListListener {
    void openAnnotationList();
}
