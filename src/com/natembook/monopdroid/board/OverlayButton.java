package com.natembook.monopdroid.board;

import com.natembook.monopdroid.board.surface.GestureRegionListener;

/**
 * Represents a button on an overlay window.
 * @author Nate
 */
public class OverlayButton {
    private String caption;
    private String tip;
    private boolean enabled;
    private GestureRegionListener listener;
    private int width = 1;
    
    public OverlayButton(String caption, String tip, boolean enabled, int width, GestureRegionListener listener) {
        this.caption = caption;
        this.tip = tip;
        this.enabled = enabled;
        this.listener = listener;
        this.width = width;
    }

    /**
     * Get the listener to call when this is tapped or long-pressed.
     * @return The listener.
     */
    public GestureRegionListener getListener() {
        return listener;
    }
    
    /**
     * Gets the width.
     * @return A number from 1 to 6.
     */
    public int getWidth() {
        return width;
    }
    
    public String getCaption() {
        return caption;
    }
    
    public String getTip() {
        return tip;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    @Override
    public String toString() {
        return caption + " overlay button";
    }
}
