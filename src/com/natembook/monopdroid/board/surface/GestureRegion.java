package com.natembook.monopdroid.board.surface;

import java.util.ArrayList;

import android.graphics.Rect;

public class GestureRegion {
    private Rect bounds;
    private int tag;
    private GestureRegionListener listener;
    private ArrayList<OnButtonStateChangedHandler> handlers = new ArrayList<OnButtonStateChangedHandler>();
    private ButtonState state;
    private boolean longClickable;
    
    public GestureRegion(Rect bounds, int tag, GestureRegionListener listener) {
        this(bounds, tag, false, listener);
    }

    public GestureRegion(Rect bounds, int tag, boolean longClickable, GestureRegionListener listener) {
        this.bounds = bounds;
        this.tag = tag;
        this.longClickable = longClickable;
        this.listener = listener;
        state = ButtonState.NORMAL;
    }
    
    public void check() {
        switch (state) {
        case NORMAL:
            state = ButtonState.CHECKED;
            break;
        case PRESSED:
            state = ButtonState.CHECKED_PRESSED;
            break;
        case FOCUSED:
            state = ButtonState.CHECKED_FOCUSED;
            break;
        case DISABLED:
            state = ButtonState.CHECKED_DISABLED;
            break;
        case DISABLED_FOCUSED:
            state = ButtonState.CHECKED_DISABLED_FOCUSED;
            break;
        default:
            return;
        }
        callStateHandlers();
    }
    
    public void uncheck() {
        switch (state) {
        case CHECKED:
            state = ButtonState.NORMAL;
            break;
        case CHECKED_PRESSED:
            state = ButtonState.PRESSED;
            break;
        case CHECKED_FOCUSED:
            state = ButtonState.FOCUSED;
            break;
        case CHECKED_DISABLED:
            state = ButtonState.DISABLED;
            break;
        case CHECKED_DISABLED_FOCUSED:
            state = ButtonState.DISABLED_FOCUSED;
            break;
        default:
            return;
        }
        callStateHandlers();
    }
    
    public void focus() {
        switch (state) {
        case NORMAL:
        case PRESSED:
            state = ButtonState.FOCUSED;
            break;
        case DISABLED:
            state = ButtonState.DISABLED_FOCUSED;
            break;
        case CHECKED:
        case CHECKED_PRESSED:
            state = ButtonState.CHECKED_FOCUSED;
            break;
        case CHECKED_DISABLED:
            state = ButtonState.CHECKED_DISABLED_FOCUSED;
            break;
        default:
            return;
        }
        callStateHandlers();
    }
    
    public void unfocus() {
        switch (state) {
        case FOCUSED:
            state = ButtonState.NORMAL;
            break;
        case DISABLED_FOCUSED:
            state = ButtonState.DISABLED;
            break;
        case CHECKED_FOCUSED:
            state = ButtonState.CHECKED;
            break;
        case CHECKED_DISABLED_FOCUSED:
            state = ButtonState.CHECKED_DISABLED;
            break;
        default:
            return;
        }
        callStateHandlers();
    }

    public void enable() {
        switch (state) {
        case DISABLED:
            state = ButtonState.NORMAL;
            break;
        case DISABLED_FOCUSED:
            state = ButtonState.FOCUSED;
            break;
        case CHECKED_DISABLED:
            state = ButtonState.CHECKED;
            break;
        case CHECKED_DISABLED_FOCUSED:
            state = ButtonState.CHECKED_FOCUSED;
            break;
        default:
            return;
        }
        callStateHandlers();
    }

    public void disable() {
        switch (state) {
        case NORMAL:
        case PRESSED:
            state = ButtonState.DISABLED;
            break;
        case FOCUSED:
            state = ButtonState.DISABLED_FOCUSED;
            break;
        case CHECKED:
        case CHECKED_PRESSED:
            state = ButtonState.CHECKED_DISABLED;
            break;
        case CHECKED_FOCUSED:
            state = ButtonState.CHECKED_DISABLED_FOCUSED;
            break;
        default:
            return;
        }
        callStateHandlers();
    }

    public void down() {
        switch (state) {
        case NORMAL:
        case FOCUSED:
            state = ButtonState.PRESSED;
            break;
        case CHECKED:
        case CHECKED_FOCUSED:
            state = ButtonState.CHECKED_PRESSED;
            break;
        default:
            return;
        }
        callStateHandlers();
    }

    public void up() {
        switch (state) {
        case PRESSED:
            state = ButtonState.NORMAL;
            break;
        case CHECKED_PRESSED:
            state = ButtonState.CHECKED;
            break;
        default:
            return;
        }
        callStateHandlers();
    }

    public void invokeClick() {
        switch (state) {
        case CHECKED_DISABLED:
        case CHECKED_DISABLED_FOCUSED:
        case DISABLED:
        case DISABLED_FOCUSED:
            break;
        default:
            this.listener.onGestureRegionClick(this);
            break;
        }
    }

    public void invokeLongPress() {
        this.listener.onGestureRegionLongPress(this);
    }

    public boolean isEnabled() {
        return !(state == ButtonState.CHECKED_DISABLED ||
                 state == ButtonState.CHECKED_DISABLED_FOCUSED ||
                 state == ButtonState.DISABLED ||
                 state == ButtonState.DISABLED_FOCUSED);
    }

    public void addStateHandler(OnButtonStateChangedHandler region) {
        handlers.add(region);
    }
    
    private void callStateHandlers() {
        for (OnButtonStateChangedHandler region : handlers) {
            region.onStateChanged(state);
        }
    }
    
    public int getTag() {
        return tag;
    }

    public Rect getBounds() {
        return bounds;
    }
    
    public boolean isLongClickable() {
        return longClickable;
    }
}
