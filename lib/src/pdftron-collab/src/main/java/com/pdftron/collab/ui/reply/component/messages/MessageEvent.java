package com.pdftron.collab.ui.reply.component.messages;

import com.pdftron.collab.ui.base.component.BaseUIEvent;
import com.pdftron.collab.ui.reply.model.ReplyMessage;

/**
 * Represents a user event from interaction with a message in the annotation reply UI.
 */
public class MessageEvent extends BaseUIEvent<MessageEvent.Type, ReplyMessage> {

    MessageEvent(MessageEvent.Type eventType, ReplyMessage data) {
        super(eventType, data);
    }

    public enum Type {
        MESSAGE_DELETE_CLICKED,
        MESSAGE_EDIT_CLICKED,
    }
}
