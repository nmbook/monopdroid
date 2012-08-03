package edu.rochester.nbook.monopdroid;

public class Configurable {
	public enum ConfigurableType {
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
	}
	
	private String title;
	private ConfigurableType type;
	private String command;
	private String value;
	private boolean editable;
	
	public Configurable(String title, String type, String command,
			String value, boolean editable) {
		this.title = title;
		this.type = ConfigurableType.BOOLEAN;
		this.command  = command;
		this.value = value;
		this.editable = editable;
	}

	public String getValue() {
		return value;
	}
	
	public void setValue(String value) {
		this.value = value;
	}
	
	public boolean isEditable() {
		return editable;
	}
	
	public void setEditable(boolean editable) {
		this.editable = editable;
	}
	
	public String getTitle() {
		return title;
	}
	
	public ConfigurableType getType() {
		return type;
	}
	
	public String getCommand() {
		return command;
	}
}
