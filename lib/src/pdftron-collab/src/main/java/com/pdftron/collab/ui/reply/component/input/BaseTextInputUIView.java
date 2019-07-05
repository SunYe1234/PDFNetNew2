package com.pdftron.collab.ui.reply.component.input;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import android.view.ViewGroup;

import com.pdftron.collab.ui.base.component.BaseUIView;

/**
 * {@hide}
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public abstract class BaseTextInputUIView extends BaseUIView<InputEvent> implements TextInputInteraction {

    public BaseTextInputUIView(@NonNull ViewGroup parent) {
        super(parent);
    }

    @Override
    public void onMessageWriteChanged(@Nullable String input) {
        if (mEventObservable != null) {
            mEventObservable.onNext(new InputEvent(InputEvent.Type.MESSAGE_WRITE_TEXT_CHANGED, input));
        }
    }

    @Override
    public void onMessageWriteFinished(@Nullable String input) {
        if (mEventObservable != null) {
            mEventObservable.onNext(new InputEvent(InputEvent.Type.MESSAGE_WRITE_FINISHED, input));
        }
    }

    @Override
    public void onMessageEditChanged(@Nullable String input) {
        if (mEventObservable != null) {
            mEventObservable.onNext(new InputEvent(InputEvent.Type.MESSAGE_EDIT_TEXT_CHANGED, input));
        }
    }

    @Override
    public void onMessageEditFinished(@Nullable String newMessage) {
        if (mEventObservable != null) {
            mEventObservable.onNext(new InputEvent(InputEvent.Type.MESSAGE_EDIT_FINISHED, newMessage));
        }
    }

    @Override
    public void onMessagedEditCancelled() {
        if (mEventObservable != null) {
            mEventObservable.onNext(new InputEvent(InputEvent.Type.MESSAGE_EDIT_CANCELED, null));
        }
    }

    public abstract void clearInput(); // todo make this reactive

    public abstract void showMessageEditView(String content);

    public abstract void closeMessageEditView();
}
