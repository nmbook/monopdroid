package edu.rochester.nbook.monopdroid;

public class GameItem {
	private int gameId;
	private String host;
	private int port;
	private String version;
	private String type;
	private String type_name;
	private String descr;
	private int players;
	private boolean canJoin;
	
	public GameItem(int gameId, String host, int port, String version, String type, String type_name, String descr, int players, boolean canJoin) {
		this.gameId = gameId;
		this.host = host;
		this.port = port;
		this.version = version;
		this.type = type;
		this.type_name = type_name;
		this.descr = descr;
		this.players = players;
		this.canJoin = canJoin;
	}

	public int getGameId() {
		return gameId;
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

	public String getType() {
		return type;
	}

	public String getTypeName() {
		return type_name;
	}

	public String getDescription() {
		return descr;
	}

	public int getPlayers() {
		return players;
	}

	public boolean canJoin() {
		return canJoin;
	}
}
