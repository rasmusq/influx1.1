package com.ralleq.influx.touch;

public class TouchEvent {

    public static final int EVENT_NONE = 0, EVENT_DOWN = 1, EVENT_UP = 2, EVENT_MOVE = 3;
    private int eventType = EVENT_NONE;

    private TouchPointer[] touchPointers;
    private int activePointerIndex;

    public TouchEvent(TouchPointer[] touchPointers, int activePointerIndex) {
        this.touchPointers = touchPointers;
        this.activePointerIndex = activePointerIndex;
    }

    public int getEventType() {
        return eventType;
    }

    public TouchPointer[] getTouchPointers() {
        return touchPointers;
    }

    public int getActivePointerIndex() {
        return activePointerIndex;
    }
}
