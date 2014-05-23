package edu.rochester.nbook.monopdroid.board;

import java.util.HashMap;

import android.util.Log;

/**
 * Represents a configurable in the lobby. The only type ever encountered has been of type "bool",
 * so for now that's all that is supported.
 * @author Nate
 *
 */
public class Configurable {
    public static final HashMap<String, XmlAttribute> configurableAttributes = new HashMap<String, XmlAttribute>() {
        private static final long serialVersionUID = -1592526247114303043L;

        {
            this.put("name", new XmlAttribute(Configurable.class, "setName", XmlAttributeType.STRING));
            this.put("description", new XmlAttribute(Configurable.class, "setDescription", XmlAttributeType.STRING));
            this.put("type", new XmlAttribute(Configurable.class, "setType", XmlAttributeType.STRING));
            this.put("edit", new XmlAttribute(Configurable.class, "setEditable", XmlAttributeType.BOOLEAN));
            this.put("value", new XmlAttribute(Configurable.class, "setValue", XmlAttributeType.STRING));
        }
    };
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

    // configurable object data
    private int configId;
    private String name;
    private String description;
    //private ConfigurableType type;
    private String command;
    private String value;
    private boolean editable;

    /**
     * Create a new configurable (old style).
     * @param configId2 
     * @param title The text label to display.
     * @param type The configurable type.
     * @param command The command used by people who can edit it to update it.
     * @param value The current value.
     * @param editable Whether this configurable can be edited by this instance.
     */
    public Configurable(int configId, String title, String type, String command, String value, boolean editable) {
        if (!type.equals("bool")) {
            Log.w("monopd", "Encountered a configuration option of type other than BOOLEAN. Ignoring.");
        }
        //this.type = ConfigurableType.BOOLEAN;
        this.configId = configId;
        this.name = command;
        this.description = title;
        this.command = command;
        this.value = value;
        this.editable = editable;
    }

    public Configurable(int configId) {
        this.configId = configId;
    }
    
    public int getConfigId() {
        return configId;
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

    public String getName() {
        return this.name;
    }
    
    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return this.description;
    }
    
    public void setDescription(String descr) {
        this.description = descr;
    }
    
    public void setType(String s) {
        // this.type = s;
    }

    public String getType() {
        return "bool";
    }

    public String getCommand() {
        if (this.command == null) {
            return ".gc" + configId + ":";
        }
        return this.command;
    }
    
    @Override
    public String toString() {
        return description + " (" + getCommand() + ", id: " + configId + ")";
    }
}
