package com.pdftron.collab.ui.reply.component.input;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.pdftron.collab.ui.base.component.BaseUIEvent;

/**
 * Represents a user event from interaction with the text input field in the annotation reply UI.
 */
public class InputEvent extends BaseUIEvent<InputEvent.Type, String> {

    InputEvent(@NonNull InputEvent.Type eventType, @Nullable String data) {
        super(eventType, data);
    }

    public enum Type {
        MESSAGE_WRITE_TEXT_CHANGED, // event when user has changed the text while writing a new message
        MESSAGE_WRITE_FINISHED,     // event when user has finished writing a new message
        MESSAGE_EDIT_TEXT_CHANGED,  // event when user has changed the text of an existing message
        MESSAGE_EDIT_FINISHED,      // event when user has finished editing an existing message
        MESSAGE_EDIT_CANCELED       // event when user has cancelled editing an existing message
    }
}
