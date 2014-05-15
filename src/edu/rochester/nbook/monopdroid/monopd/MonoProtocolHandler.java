package edu.rochester.nbook.monopdroid.monopd;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import edu.rochester.nbook.monopdroid.board.Button;
import edu.rochester.nbook.monopdroid.board.Configurable;
import edu.rochester.nbook.monopdroid.board.Player;
import edu.rochester.nbook.monopdroid.gamelist.GameItem;
import edu.rochester.nbook.monopdroid.gamelist.ServerItem;

import android.util.Log;

public class MonoProtocolHandler {
    /**
     * Static map between node names and enum above.
     */
    public static final HashMap<String, XmlNodeType> nodeByName = new HashMap<String, XmlNodeType>() {
        private static final long serialVersionUID = 6215377626338659579L;

        {
            this.put("meta_atlantic", XmlNodeType.META_ATLANTIC);
            this.put("metaserver", XmlNodeType.METASERVER);
            this.put("servergamelist", XmlNodeType.SERVERGAMELIST);
            this.put("game", XmlNodeType.GAME);

            this.put("monopd", XmlNodeType.MONOPD);
            this.put("server", XmlNodeType.SERVER);
            this.put("client", XmlNodeType.CLIENT);
            this.put("playerupdate", XmlNodeType.PLAYERUPDATE);
            this.put("msg", XmlNodeType.MSG);
            this.put("gameupdate", XmlNodeType.GAMEUPDATE);
            this.put("updateplayerlist", XmlNodeType.UPDATEPLAYERLIST);
            this.put("player", XmlNodeType.PLAYER);
            this.put("configupdate", XmlNodeType.CONFIGUPDATE);
            this.put("option", XmlNodeType.OPTION);
            this.put("estateupdate", XmlNodeType.ESTATEUPDATE);
            this.put("estategroupupdate", XmlNodeType.ESTATEGROUPUPDATE);
            this.put("display", XmlNodeType.DISPLAY);
            this.put("updategamelist", XmlNodeType.UPDATEGAMELIST);
            this.put("deleteplayer", XmlNodeType.DELETEPLAYER);
            this.put("button", XmlNodeType.BUTTON);
            this.put("auctionupdate", XmlNodeType.AUCTIONUPDATE);
        }
    };

    private Socket socket = null;
    private boolean socketClosed = false;
    private BufferedWriter wr = null;
    private BufferedReader rd = null;
    private XmlPullParser parser = null;
    private MonoProtocolListener listener = null;
    private MonoProtocolType type = MonoProtocolType.GAME;
    private String client = null;
    private String version = null;

    private void initXml() {
        XmlPullParserFactory factory;
        try {
            factory = XmlPullParserFactory.newInstance();
            this.parser = factory.newPullParser();
        } catch (XmlPullParserException ex) {
            this.listener.onException("XML parser initialization failure", ex);
        }
    }

    private void initSocket(String host, int port) {
        try {
            this.socket = new Socket(host, port);
        } catch (UnknownHostException ex) {
            this.listener.onException("Connection failure", ex);
            return;
        } catch (IOException ex) {
            this.listener.onException("Connection failure", ex);
            return;
        }
        try {
            this.wr = new BufferedWriter(new OutputStreamWriter(this.socket.getOutputStream()));
        } catch (IOException e) {
            this.listener.onException("Socket cannot be written to", e);
        }
        try {
            this.rd = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
        } catch (IOException e) {
            this.listener.onException("Socket cannot be written to", e);
        }
    }

    /**
     * Initialize and connect to a meta server.
     * 
     * @param meta
     *            The listener to handle meta server events.
     * @param host
     *            The meta server host name.
     * @param port
     *            The meta server port.
     * @param client
     *            The game client name to send. #param version The game client
     *            version to send.
     */
    public MonoProtocolHandler(MonoProtocolMetaListener meta, String host, int port, String client, String version) {
        this.listener = meta;
        this.type = MonoProtocolType.META;
        this.client = client;
        this.version = version;
        this.initXml();
        this.initSocket(host, port);
    }

