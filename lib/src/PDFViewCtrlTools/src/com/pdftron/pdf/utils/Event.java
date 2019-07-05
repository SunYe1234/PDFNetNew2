package com.pdftron.pdf.utils;

// Referenced from: https://medium.com/androiddevelopers/livedata-with-snackbar-navigation-and-other-events-the-singleliveevent-case-ac2622673150

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Used as a wrapper for data that is exposed via a LiveData that represents an event.
 */
public class Event<T> {

    private T mContent;

    private boolean hasBeenHandled = false;

    public Event(@NonNull T content) {
        //noinspection ConstantConditions
        if (content == null) {
            throw new IllegalArgumentException("Null values in Event are not allowed.");
        }
        mContent = content;
    }

    @Nullable
    public T getContentIfNotHandled() {
        if (hasBeenHandled) {
            return null;
        } else {
            hasBeenHandled = true;
            return mContent;
        }
    }

    public boolean hasBeenHandled() {
        return hasBeenHandled;
    }
}
