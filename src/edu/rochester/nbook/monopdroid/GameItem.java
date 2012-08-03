package edu.rochester.nbook.monopdroid;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameItem {
	private static Random rng = new Random();
	
	private int gameId;
	private List<ServerItem> servers;
	private String type;
	private String type_name;
	private String descr;
	private int players;
	private boolean canJoin;
	
	/**
	 * Create a game item for joining a game.
	 * @param gameId The game ID.
	 * @param host The server host.
	 * @param port The server port.
	 * @param version The server version.
	 * @param type The internal game type.
	 * @param type_name The external game type.
	 * @param descr The game description.
	 * @param players Current number of players.
	 * @param canJoin Whether the game server says we can join.
	 */
	public GameItem(int gameId, String host, int port, String version, String type, String type_name, String descr, int players, boolean canJoin) {
		this.gameId = gameId;
		ServerItem item = new ServerItem(host, port, version, players);
		this.servers = new ArrayList<ServerItem>();
		this.servers.add(item);
		this.type = type;
		this.type_name = type_name;
		this.descr = descr;
		this.players = players;
		this.canJoin = canJoin;
	}
	
	/**
	 * Create a combined game item for creating a game, listing multiple servers.
	 * @param gameId The game ID (-1)
	 * @param servers The list of servers
	 * @param type The internal game type.
	 * @param type_name The external game type.
	 * @param descr The game description.
	 * @param players Current number of players.
	 * @param canJoin Whether the game server says we can join.
	 */
	public GameItem(int gameId, List<ServerItem> servers, String type, String type_name, String descr, int players, boolean canJoin) {
		this.gameId = gameId;
		this.servers = servers;
		this.type = type;
		this.type_name = type_name;
		this.descr = descr;
		this.players = players;
		this.canJoin = canJoin;
	}

	/**
	 * Chooses a server to create a game on randomly.
	 * Alters the game list to only include the chosen server.
	 */
	public void chooseServer() {
		if (this.servers.size() <= 1) {
			// already only one
			return;
		}
		int index = rng.nextInt(this.servers.size());
		ServerItem server = this.servers.get(index);
		this.servers = new ArrayList<ServerItem>();
		this.servers.add(server);
	}

	public int getGameId() {
		return gameId;
	}
	
	public List<ServerItem> getServers() {
		return servers;
	}
	
	public ServerItem getServer() {
		return servers.get(0);
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
