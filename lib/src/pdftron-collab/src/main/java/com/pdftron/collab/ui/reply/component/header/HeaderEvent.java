package com.pdftron.collab.ui.reply.component.header;

import com.pdftron.collab.ui.base.component.BaseUIEvent;
import com.pdftron.collab.ui.reply.model.ReplyHeader;

/**
 * Represents a user event from interaction the header in the annotation reply UI.
 */
public class HeaderEvent extends BaseUIEvent<HeaderEvent.Type, ReplyHeader> {

    HeaderEvent(HeaderEvent.Type eventType, ReplyHeader data) {
        super(eventType, data);
    }

    public enum Type {
        CLOSE_CLICKED,  // emitted when the close button is clicked
        LIST_CLICKED    // emitted when the list button is tapped in the header
    }
}
