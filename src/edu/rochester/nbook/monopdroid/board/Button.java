package edu.rochester.nbook.monopdroid.board;

import edu.rochester.nbook.monopdroid.board.surface.GestureRegionListener;

public class Button {
    private String caption;
    private String command;
    private boolean enabled;
    private GestureRegionListener listener;
    private int width = 1;
    
    public Button(String caption, boolean enabled, String command) {
        this.caption = caption;
        this.command = command;
        this.enabled = enabled;
        this.listener = null;
    }
    
    public Button(String caption, boolean enabled, int width, GestureRegionListener listener) {
        this.caption = caption;
        this.command = null;
        this.enabled = enabled;
        this.listener = listener;
        this.width = width;
    }
    
    public String getCaption() {
        return caption;
    }
    
    public String getCommand() {
        return command;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    @Override
    public String toString() {
        return caption + " (" + command + ")";
    }

    public GestureRegionListener getListener() {
        return listener;
    }
    
    public int getWidth() {
        return width;
    }
}