    /**
     * Initialize and connect to a game server.
     * 
     * @param game
     *            The listener to handle game server events.
     * @param host
     *            The game server host name.
     * @param port
     *            The game server port.
     * @param client
     *            The game client name to send. #param version The game client
     *            version to send.
     */
    public MonoProtocolHandler(MonoProtocolGameListener game, String host, int port, String client, String version) {
        this.listener = game;
        this.type = MonoProtocolType.GAME;
        this.client = client;
        this.version = version;
        this.initXml();
        this.initSocket(host, port);
    }
    
    /**
     * Gets whether the socket has closed.
     * 
     * @return Returns true if the socket is closed.
     */
    public boolean isClosed() {
        try {
            return (this.socketClosed && this.socket.getInputStream().available() == 0);
        } catch (IOException e) {
            return this.socketClosed;
        }
    }

    /**
     * Sends command to server.
     * 
     * @param command
     *            The comamnd.
     * @param doFlush
     *            Pass true to send right away, false to wait for more input.
     */
    private void sendCommand(String command, boolean doFlush) {
        if (this.wr == null) { // connection failed
            return;
        }
        try {
            this.wr.write(command + "\n");
            if (doFlush) {
                this.wr.flush();
            }
        } catch (IOException e) {
            this.listener.onException("Socket write error", e);
        }
    }

    /**
     * Sends command to server, flushing the write buffer after send.
     * 
     * @param command
     */
    public void sendCommand(String command) {
        this.sendCommand(command, true);
    }

    /**
     * Receives a line of XML from the server and parses it. Calls the
     * listener's methods when handled. Will block until a whole line is
     * received, so use a background thread!
     */
    public void doReceive() {
        if (this.wr == null) { // connection failed
            return;
        }
        try {
            Log.v("monopd", "monopd: Do receive");
            while (this.rd.ready()) {
                String line = this.rd.readLine();
                if (line == null) {
                    this.socketClosed = true;
                    this.listener.onClose(true);
                    return;
                } else {
                    this.pullParse(line);
                }
            }
        } catch (IOException e) {
            this.listener.onException("Socket read error", e);
            this.socketClosed = true;
            this.listener.onClose(true);
        }
    }

