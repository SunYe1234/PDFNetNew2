package com.pdftron.collab.ui.base.component;

import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;
import android.view.ViewGroup;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.subjects.PublishSubject;

/**
 * A {@link BaseUIComponent} describes a UI component on the screen, which could be a part of an
 * activity or fragment (basically anything that is a {@link LifecycleOwner}. The main responsibility
 * of the {@link BaseUIComponent} is to define the business logic of its views and maintain the data
 * associated with the views.
 *
 * <p>
 * The data for {@link BaseUIComponent} is contained within a {@link ViewModel} object.
 * Updates to the data can be observed directly by the {@link BaseUIComponent}, or by the caller if
 * needed (usually a {@link android.support.v4.app.Fragment} or an {@link android.app.Activity}).
 * <p>
 * Android views are handled by a {@link BaseUIView} object. The only responsibilities of this object
 * are to inflate the views and to emit user interaction events to the component.
 * User interaction is captured through {@link BaseUIEvent}s and these events can be observed
 * directly by the {@link BaseUIComponent}, or by the caller.
 */
public abstract class BaseUIComponent<V extends BaseUIView<E>, E extends BaseUIEvent, VM extends ViewModel> {
    @NonNull
    protected final PublishSubject<E> mSubject;
    @NonNull
    protected final CompositeDisposable mDisposables = new CompositeDisposable();
    @NonNull
    protected final V mView;

    /**
     * Constructor for creating a UI component.
     *
     * @param parent         the ViewGroup to inflate this component's UI
     * @param lifecycleOwner the {@link LifecycleOwner} that uses this component
     * @param model          the ViewModel that contains the view state data for this component
     * @param observable     the observable that will be used to emit user UI interfaction events
     */
    @SuppressWarnings("unused")
    public BaseUIComponent(@NonNull ViewGroup parent,
            @NonNull LifecycleOwner lifecycleOwner,
            @NonNull VM model,
            @NonNull PublishSubject<E> observable) {
        mSubject = observable;
        mView = inflateUIView(parent);
        mView.setEventObservable(observable);
    }

    /**
     * Creates the {@link BaseUIView} of type {@code V} for this BaseUIComponent. The BaseUIView will inflate
     * and initialize the Android Views
     *
     * @param parent ViewGroup parent to inflate and attach the component views
     * @return the BaseUIView
     */
    @NonNull
    protected abstract V inflateUIView(@NonNull ViewGroup parent);

    /**
     * Returns the {@link Observable} that emits user events {@link BaseUIEvent} of type {@code E}.
     * This is typically called outside of the component, by Fragments or Activities that would like
     * to listen to user events.
     *
     * @return the observable that is used to listen BaseUIEvents
     */
    @NonNull
    public final Observable<E> getObservable() {
        return mSubject.serialize();
    }
}
