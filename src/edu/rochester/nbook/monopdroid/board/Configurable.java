package edu.rochester.nbook.monopdroid.board;

import android.util.Log;

/**
 * Represents a configurable in the lobby. The only tpye ever encountered was of type "bool",
 * so for now that's all that is supported.
 * @author Nate
 *
 */
public class Configurable {
    /*public enum ConfigurableType {
        BOOLEAN;

        public static ConfigurableType getTypeFromString(String type) {
            return BOOLEAN;
        }

        public String getAndroidViewType() {
            return "CheckBox";
        }

        public String transformValueToAndroid(String value) {
            if (value == "0") {
                return "false";
            } else {
                return "true";
            }
        }
    }*/

    private String title;
    //private ConfigurableType type;
    private String command;
    private String value;
    private boolean editable;

    /**
     * Create a new configurable.
     * @param title The text label to display.
     * @param type The configurable type.
     * @param command The command used by people who can edit it to update it.
     * @param value The current value.
     * @param editable Whether this configurable can be edited by this instance.
     */
    public Configurable(String title, String type, String command, String value, boolean editable) {
        this.title = title;
        if (!type.equals("bool")) {
            Log.w("monopd", "Encountered a configuration option of type other than BOOLEAN. Ignoring.");
        }
        //this.type = ConfigurableType.BOOLEAN;
        this.command = command;
        this.value = value;
        this.editable = editable;
    }

    /**
     * Gets the current value of this option.
     * @return The current value.
     */
    public String getValue() {
        return this.value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean isEditable() {
        return this.editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    public String getTitle() {
        return this.title;
    }

    /*public ConfigurableType getType() {
        return this.type;
    }*/

    public String getCommand() {
        return this.command;
    }
    
    @Override
    public String toString() {
        return title + " (" + command + ")";
    }
}
