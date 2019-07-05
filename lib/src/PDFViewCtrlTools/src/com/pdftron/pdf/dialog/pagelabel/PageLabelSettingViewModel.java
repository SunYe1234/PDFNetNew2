package com.pdftron.pdf.dialog.pagelabel;

import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;

import com.pdftron.pdf.utils.Event;

/**
 * View Model containing user settings from the Page label setting dialog.
 * Observe data changes from this View Model if you would like to respond to
 * Page label edit/modification events.
 */
public class PageLabelSettingViewModel extends ViewModel {
    @NonNull
    private final MutableLiveData<Event<PageLabelSetting>> mPageLabelObservable = new MutableLiveData<>();
    @NonNull
    private PageLabelSetting mPageLabelSettings;

    void set(PageLabelSetting setting) {
        mPageLabelSettings = setting;
    }

    PageLabelSetting get() {
        return mPageLabelSettings;
    }

    void complete() {
        mPageLabelObservable.setValue(new Event<>(mPageLabelSettings));
    }

    /**
     * Observe changes to {@link PageLabelSetting}. The listener will get notified
     * with a {@link PageLabelSetting} object, when the user would like to edit/modify
     * the page labels.
     *
     * Note only one observer is going to be notified of changes.
     *
     * @param owner    The LifecycleOwner which controls the observer
     * @param observer The observer that will receive the events
     */
    public void observeOnComplete(@NonNull LifecycleOwner owner,
                                  @NonNull Observer<Event<PageLabelSetting>> observer) {
        mPageLabelObservable.observe(owner, observer);
    }
}
