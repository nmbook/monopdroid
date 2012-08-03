package edu.rochester.nbook.monopdroid;

public class ServerItem {
	private String host;
	private int port;
	private String version;
	private int users;
	
	public ServerItem(String host, int port, String version, int users) {
		this.host = host;
		this.port = port;
		this.version = version;
		this.users = users;
	}
	
	public String getHost() {
		return host;
	}
	
	public int getPort() {
		return port;
	}
	
	public String getVersion() {
		return version;
	}
	
	public int getUsers() {
		return users;
	}
}
