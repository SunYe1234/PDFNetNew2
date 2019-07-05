package com.pdftron.pdf.widget.signature;

class InkEvent {
    public final InkEventType eventType;
    public final float x;
    public final float y;
    final float pressure;

    InkEvent(InkEventType eventType, float x, float y, float pressure) {
        this.eventType = eventType;
        this.x = x;
        this.y = y;
        this.pressure = pressure;
    }
}
