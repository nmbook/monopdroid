package com.natembook.monopdroid.board;

/**
 * Represents a MONOPD Button object.
 * @author Nate
 *
 */
public class Button {
    private String caption;
    private String command;
    private boolean enabled;
    
    public Button(String caption, boolean enabled, String command) {
        this.caption = caption;
        this.command = command;
        this.enabled = enabled;
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
}
