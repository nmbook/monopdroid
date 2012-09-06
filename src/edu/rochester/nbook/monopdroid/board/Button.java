package edu.rochester.nbook.monopdroid.board;

public class Button {
    private String caption;
    private String command;
    private boolean enabled;
    
    public Button(String caption, String command, boolean enabled) {
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
