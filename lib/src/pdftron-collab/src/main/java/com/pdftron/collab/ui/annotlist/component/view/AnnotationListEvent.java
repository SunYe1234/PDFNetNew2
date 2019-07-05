package com.pdftron.collab.ui.annotlist.component.view;

import com.pdftron.collab.ui.annotlist.model.list.item.AnnotationListContent;
import com.pdftron.collab.ui.base.component.BaseUIEvent;

/**
 * Represents a user event from interaction with the annotation list UI.
 */
public class AnnotationListEvent extends BaseUIEvent<AnnotationListEvent.Type, AnnotationListContent> {

    AnnotationListEvent(AnnotationListEvent.Type eventType, AnnotationListContent data) {
        super(eventType, data);
    }

    public enum Type {
        ANNOTATION_ITEM_CLICKED
    }
}
