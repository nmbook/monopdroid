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
    private GameItemType item_type;

    /**
     * Create a game item for joining a game.
     * 
     * @param gameId
     *            The game ID.
     * @param host
     *            The server host.
     * @param port
     *            The server port.
     * @param version
     *            The server version.
     * @param type
     *            The internal game type.
     * @param type_name
     *            The external game type.
     * @param descr
     *            The game description.
     * @param players
     *            Current number of players.
     * @param canJoin
     *            Whether the game server says we can join.
     */
    public GameItem(int gameId, String host, int port, String version, String type, String type_name, String descr,
            int players, boolean canJoin) {
        this.item_type = GameItemType.JOIN;
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
     * Create a combined game item for creating a game, listing multiple
     * servers.
     * 
     * @param servers
     *            The list of servers
     * @param type
     *            The internal game type.
     * @param type_name
     *            The external game type.
     * @param descr
     *            The game description.
     */
    public GameItem(String host, int port, String version, String type, String type_name, String descr) {
        this.item_type = GameItemType.CREATE;
        this.gameId = -1;
        ServerItem item = new ServerItem(host, port, version, players);
        this.servers = new ArrayList<ServerItem>();
        this.servers.add(item);
        this.type = type;
        this.type_name = type_name;
        this.descr = descr;
        this.players = 0;
        this.canJoin = true;
    }
    
    /**
     * Create a game item for indicating error or progress of the given type.
     * 
     * @param type
     *            The game item type.
     */
    public GameItem(GameItemType type) {
        this.item_type = type;
        this.gameId = -1;
        this.canJoin = false;
        this.servers = new ArrayList<ServerItem>();
    }
    
    /**
     * Create a game item for reconnecting to a game you lost connection to.
     * 
     * @param gameId
     *            The game ID.
     * @param host
     *            The server host.
     * @param port
     *            The server port.
     * @param version
     *            The server version.
     * @param type
     *            The internal game type.
     * @param type_name
     *            The external game type.
     * @param descr
     *            The game description.
     * @param players
     *            Number of players.
     */
    public GameItem(int gameId, String host, int port, String version, String type, String type_name, String descr,
            int players) {
        this.item_type = GameItemType.RECONNECT;
        this.gameId = gameId;
        ServerItem item = new ServerItem(host, port, version, players);
        this.servers = new ArrayList<ServerItem>();
        this.servers.add(item);
        this.type = type;
        this.type_name = type_name;
        this.descr = descr;
        this.players = players;
        this.canJoin = true;
    }
    
    /**
     * Create a game item from existing game item data.
     * 
     * @param item_type The item type.
     * @param id The game ID.
     * @param servers The game server list.
     * @param type The internal game type.
     * @param type_name The external game type.
     * @param descr The game description.
     * @param players The player count.
     * @param can_join Whether we can join this game.
     */
    public GameItem(GameItemType item_type, int id, List<ServerItem> servers, String type, String type_name,
            String descr, int players, boolean can_join) {
        this.item_type = item_type;
        this.gameId = id;
        this.servers = servers;
        this.type = type;
        this.type_name = type_name;
        this.descr = descr;
        this.players = players;
        this.canJoin = can_join;
    }

    /**
     * Chooses a server to create a game on randomly. Alters the game list to
     * only include the chosen server.
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
        return this.gameId;
    }

    public void setGameId(int gameId) {
        this.gameId = gameId;
    }

    public List<ServerItem> getServers() {
        return this.servers;
    }

    public ServerItem getServer() {
        return this.servers.get(0);
    }

    public String getType() {
        return this.type;
    }
    
    public void setType(String type) {
        this.type = type;
    }

    public String getTypeName() {
        return this.type_name;
    }

    public void setTypeName(String type_name) {
        this.type_name = type_name;
    }

    public String getDescription() {
        return this.descr;
    }
    
    public void setDescription(String descr) {
        this.descr = descr;
    }

    public int getPlayers() {
        return this.players;
    }

    public void setPlayers(int players) {
        this.players = players;
    }

    public boolean canJoin() {
        return this.canJoin;
    }
    
    public void setCanJoin(boolean canJoin) {
        this.canJoin = canJoin;
    }
    
    public GameItemType getItemType() {
        return this.item_type;
    }
}
