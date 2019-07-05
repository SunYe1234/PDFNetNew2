package com.pdftron.collab.ui.base.component;

import android.support.annotation.NonNull;

/**
 * Describes user UI interaction (i.e. clicks, long-presses, radio button selected) by defining
 * a type (usually an enumerator) and the data associated with the event (if no data is needed,
 * you can specify {@link Void}.
 */
public class BaseUIEvent<T, D> {
    @NonNull
    private final T mEventType;
    @NonNull
    private final D mData;

    /**
     * Base class for defining user UI interfaction within a component
     * @param eventType type of event this class represents, typically an enumeration.
     * @param data the data that can be
     */
    public BaseUIEvent(@NonNull T eventType, @NonNull D data) {
        mEventType = eventType;
        mData = data;
    }

    @NonNull
    public T getEventType() {
        return mEventType;
    }

    @NonNull
    public D getData() {
        return mData;
    }
}
