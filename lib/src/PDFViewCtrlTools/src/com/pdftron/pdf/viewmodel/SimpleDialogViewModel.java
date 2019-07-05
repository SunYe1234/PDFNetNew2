package com.pdftron.pdf.viewmodel;

import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;

import com.pdftron.pdf.utils.Event;

public abstract class SimpleDialogViewModel<T> extends ViewModel {
    @NonNull
    private final MutableLiveData<Event<T>> mCompleteable = new MutableLiveData<>();
    @NonNull
    private MutableLiveData<T> mResult = new MutableLiveData<>();

    public SimpleDialogViewModel() {
        mCompleteable.setValue(null); // initialize view model
        mResult.setValue(null);
    }

    public void set(T result) {
        mResult.setValue(result);
    }

    public void complete() {
        mCompleteable.setValue(mResult.getValue() == null ?
                null : new Event<>(mResult.getValue()));
        mResult.setValue(null);
    }

    public void observeOnComplete(@NonNull LifecycleOwner owner,
            @NonNull Observer<Event<T>> observer) {
        mCompleteable.observe(owner, observer);
    }

    public void observeChanges(@NonNull LifecycleOwner owner,
            @NonNull Observer<T> observer) {
        mResult.observe(owner, observer);
    }
}