    /**
     * Handles a line of XML. Must be a single complete tag.
     * 
     * @param line
     *            The line to parse.
     */
    private void pullParse(String line) {
        int eventType;
        XmlNodeType nodeType = XmlNodeType.NONE;
        XmlNodeType prevReadNodeType = XmlNodeType.NONE;
        String attr, val;
        HashMap<String, String> data = new HashMap<String, String>();
        ArrayList<Object> l = new ArrayList<Object>();

        try {
            eventType = this.parser.getEventType();
        } catch (XmlPullParserException e) {
            this.listener.onException("XML parser error", e);
            return;
        }

        try {
            this.parser.setInput(new StringReader(line));
        } catch (XmlPullParserException e) {
            this.listener.onException("XML read error", e);
            return;
        }

        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_DOCUMENT) {
                Log.v("monopd", "xml: START DOCUMENT");
            } else if (eventType == XmlPullParser.START_TAG) {
                Log.v("monopd", "xml: START TAG " + this.parser.getName());
                nodeType = nodeByName.get(this.parser.getName());
                for (int index = 0; index < this.parser.getAttributeCount(); index++) {
                    attr = this.parser.getAttributeName(index);
                    val = this.parser.getAttributeValue(index);
                    data.put(attr, val);
                }
            } else if (eventType == XmlPullParser.END_TAG) {
                Log.v("monopd", "xml: END TAG " + this.parser.getName());
                nodeType = nodeByName.get(this.parser.getName());
                if (nodeType == null) {
                    Log.w("monopd", "Unhandled tag type: " + this.parser.getName());
                } else {
                    this.handleNode(nodeType, prevReadNodeType, data, l);
                }
                prevReadNodeType = nodeType;
            } else if (eventType == XmlPullParser.TEXT) {
                Log.v("monopd", "xml: TEXT TAG " + this.parser.getText());
            }
            try {
                eventType = this.parser.next();
            } catch (XmlPullParserException e) {
                this.listener.onException("XML read/format error", e);
                return;
            } catch (IOException e) {
                this.listener.onException("XML read I/O error", e);
                return;
            }
        }
        Log.v("monopd", "xml: END DOCUMENT");

        try {
            this.parser.setInput(null);
        } catch (XmlPullParserException e) {
            this.listener.onException("XML reset error", e);
            return;
        }
    }

    /**
     * Handles a node in a parse operation. Called when the end tag of any node
     * is reached, with a map containing the data in the node's attributes.
     * 
     * @param nodeType
     *            The type of node.
     * @param prevReadNodeType
     *            The type of node that was read last before this one. Used in
     *            some cases for context.
     * @param data
     *            The data map. The keys are the attributes. The special key
     *            "#text", if present, is the text contained in the current
     *            node. The methods called by this may manipulate the map to
     *            aggregate lists and other things I need to do. The methods
     *            shuld clear the data once an event is dispatched to the
     *            listener and the data is no longer needed.
     */
    private void handleNode(XmlNodeType nodeType, XmlNodeType prevReadNodeType, HashMap<String, String> data,
            ArrayList<Object> currentList) {
        MonoProtocolGameListener glistener = null;
        MonoProtocolMetaListener mlistener = null;
        if (this.type == MonoProtocolType.META) {
            mlistener = (MonoProtocolMetaListener) this.listener;
        } else {
            glistener = (MonoProtocolGameListener) this.listener;
        }
        switch (nodeType) {
        default:
            Log.w("monopd", "Unhandled tag " + nodeType.toString());
            break;
        case META_ATLANTIC:
            this.handleNodeMetaAtlantic(nodeType, prevReadNodeType, data, mlistener, currentList);
            break;
        case METASERVER:
            this.handleNodeMetaServer(nodeType, data, mlistener);
            break;
        case SERVERGAMELIST:
            this.handleNodeServerGameList(nodeType, data, mlistener, currentList);
            break;
        case GAME:
            if (this.type == MonoProtocolType.META) {
                // meta game list item
                this.handleNodeGameItem(nodeType, data, mlistener, currentList);
            } else {
                // in-game game update notification
                this.handleNodeGame(nodeType, data, glistener);
            }
            break;
        case MONOPD:
            // <monopd>[varied]</monopd>
            break;
        case SERVER:
            if (this.type == MonoProtocolType.META) {
                // meta server list item
                this.handleNodeServerItem(nodeType, data, mlistener, currentList);
            } else {
                // game server identification
                this.handleNodeServer(nodeType, data, glistener);
            }
            break;
        case CLIENT:
            this.handleNodeClient(nodeType, data, glistener);
            break;
        case PLAYERUPDATE:
            this.handleNodePlayerUpdate(nodeType, data, glistener);
            break;
        case GAMEUPDATE:
            this.handleNodeGameUpdate(nodeType, data, glistener);
            break;
        case CONFIGUPDATE:
            this.handleNodeConfigUpdate(nodeType, data, glistener, currentList);
            break;
        case OPTION:
            this.handleNodeOption(nodeType, data, glistener, currentList);
            break;
        case MSG:
            this.handleNodeMessage(nodeType, data, glistener);
            break;
        case DISPLAY:
            this.handleNodeDisplay(nodeType, data, glistener, currentList);
            break;
        case BUTTON:
            this.handleNodeButton(nodeType, data, glistener, currentList);
            break;
        case PLAYER:
            this.handleNodePlayer(nodeType, data, glistener, currentList);
            break;
        case UPDATEPLAYERLIST:
            this.handleNodeUpdatePlayerList(nodeType, data, glistener, currentList);
            break;
        case ESTATEUPDATE:
            this.handleNodeEstateUpdate(nodeType, data, glistener);
            break;
        case DELETEPLAYER:
            this.handleNodeDeletePlayer(nodeType, data, glistener);
            break;
        case ESTATEGROUPUPDATE:
            this.handleNodeEstateGroupUpdate(nodeType, data, glistener);
            break;
        case AUCTIONUPDATE:
            this.handleNodeAuctionUpdate(nodeType, data, glistener);
            break;
        case UPDATEGAMELIST:
            // do nothing, the contained <game> nodes will be parsed
            // also, there is a bug in the server where sometimes the type is set to "add" when "edit" is intended.     
            break;
        }
    }

    private void handleNodeDeletePlayer(XmlNodeType nodeType, HashMap<String, String> data,
            MonoProtocolGameListener glistener) {
        //<deleteplayer playerid="481"/>
        int playerId = getAttributeAsInt(data, "playerid");
        glistener.onPlayerDelete(playerId);
        data.clear();
    }

    /**
     * Gets an attribute from a map (constructed in pullParse) as an integer.
     * Passes an exception to the listener if integer not valid.
     * 
     * @param data
     *            The map.
     * @param attr
     *            The attribute name.
     * @return The integer value of the attribute.
     */
    private int getAttributeAsInt(HashMap<String, String> data, String attr) {
        try {
            if (data.containsKey(attr)) {
                return Integer.parseInt(data.get(attr).toString());
            } else {
                return Integer.MIN_VALUE;
            }
        } catch (NumberFormatException nfex) {
            this.listener.onException("Number format error", nfex);
            return Integer.MIN_VALUE;
        }
    }

    /**
     * Gets an attribute from a map (constructed in pullParse) as a String.
     * 
     * @param data
     *            The map.
     * @param attr
     *            The attribute name.
     * @return The String value of the attribute.
     */
    private String getAttributeAsString(HashMap<String, String> data, String attr) {
        if (data.containsKey(attr)) {
            return data.get(attr).toString();
        } else {
            return null;
        }
    }

    /**
     * Gets an attribute from a map (constructed in pullParse) as a boolean.
     * Returns false if the value is "0". Returns true in all other cases.
     * Passes an exception to the listener if not a valid integer.
     * 
     * @param map
     *            The map.
     * @param attr
     *            The attribute name.
     * @return The boolean value of the attribute.
     */
    private boolean getAttributeAsBoolean(HashMap<String, String> map, String attr) {
        return this.getAttributeAsInt(map, attr) != 0;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void handleNodeMetaAtlantic(XmlNodeType nodeType, XmlNodeType prevReadNodeType,
            HashMap<String, String> data, MonoProtocolMetaListener mlistener, ArrayList list) {
        if (prevReadNodeType == XmlNodeType.SERVERGAMELIST) {
            mlistener.onServerGameListEnd();
        } else if (prevReadNodeType == XmlNodeType.SERVERGAMELIST) {
            mlistener.onServerList(list);
            data.clear();
        } else {
            // was server identification, do not close
            return;
        }

        close();
    }

    public void close() {
        this.listener.onClose(false);
        try {
            this.socket.close();
        } catch (IOException e) {
        }
    }

    private void handleNodeMetaServer(XmlNodeType nodeType, HashMap<String, String> data,
            MonoProtocolMetaListener mlistener) {
        // <metaserver version="#.#.#"></metaserver>
        mlistener.onMetaServer(this.getAttributeAsString(data, "version"));
        data.clear();
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void handleNodeServerGameList(XmlNodeType nodeType, HashMap<String, String> data,
            MonoProtocolMetaListener mlistener, ArrayList list) {
        // <servergamelist host="monopd.gradator.net" port="#"
        // version="#.#.#"><game>[...]</servergamelist>
        mlistener.onServerGameList(this.getAttributeAsString(data, "host"), this.getAttributeAsInt(data, "port"),
                this.getAttributeAsString(data, "version"), list);
        list.clear();
        data.clear();
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void handleNodeGameItem(XmlNodeType nodeType, HashMap<String, String> data,
            MonoProtocolMetaListener mlistener, ArrayList list) {
        // <game id="162" players="1" gametype="city" name="Monopoly"
        // description="Ribose2's game" canbejoined="1"/>
        if (!data.containsKey("players")) {
            data.put("players", "0");
        }
        if (!data.containsKey("canbejoined")) {
            data.put("canbejoined", "1");
        }
        
        int id = getAttributeAsInt(data, "id");
        String host = getAttributeAsString(data, "host");
        int port = getAttributeAsInt(data, "port");
        String version = getAttributeAsString(data, "version");
        String type = getAttributeAsString(data, "gametype");
        String type_name = getAttributeAsString(data, "name");
        String descr = getAttributeAsString(data, "description");
        int players = getAttributeAsInt(data, "players");
        boolean can_be_joined = getAttributeAsBoolean(data, "canbejoined");
        
        if (id < 0) {
            ArrayList<ServerItem> servers = new ArrayList<ServerItem>();
            servers.add(new ServerItem(host, port, version, players));
            list.add(new GameItem(host, port, version, type, type_name, descr));
        } else {
            list.add(new GameItem(id, host, port, version, type, type_name, descr, players, can_be_joined));
        }

        data.remove("id");
        data.remove("gametype");
        data.remove("name");
        data.remove("description");
        data.remove("players");
        data.remove("canbejoined");
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void handleNodeServerItem(XmlNodeType nodeType, HashMap<String, String> data,
            MonoProtocolMetaListener mlistener, ArrayList list) {
        // <meta_server><server>[]</meta_server>
        // <server host="monopd.gradator.net" port="1230" version="0.8.3"
        // users="0" />
        String host = this.getAttributeAsString(data, "host");
        int port = this.getAttributeAsInt(data, "port");
        String version = this.getAttributeAsString(data, "version");
        int users = this.getAttributeAsInt(data, "users");

        list.add(new ServerItem(host, port, version, users));

        data.remove("host");
        data.remove("port");
        data.remove("version");
        data.remove("users");
    }

    private void handleNodeServer(XmlNodeType nodeType, HashMap<String, String> data, MonoProtocolGameListener glistener) {
        // <server version="0.8.3" />
        String version = this.getAttributeAsString(data, "version");
        glistener.onServer(version);
        data.clear();
    }

    private void handleNodeClient(XmlNodeType nodeType, HashMap<String, String> data, MonoProtocolGameListener glistener) {
        // <client playerid="422" cookie="422/1949477926"/>
        int playerId = this.getAttributeAsInt(data, "playerid");
        String cookie = this.getAttributeAsString(data, "cookie");
        glistener.onClient(playerId, cookie);
        data.clear();
    }

    private void handleNodePlayerUpdate(XmlNodeType nodeType, HashMap<String, String> data,
            MonoProtocolGameListener glistener) {
        int playerId = this.getAttributeAsInt(data, "playerid");
        data.remove("playerid");
        glistener.onPlayerUpdate(playerId, data);
        data.clear();
    }

    private void handleNodeGameUpdate(XmlNodeType nodeType, HashMap<String, String> data,
            MonoProtocolGameListener glistener) {
        int gameId = this.getAttributeAsInt(data, "gameid");
        if (data.containsKey("status")) {
            String status = this.getAttributeAsString(data, "status");
            glistener.onGameUpdate(gameId, status);
        }
        data.clear();
    }

    private void handleNodeOption(XmlNodeType nodeType, HashMap<String, String> data,
            MonoProtocolGameListener glistener, ArrayList<Object> list) {
        String title = this.getAttributeAsString(data, "title");
        String type = this.getAttributeAsString(data, "type");
        String command = this.getAttributeAsString(data, "command");
        String value = this.getAttributeAsString(data, "value");
        boolean editable = this.getAttributeAsBoolean(data, "edit");

        list.add(new Configurable(title, type, command, value, editable));

        data.remove("title");
        data.remove("type");
        data.remove("command");
        data.remove("value");
        data.remove("editable");
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void handleNodeConfigUpdate(XmlNodeType nodeType, HashMap<String, String> data,
            MonoProtocolGameListener glistener, ArrayList list) {
        glistener.onConfigUpdate(list);
        list.clear();
        data.clear();
    }

    private void handleNodeMessage(XmlNodeType nodeType, HashMap<String, String> data,
            MonoProtocolGameListener glistener) {
        String type = this.getAttributeAsString(data, "type");
        int playerId = this.getAttributeAsInt(data, "playerid");
        String author = this.getAttributeAsString(data, "author");
        String text = this.getAttributeAsString(data, "value");
        if (type != null) {
            if (type.equals("chat")) {
                glistener.onChatMessage(playerId, author, text);
            } else if (type.equals("error")) {
                glistener.onErrorMessage(text);
            } else if (type.equals("info")) {
                glistener.onInfoMessage(text);
            }
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void handleNodeDisplay(XmlNodeType nodeType, HashMap<String, String> data,
            MonoProtocolGameListener glistener, ArrayList list) {
        int estateId = this.getAttributeAsInt(data, "estateid");
        String text = this.getAttributeAsString(data, "text");
        boolean clearText = this.getAttributeAsBoolean(data, "cleartext");
        boolean clearButtons = this.getAttributeAsBoolean(data, "clearbuttons");
        glistener.onDisplayMessage(estateId, text, clearText, clearButtons, list);
        list.clear();
        data.clear();
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void handleNodeUpdatePlayerList(XmlNodeType nodeType, HashMap<String, String> data,
            MonoProtocolGameListener glistener, ArrayList list) {
        String type = this.getAttributeAsString(data, "type");
        glistener.onPlayerListUpdate(type, list);
        list.clear();
        data.clear();
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void handleNodePlayer(XmlNodeType nodeType, HashMap<String, String> data,
            MonoProtocolGameListener glistener, ArrayList list) {
        // <player playerid="680" name="Ribose"
        // host="dynamic-addr-90-116.resnet.rochester.edu" master="1" />
        int playerId = this.getAttributeAsInt(data, "playerid");
        String name = this.getAttributeAsString(data, "name");
        String host = this.getAttributeAsString(data, "host");
        boolean master = this.getAttributeAsBoolean(data, "master");

        Player player = new Player(playerId);
        player.setName(name);
        player.setHost(host);
        player.setMaster(master);
        list.add(player);

        data.remove("playerid");
        data.remove("name");
        data.remove("host");
        data.remove("master");
    }

    private void handleNodeGame(XmlNodeType nodeType, HashMap<String, String> data, MonoProtocolGameListener glistener) {
        // unused, we use gameupdate to get gameid
        // <game id="234" players="1" gametype="city" name="Monopoly" description="Ribose's game" canbejoined="1"/>
        int gameId = getAttributeAsInt(data, "id");
        int players = getAttributeAsInt(data, "players");
        String type = getAttributeAsString(data, "gametype");
        String type_name = getAttributeAsString(data, "name");
        String descr = getAttributeAsString(data, "description");
        boolean canBeJoined = getAttributeAsBoolean(data, "canbejoined");
        GameItem gameItem = new GameItem(gameId, null, 0, null, type, type_name, descr, players, canBeJoined);
        glistener.onGameItemUpdate(gameItem);
        data.clear();
    }

    private void handleNodeEstateUpdate(XmlNodeType nodeType, HashMap<String, String> data,
            MonoProtocolGameListener glistener) {
        int estateId = this.getAttributeAsInt(data, "estateid");
        data.remove("estateid");
        glistener.onEstateUpdate(estateId, data);
        data.clear();
    }

    private void handleNodeEstateGroupUpdate(XmlNodeType nodeType, HashMap<String, String> data,
            MonoProtocolGameListener glistener) {
        int estateGroupId = this.getAttributeAsInt(data, "groupid");
        data.remove("groupid");
        glistener.onEstateGroupUpdate(estateGroupId, data);
        data.clear();
    }

    private void handleNodeAuctionUpdate(XmlNodeType nodeType, HashMap<String, String> data,
            MonoProtocolGameListener glistener) {
        int auctionId = this.getAttributeAsInt(data, "auctionid");
        data.remove("auctionid");
        glistener.onAuctionUpdate(auctionId, data);
        data.clear();
    }
    
    private void handleNodeButton(XmlNodeType nodeType, HashMap<String, String> data,
            MonoProtocolGameListener glistener, ArrayList<Object> list) {
        String caption = this.getAttributeAsString(data, "caption");
        String command = this.getAttributeAsString(data, "command");
        boolean enabled = this.getAttributeAsBoolean(data, "enabled");
        list.add(new Button(caption, command, enabled));
    }

    public void disconnect() {
        if (socket != null) {
            try {
                this.socket.close();
            } catch (IOException e) {
            }
        }
    }

    public void setListener(MonoProtocolGameListener listener) {
        this.listener = listener;
    }

    /**
     * Send meta game list request.
     */
    public void sendMetaListGames() {
        if (this.type == MonoProtocolType.META) {
            this.sendCommand("CHECKCLIENT" + this.client + this.version, false);
            this.sendCommand("GAMELIST");
        } else {
            this.listener.onException("Incorrect monopd handler type", new Exception());
        }
    }

    /**
     * Send meta server list request.
     */
    public void sendMetaListServers() {
        if (this.type == MonoProtocolType.META) {
            this.sendCommand("CHECKCLIENT" + this.client + this.version, false);
            this.sendCommand("SERVERLIST");
        } else {
            this.listener.onException("Incorrect monopd handler type", new Exception());
        }
    }

    /**
     * Send client hello. This message is buffered until another message is
     * sent.
     */
    public void sendClientHello() {
        this.sendCommand("client name=\"" + this.client + "\" version=\"" + this.version + "\" protocol=\"monopd\"",
                false);
    }
    
    public void sendChangeNick(String nick, boolean doFlush) {
        this.sendCommand(".n" + nick, doFlush);
    }
    
    public void sendChangeNick(String nick) {
        this.sendChangeNick(nick, true);
    }
    
    public void sendChangeConfiguration(String command, String value) {
        this.sendCommand(command + value);
    }
    
    public void sendReconnect(String cookie) {
        this.sendCommand(".R" + cookie);
    }
    
    public void sendTurnIndicator(int estateId) {
        this.sendCommand(".t" + estateId);
    }

    public void sendDeclareBankrupcy() {
        this.sendCommand(".D");
    }
    
    public void sendRoll() {
        this.sendCommand(".r");
    }
    
    public void sendStartGame() {
        this.sendCommand(".gs");
    }
    
    public void sendQuitGame() {
        this.sendCommand(".gx");
    }
    
    public void sendCreateGame(String type) {
        this.sendCommand(".gn" + type);
    }
    
    public void sendJoinGame(int gameId) {
        this.sendCommand(".gj" + gameId);
    }

    public void sendToggleMortgage(int estateId) {
        this.sendCommand(".em" + estateId);
    }
    
    public void sendButtonCommand(String command) {
        this.sendCommand(command);
    }

    public void sendBuyHouse(int estateId) {
        this.sendCommand(".hb" + estateId);
    }

    public void sendSellHouse(int estateId) {
        this.sendCommand(".hs" + estateId);
    }

    public void sendAuctionBid(int auctionId, int bid) {
        this.sendCommand(".ab" + auctionId + ":" + bid);
    }
}
