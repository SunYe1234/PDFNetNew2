package com.pdftron.collab.ui.base.component;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.ViewGroup;

import io.reactivex.subjects.Subject;

/**
 * The main responsibility of a {@link BaseUIView} is the inflate and setup the views. It is, however,
 * not responsible for initializing the view state (however there are cases where view states can be
 * initialized here, such as views with static states).
 */
public abstract class BaseUIView<E extends BaseUIEvent> {

    @Nullable
    protected Subject<E> mEventObservable;

    @NonNull
    protected ViewGroup mParent;

    public BaseUIView(@NonNull ViewGroup parent) {
        mParent = parent;
    }

    void setEventObservable(@Nullable Subject<E> eventObservable) {
        mEventObservable = eventObservable;
    }
}
