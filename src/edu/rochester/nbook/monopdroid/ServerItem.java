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
        return this.host;
    }

    public int getPort() {
        return this.port;
    }

    public String getVersion() {
        return this.version;
    }

    public int getUsers() {
        return this.users;
    }
}
