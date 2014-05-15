package edu.rochester.nbook.monopdroid.board;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.text.DateFormat;

import edu.rochester.nbook.monopdroid.R;
import edu.rochester.nbook.monopdroid.SettingsActivity;
import edu.rochester.nbook.monopdroid.board.surface.BoardView;
import edu.rochester.nbook.monopdroid.board.surface.BoardViewListener;
import edu.rochester.nbook.monopdroid.board.surface.BoardViewPiece;
import edu.rochester.nbook.monopdroid.board.surface.BoardViewSurfaceThread;
import edu.rochester.nbook.monopdroid.board.surface.BoardViewPiece.For;
import edu.rochester.nbook.monopdroid.gamelist.GameItem;
import edu.rochester.nbook.monopdroid.gamelist.GameItemType;
import edu.rochester.nbook.monopdroid.gamelist.ServerItem;
import edu.rochester.nbook.monopdroid.monopd.MonoProtocolGameListener;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.text.SpannableString;
import android.text.SpannedString;
import android.text.style.StyleSpan;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class BoardActivity extends FragmentActivity {
    
    private static final HashMap<String, XmlAttribute> playerAttributes = new HashMap<String, XmlAttribute>() {
        private static final long serialVersionUID = 1431923100451372984L;

        {
            this.put("name", new XmlAttribute(Player.class, "setName", XmlAttributeType.STRING));
            this.put("host", new XmlAttribute(Player.class, "setHost", XmlAttributeType.STRING));
            this.put("master", new XmlAttribute(Player.class, "setMaster", XmlAttributeType.BOOLEAN));
            this.put("money", new XmlAttribute(Player.class, "setMoney", XmlAttributeType.INT));
            this.put("doublecount", new XmlAttribute(Player.class, "setDoubleCount", XmlAttributeType.INT));
            this.put("jailcount", new XmlAttribute(Player.class, "setJailCount", XmlAttributeType.INT));
            this.put("bankrupt", new XmlAttribute(Player.class, "setBankrupt", XmlAttributeType.BOOLEAN));
            this.put("jailed", new XmlAttribute(Player.class, "setJailed", XmlAttributeType.BOOLEAN));
            this.put("hasturn", new XmlAttribute(Player.class, "setHasTurn", XmlAttributeType.BOOLEAN));
            this.put("spectator", new XmlAttribute(Player.class, "setSpectator", XmlAttributeType.BOOLEAN));
            this.put("can_roll", new XmlAttribute(Player.class, "setCanRoll", XmlAttributeType.BOOLEAN));
            this.put("canrollagain", new XmlAttribute(Player.class, "setCanRollAgain", XmlAttributeType.BOOLEAN));
            this.put("can_buyestate", new XmlAttribute(Player.class, "setCanBuyEstate", XmlAttributeType.BOOLEAN));
            this.put("canauction", new XmlAttribute(Player.class, "setCanAuction", XmlAttributeType.BOOLEAN));
            this.put("canusecard", new XmlAttribute(Player.class, "setCanUseCard", XmlAttributeType.BOOLEAN));
            this.put("hasdebt", new XmlAttribute(Player.class, "setHasDebt", XmlAttributeType.BOOLEAN));
            this.put("location", new XmlAttribute(Player.class, "setLocation", XmlAttributeType.INT));
            this.put("directmove", new XmlAttribute(Player.class, "setDirectMove", XmlAttributeType.BOOLEAN));
            this.put("game", new XmlAttribute(Player.class, "setGameId", XmlAttributeType.INT));
            this.put("cookie", new XmlAttribute(Player.class, null, XmlAttributeType.STRING));
            this.put("image", new XmlAttribute(Player.class, null, XmlAttributeType.STRING));
        }
    };
    
    private static final HashMap<String, XmlAttribute> estateAttributes = new HashMap<String, XmlAttribute>() {
        private static final long serialVersionUID = -1649097477143814788L;

        {
            this.put("name", new XmlAttribute(Estate.class, "setName", XmlAttributeType.STRING));
            this.put("houses", new XmlAttribute(Estate.class, "setHouses", XmlAttributeType.INT));
            this.put("money", new XmlAttribute(Estate.class, "setMoney", XmlAttributeType.INT));
            this.put("price", new XmlAttribute(Estate.class, "setPrice", XmlAttributeType.INT));
            this.put("mortgageprice", new XmlAttribute(Estate.class, "setMortgagePrice", XmlAttributeType.INT));
            this.put("unmortgageprice", new XmlAttribute(Estate.class, "setUnmortgagePrice", XmlAttributeType.INT));
            this.put("sellhouseprice", new XmlAttribute(Estate.class, "setSellHousePrice", XmlAttributeType.INT));
            this.put("mortgaged", new XmlAttribute(Estate.class, "setMortgaged", XmlAttributeType.BOOLEAN));
            this.put("color", new XmlAttribute(Estate.class, "setColor", XmlAttributeType.COLOR));
            this.put("bgcolor", new XmlAttribute(Estate.class, "setBgColor", XmlAttributeType.COLOR));
            this.put("owner", new XmlAttribute(Estate.class, "setOwner", XmlAttributeType.INT));
            this.put("houseprice", new XmlAttribute(Estate.class, "setHousePrice", XmlAttributeType.INT));
            this.put("groupid", new XmlAttribute(Estate.class, "setEstateGroup", XmlAttributeType.INT));
            this.put("group", this.get("groupid"));
            this.put("can_be_owned", new XmlAttribute(Estate.class, "setCanBeOwned", XmlAttributeType.BOOLEAN));
            this.put("can_toggle_mortgage", new XmlAttribute(Estate.class, "setCanToggleMortgage", XmlAttributeType.BOOLEAN));
            this.put("can_buy_houses", new XmlAttribute(Estate.class, "setCanBuyHouses", XmlAttributeType.BOOLEAN));
            this.put("can_sell_houses", new XmlAttribute(Estate.class, "setCanSellHouses", XmlAttributeType.BOOLEAN));
            this.put("rent0", new XmlAttribute(Estate.class, "setRent0", XmlAttributeType.RENT));
            this.put("rent1", new XmlAttribute(Estate.class, "setRent1", XmlAttributeType.RENT));
            this.put("rent2", new XmlAttribute(Estate.class, "setRent2", XmlAttributeType.RENT));
            this.put("rent3", new XmlAttribute(Estate.class, "setRent3", XmlAttributeType.RENT));
            this.put("rent4", new XmlAttribute(Estate.class, "setRent4", XmlAttributeType.RENT));
            this.put("rent5", new XmlAttribute(Estate.class, "setRent5", XmlAttributeType.RENT));
            this.put("passmoney", new XmlAttribute(Estate.class, "setPassMoney", XmlAttributeType.INT));
            this.put("taxpercentage", new XmlAttribute(Estate.class, "setTaxPercentage", XmlAttributeType.INT));
            this.put("tax", new XmlAttribute(Estate.class, "setTax", XmlAttributeType.INT));
            this.put("icon", new XmlAttribute(Estate.class, "setIcon", XmlAttributeType.STRING));
            this.put("jail", new XmlAttribute(Estate.class, "setIsJail", XmlAttributeType.BOOLEAN));
            this.put("payamount", new XmlAttribute(Estate.class, "setPayAmount", XmlAttributeType.INT));
            this.put("tojail", new XmlAttribute(Estate.class, "setIsToJail", XmlAttributeType.BOOLEAN));
        }
    };
    
    private static final HashMap<String, XmlAttribute> estateGroupAttributes = new HashMap<String, XmlAttribute>() {
        private static final long serialVersionUID = -4145245059753235694L;

        {
            this.put("name", new XmlAttribute(EstateGroup.class, "setName", XmlAttributeType.STRING));
        }
    };
    
    private static final HashMap<String, XmlAttribute> auctionAttributes = new HashMap<String, XmlAttribute>() {
        private static final long serialVersionUID = 2978911583408943533L;

        {
            this.put("actor", new XmlAttribute(Auction.class, "setActorId", XmlAttributeType.INT));
            this.put("estateid", new XmlAttribute(Auction.class, "setEstateId", XmlAttributeType.INT));
            this.put("status", new XmlAttribute(Auction.class, "setStatus", XmlAttributeType.INT));
            this.put("highbid", new XmlAttribute(Auction.class, "setHighBid", XmlAttributeType.INT));
            this.put("highbidder", new XmlAttribute(Auction.class, "setHighBidder", XmlAttributeType.INT));
        }
    };
    
    /**
     * The Board UI. Do not access from networking thread.
     */
    private BoardView boardView = null;
    /**
     * The chat log. Do not access from networking thread.
     */
    private ListView chatList = null;
    /**
     * The chat log adapter. Do not access from networking thread.
     */
    private ChatListAdapter chatListAdapter = null;
    /**
     * The chat send box. Do not access from networking thread.
     */
    private EditText chatSendBox = null;
    /**
     * The player views. Do not access from networking thread.
     */
    private LinearLayout[] playerView = new LinearLayout[4];
    /**
     * The networking handler. Used to send messages to the networking thread.
     */
    private Handler netHandler = null;
    /**
     * The networking thread runner (created in UI thread). Do not access from
     * networking thread.
     */
    private BoardActivityNetworkThread netRunnable = null;

    /**
     * This game item.
     */
    private GameItem gameItem = null;
    /**
     * Array of players to show in the 4 slots.
     */
    private int[] playerIds = new int[4];
    /**
     * List of players.
     */
    private SparseArray<Player> players = new SparseArray<Player>();
    /**
     * List of estates.
     */
    private ArrayList<Estate> estates = new ArrayList<Estate>(40);
    /**
     * List of estate groups.
     */
    private SparseArray<EstateGroup> estateGroups = new SparseArray<EstateGroup>();
    /**
     * Auction. Even though they have an auctionId, it is always 0.
     */
    private Auction auction = new Auction(0);
    /**
     * List of options.
     */
    private ArrayList<Configurable> configurables = new ArrayList<Configurable>();
    /**
     * List of buttons.
     */
    private ArrayList<Button> buttons = new ArrayList<Button>();
    /**
     * Current player ID.
     */
    private int playerId = 0;
    /**
     * Current player cookie.
     */
    private String cookie = null;
    /**
     * Game status.
     */
    private GameStatus status = GameStatus.ERROR;
    /**
     * Client name.
     */
    private String clientName;
    /**
     * Client version.
     */
    private String clientVersion;
    /**
     * Current nick name.
     */
    private String nickname;
    /**
     * Whether we are the master of this game lobby.
     */
    private boolean isMaster = false;
    
    private int master = 0;
    /**
     * Whether this onDestroy() occured after saving state.
     */
    private boolean savingState = false;
    /**
     * Whether this onResume() occured with intent info (true) or saved state data (false).
     */
    private boolean firstInit = false;
    
    private boolean running = false;
    
    private String playerLeavingNick = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Log.d("monopd", "board: BoardActivity start");
        
        this.setContentView(R.layout.board);
        
        storeViews();
        
        BoardActivityState state = (BoardActivityState) getLastCustomNonConfigurationInstance();
        
        firstInit = (state == null);
        
        if (firstInit) {
            Intent i = this.getIntent();
            int game_id = i.getIntExtra("edu.rochester.nbook.game_id", 0);
            String host = i.getStringExtra("edu.rochester.nbook.host");
            int port = i.getIntExtra("edu.rochester.nbook.port", 0);
            String version = i.getStringExtra("edu.rochester.nbook.version");
            String type = i.getStringExtra("edu.rochester.nbook.type");
            String type_name = i.getStringExtra("edu.rochester.nbook.type_name");
            String descr = i.getStringExtra("edu.rochester.nbook.descr");
            int playerCount = i.getIntExtra("edu.rochester.nbook.players", 0);
            boolean can_join = i.getBooleanExtra("edu.rochester.nbook.can_join", false);
            GameItemType item_type = GameItemType.fromInt(i.getIntExtra("edu.rochester.nbook.act_type", 0));
            // check can_join value
            if (!can_join) {
                this.finish();
                return;
            }
            // check item type valid
            switch (item_type) {
            default:
            case ERROR:
            case READY:
            case LOADING:
            case EMPTY:
                // failure
                this.finish();
                return;
            case CREATE:
            case JOIN:
            case RECONNECT:
                // success
                break;
            }
            ArrayList<ServerItem> servers = new ArrayList<ServerItem>();
            servers.add(new ServerItem(host, port, version, playerCount));
            this.gameItem = new GameItem(item_type, game_id, servers, type, type_name, descr, playerCount, can_join);
            this.isMaster = gameItem.getItemType() == GameItemType.CREATE;
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            this.clientName = "monopdroid";
            try {
                this.clientVersion = this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName;
            } catch (NameNotFoundException e) {
                this.clientVersion = "0.0.0";
            }
            this.nickname = prefs.getString("player_nick", "anonymous");
            
            this.netRunnable = new BoardActivityNetworkThread();
        } else {
            boardView.setSurfaceRunner(state.surfaceRunner);
            chatListAdapter.restoreState(state);
            playerIds = state.playerIds;
            netRunnable = state.netRunnable;
            gameItem = state.gameItem;
            estates = state.estates;
            players = state.players;
            configurables = state.configurables;
            playerId = state.playerId;
            cookie = state.cookie;
            status = state.status;
            clientName = state.clientName;
            clientVersion = state.clientVersion;
            nickname = state.nickname;
            isMaster = state.isMaster;
        }
        
        attachListeners();

        Log.d("monopd", "board: Completed activity set-up");
    }
    
    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        BoardActivityState state = new BoardActivityState();
        state.surfaceRunner = boardView.getSurfaceRunner();
        state.chat = chatListAdapter.saveState();
        state.playerIds = playerIds;
        state.netRunnable = netRunnable;
        state.gameItem = gameItem;
        state.players = players;
        state.estates = estates;
        state.configurables = configurables;
        state.playerId = playerId;
        state.cookie = cookie;
        state.status = status;
        state.clientName = clientName;
        state.clientVersion = clientVersion;
        state.nickname = nickname;
        state.isMaster = isMaster;
        savingState = true;
        return state;
    }
    
    public class BoardActivityState {
        public BoardViewSurfaceThread surfaceRunner;
        
        public ArrayList<ChatItem> chat;
        public int[] playerIds = new int[4];
        public BoardActivityNetworkThread netRunnable;
        public GameItem gameItem;
        public SparseArray<Player> players;
        public ArrayList<Estate> estates;
        public ArrayList<Configurable> configurables;
        public int playerId;
        public String cookie;
        public GameStatus status;
        public String clientName;
        public String clientVersion;
        public String nickname;
        public boolean isMaster;
    }

    private void showConnectionError(String error) {
        Bundle state = new Bundle();
        state.putString("error", error);
        if (running) {
            DialogFragment connErrorDialog = new DialogFragment() {
                @Override
                public Dialog onCreateDialog(Bundle savedInstanceState) {
                    return new AlertDialog.Builder(BoardActivity.this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle(R.string.dialog_conn_error)
                    .setMessage(getArguments().getString("error"))
                    .setPositiveButton(R.string.reconnect, new OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            finish();
                        }
                    })
                    .setNegativeButton(R.string.quit, new OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            sendToNetThread(BoardNetworkAction.MSG_SOCKET_RECONNECT, null);
                        }
                    })
                    .create();
                }
            };
            connErrorDialog.setArguments(state);
            connErrorDialog.show(getSupportFragmentManager(), "conn_error");
        }
    }
    
    private void attachListeners() {
        this.boardView.setBoardViewListener(new BoardViewListener() {

            @Override
            public void onConfigChange(String command, String value) {
                Log.d("monopd", "BoardView tapped config change " + command + " = " + value);
                Bundle state = new Bundle();
                state.putString("command", command);
                state.putString("value", value);
                BoardActivity.this.sendToNetThread(BoardNetworkAction.MSG_CONFIG, state);
            }

            @Override
            public void onStartGame() {
                Log.d("monopd", "BoardView tapped start game");
                BoardActivity.this.sendToNetThread(BoardNetworkAction.MSG_GAME_START, null);
            }

            @Override
            public void onRoll() {
                sendToNetThread(BoardNetworkAction.MSG_ROLL, null);
            }

            @Override
            public void onResize(int width, int height) {
                Log.d("monopd", "BoardView resized to " + width + "," + height);
                if (firstInit) {
                    // first init
                    switch (gameItem.getItemType()) {
                    case JOIN:
                        status = GameStatus.JOIN;
                        boardView.setStatus(GameStatus.JOIN);
                        writeMessage("Joining " + gameItem.getDescription() + "...", Color.YELLOW, -1, -1);
                        break;
                    case CREATE:
                        status = GameStatus.CREATE;
                        boardView.setStatus(GameStatus.CREATE);
                        writeMessage("Creating " + gameItem.getTypeName() + " game...", Color.YELLOW, -1, -1);
                        break;
                    case RECONNECT:
                        status = GameStatus.RECONNECT;
                        boardView.setStatus(GameStatus.RECONNECT);
                        writeMessage("Reconnecting to " + gameItem.getDescription(), Color.YELLOW, -1, -1);
                        break;
                    default:
                        break;
                    }
                    
                    Thread netThread = new Thread(netRunnable);
                    netThread.start();
                    firstInit = false;
                } else {
                    // re-init
                    sendToNetThread(BoardNetworkAction.MSG_RESUME, null);
                    updatePlayerView();
                }
                redrawRegions();
            }

            @Override
            public void onEstateClick(int estateId) {
                BoardActivity.this.boardView.overlayEstateInfo(estates, estateId, playerId);
            }

            @Override
            public void onCloseOverlay() {
                boardView.closeOverlay();
            }

            @Override
            public void onOpenTradeWindow(int playerId) {
                // TODO: implement trade
            }

            @Override
            public void onPlayerCommandPing(int playerId) {
                sendCommand("!ping " + players.get(playerId).getName());
            }

            @Override
            public void onPlayerCommandDate(int playerId) {
                sendCommand("!date " + players.get(playerId).getName());
            }

            @Override
            public void onPlayerCommandVersion(int playerId) {
                sendCommand("!version " + players.get(playerId).getName());
            }

            @Override
            public void onToggleMortgage(int estateId) {
                Bundle args = new Bundle();
                args.putInt("estateId", estateId);
                sendToNetThread(BoardNetworkAction.MSG_ESTATE_MORTGAGE, args);
            }

            @Override
            public void onBuyHouse(int estateId) {
                Bundle args = new Bundle();
                args.putInt("estateId", estateId);
                sendToNetThread(BoardNetworkAction.MSG_ESTATE_BUYHOUSE, args);
            }

            @Override
            public void onSellHouse(int estateId) {
                Bundle args = new Bundle();
                args.putInt("estateId", estateId);
                sendToNetThread(BoardNetworkAction.MSG_ESTATE_SELLHOUSE, args);
            }

            @Override
            public void onBid(int auctionId, int raise) {
                if (raise > 0) {
                    int amount = auction.getHighBid() + raise;
                    Bundle args = new Bundle();
                    args.putInt("auctionId", auctionId);
                    args.putInt("bid", amount);
                    sendToNetThread(BoardNetworkAction.MSG_AUCTION_BID, args);
                } else {
                    // "+" command: prompt for amount
                }
            }

            @Override
            public String getPlayerBodyText(int playerId) {
                Player player = players.get(playerId);
                StringBuilder sb = new StringBuilder();
                BoardViewPiece playerPiece = BoardViewPiece.getPiece(playerId);
                int playerPieceColor = Color.WHITE;
                if (playerPiece != null) {
                    playerPieceColor = playerPiece.getColor();
                }
                sb.append("<b><u><font color=\"#" + getHtmlColor(playerPieceColor) + "\">" + player.getName() + "</font></u></b><br>");
                //sb.append("Player ID: " + player.getPlayerId() + "\n");
                if (player.isMaster() && status == GameStatus.CONFIG) {
                    sb.append("<i>This player created this game.</i><br>");
                }
                if (player.isTurn()) {
                    sb.append("<i>It is this player's turn.</i><br>");
                }
                if (player.getHost() != null) {
                    sb.append("<b>Host:</b> " + player.getHost() + "<br>");
                }
                if (status == GameStatus.RUN) {
                    sb.append("<b>Money:</b> $" + player.getMoney() + "<br>");
                    sb.append("<b>On estate:</b> " + estates.get(player.getLocation()).getName() + "<br>");
                    int owned = 0;
                    int mortgaged = 0;
                    int houses = 0;
                    int hotels = 0;
                    int completeGroups = 0;
                    SparseIntArray incompleteGroupMap = new SparseIntArray();
                    SparseIntArray groupMap = new SparseIntArray();
                    for (Estate est : estates) {
                        int groupId = est.getEstateGroup();
                        EstateGroup group = estateGroups.get(groupId);
                        if (group != null) {
                            groupMap.put(groupId, groupMap.get(groupId) + 1);
                        }
                    }
                    for (Estate est : estates) {
                        int groupId = est.getEstateGroup();
                        EstateGroup group = estateGroups.get(groupId);
                        if (group != null) {
                            int subsetOwned = incompleteGroupMap.get(groupId);
                            incompleteGroupMap.put(groupId, subsetOwned++);
                            if (subsetOwned == groupMap.get(groupId)) {
                                completeGroups++;
                            }
                        }
                        if (est.getOwner() == player.getPlayerId()) {
                            owned++;
                            if (est.getHouses() < 5) {
                                houses += est.getHouses();
                            } else {
                                hotels++;
                            }
                            if (est.isMortgaged()) {
                                mortgaged++;
                            }
                        }
                    }
                    sb.append("<b>Owned estates:</b> " + owned + "<br>");
                    sb.append("<b>Owned estates mortgaged:</b> " + mortgaged + "<br>");
                    sb.append("<b>Complete estate sets:</b> " + completeGroups + "<br>");
                    sb.append("<b>Owned houses:</b> " + houses + "<br>");
                    sb.append("<b>Owned hotels:</b> " + hotels + "<br>");
                }
                return sb.toString();
            }

            @Override
            public String getEstateBodyText(int estateId) {
                Estate estate = estates.get(estateId);
                StringBuilder sb = new StringBuilder();
                int estateColor = Color.WHITE;
                if (estate.getColor() != 0) {
                    estateColor = estate.getColor();
                }
                sb.append("<b><font color=\"#" + getHtmlColor(estateColor) + "\">" + estate.getName() + "</font></b><br>");
                //sb.append("Estate ID: " + estate.getEstateId() + "\n");
                //EstateGroup group = estateGroups.get(estate.getEstateGroup());
                if (estate.canBeOwned()) {
                    if (estate.getOwner() <= 0) {
                        sb.append("<b>Owner:</b> <i>none</i><br>");
                    } else {
                        sb.append("<b>Owner:</b> " + players.get(estate.getOwner()).getName() + "<br>");
                    }
                    sb.append("<b>Price to buy:</b> $" + estate.getPrice() + "<br>");
                    if (estate.getMortgagePrice() > 0) {
                        sb.append("<b>Price to mortgage:</b> $" + estate.getMortgagePrice() + "<br>");
                        sb.append("<b>Price to unmortgage:</b> $" + estate.getUnmortgagePrice() + "<br>");
                    }
                    if (estate.getColor() != 0) {
                        sb.append("<b>Houses:</b> " + estate.getHouses() + "<br>");
                        sb.append("<b>Rent (houses):</b> 0:$" + estate.getRent(0) + ", 1:$" + estate.getRent(1) + ", 2:$" + estate.getRent(2) + ", 3:$" + estate.getRent(3) + ", 4:$" + estate.getRent(4) + ", 5:$" + estate.getRent(5) + "<br>");
                        sb.append("<b>Price to buy house:</b> $" + estate.getHousePrice() + "<br>");
                        sb.append("<b>Price to sell house:</b> $" + estate.getSellHousePrice() + "<br>");
                    }
                }
                if (estate.getPassMoney() > 0) {
                    sb.append("<b>On-pass money:</b> $" + estate.getPassMoney() + "<br>");
                }
                if (estate.getTax() > 0) {
                    sb.append("<b>Tax amount:</b> $" + estate.getTax() + "<br>");
                }
                if (estate.getTaxPercentage() > 0) {
                    sb.append("<b>Tax percent:</b> " + estate.getTaxPercentage() + " %<br>");
                }
                if (estate.isJail()) {
                    sb.append("<i>Is jail</i><br>");
                }
                if (estate.isToJail()) {
                    sb.append("<i>Go to jail</i><br>");
                }
                return sb.toString();
            }

            @Override
            public String getAuctionBodyText(int auctionId) {
                StringBuilder sb = new StringBuilder();
                Estate estate = estates.get(auction.getEstateId());
                sb.append("<b><u>Auction of " + estate.getName() + "</u></b><br>");
                switch (auction.getStatus()) {
                case 0:
                    break;
                case 1:
                    sb.append("<font color=\"#ffff00\"><b>GOING ONCE</b></font><br>");
                    break;
                case 2:
                    sb.append("<font color=\"#ff8800\"><b>GOING TWICE</b></font><br>");
                    break;
                case 3:
                    sb.append("<font color=\"#ff0000\"><b>SOLD</b></font><br>");
                    break;
                }
                sb.append("<b>Auctioned by:</b> " + players.get(auction.getActorId()).getName() + "<br>");
                if (auction.getHighBidder() == 0) {
                    sb.append("<b>Current bid:</b> <i>none yet</i><br>");
                } else {
                    sb.append("<b>Current bid:</b> $" + auction.getHighBid() + "<br>");
                    sb.append("<b>Bid by:</b> " + players.get(auction.getHighBidder()).getName() + "<br>");
                }
                return sb.toString();
            }

            @Override
            public void onButtonCommand(String command) {
                Bundle args = new Bundle();
                args.putString("command", command);
                sendToNetThread(BoardNetworkAction.MSG_BUTTON_COMMAND, args);
            }
        });
        this.chatSendBox.setOnKeyListener(new OnKeyListener() {

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_ENTER) {
                    String text = BoardActivity.this.chatSendBox.getText().toString();
                    if (text.length() > 0) {
                        BoardActivity.this.sendCommand(text);
                        BoardActivity.this.chatSendBox.setText("");
                    }
                }
                return false;
            }
        });
        this.chatList.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> listView, View itemView, int position, long id) {
                ChatItem item = BoardActivity.this.chatListAdapter.getItem(position);
                if (item.getPlayerId() > 0) {
                    BoardActivity.this.boardView.overlayPlayerInfo(item.getPlayerId());
                } else if (item.getEstateId() > 0) {
                    BoardActivity.this.boardView.overlayEstateInfo(estates, item.getEstateId(), playerId);
                }
            }
        });
        
        this.netRunnable.setActivity(this, new MonoProtocolGameListener() {
            @Override
            public void onException(String description, Exception ex) {
                Log.v("monopd", "net: Received onException() from MonoProtocolHandler");
                showConnectionError(description + ": " + ex.getMessage());
            }

            @Override
            public void onClose(boolean remote) {
                Log.d("monopd", "net: onClose(" + remote + ")");
                if (remote) {
                    showConnectionError("Connection lost.");
                }
            }

            @Override
            public void onServer(String version) {
                Log.v("monopd", "net: Received onServer() from MonoProtocolHandler");
            }

            @Override
            public void onClient(int playerId, String cookie) {
                Log.v("monopd", "net: Received onClient() from MonoProtocolHandler");
                BoardActivity.this.playerId = playerId;
                BoardActivity.this.cookie = cookie;
            }

            @Override
            public void onPlayerUpdate(final int playerId, HashMap<String, String> data) {
                Log.v("monopd", "net: Received onPlayerUpdate() from MonoProtocolHandler");
                final HashMap<String, String> map = new HashMap<String, String>(data);
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        boolean found = false;
                        boolean isJoin = false;
                        String oldNick = null;
                        boolean changingLocation = false;
                        boolean changingMaster = false;
                        boolean wasMaster = false;
                        // get player from internal data
                        Player player = players.get(playerId);
                        // if not found, add internally
                        if (player == null) {
                            player = new Player(playerId);
                        }
                        // update internal player data
                        for (String key : map.keySet()) {
                            String value = map.get(key);
                            XmlAttribute attr = playerAttributes.get(key);
                            if (attr == null) {
                                Log.w("monopd", "player." + key + " was unknown. Value = " + value);
                            } else {
                                if (key.equals("location")) {
                                    changingLocation = true;
                                }
                                if (key.equals("name")) {
                                    if (!value.equals(player.getName())) {
                                        oldNick = player.getName();
                                    }
                                    if (player.getPlayerId() == BoardActivity.this.playerId) {
                                        nickname = value;
                                    }
                                }
                                if (key.equals("master")) {
                                    changingMaster = true;
                                    wasMaster = player.isMaster();
                                }
                                attr.set(player, value);
                            }
                        }
                        // put internal player data
                        players.put(playerId, player);
                        // stop here if this player is not in this game (could be in the future)
                        if (player.getGameId() != gameItem.getGameId()) {
                            // this player is not in this game
                            return;
                        }
                        // find in player list
                        for (int i = 0; i < 4; i++) {
                            if (playerIds[i] == playerId) {
                                found = true;
                            }
                        }
                        // add to player list on nick seen
                        if (!found) {
                            if (player.getName() != null) {
                                switch (status) {
                                case ERROR:
                                case RECONNECT:
                                case CREATE:
                                case JOIN:
                                    // ignore playerupdates before game state change!
                                    return;
                                case CONFIG:
                                case INIT:
                                    // add to player list
                                    for (int i = 0; i < 4; i++) {
                                        if (playerIds[i] == 0) {
                                            playerIds[i] = playerId;
                                            isJoin = true;
                                            break;
                                        }
                                    }
                                    break;
                                case RUN:
                                    // do not update player list
                                    return;
                                }
                            }
                        }
                        // update player view
                        updatePlayerView();
                        // redraw for possible new colors
                        boardView.drawActionRegions(estates, playerIds, players, buttons, playerId);
                        // redraw overlay for possible new data
                        boardView.redrawOverlay();
                        // join message
                        if (isJoin) {
                            // before estate data, is in-game list
                            if (estates.size() == 0) {
                                // indicate master
                                if (player.isMaster()) {
                                    writeMessage(player.getName() + " is in the game as game master.", Color.GRAY, playerId, -1);
                                    wasMaster = true;
                                } else {
                                    writeMessage(player.getName() + " is in the game.", Color.GRAY, playerId, -1);
                                }
                            } else {
                                // normal join
                                writeMessage(player.getName() + " joined the game.", Color.GRAY, playerId, -1);
                            }
                        } else if (oldNick != null) {
                            // nick changed
                            writeMessage(oldNick + " is now known as " + player.getName() + ".", Color.GRAY, playerId, -1);
                        }
                        if (changingLocation) {
                            animateMove(player);
                        }
                        if (changingMaster) {
                            if (player.getPlayerId() == BoardActivity.this.playerId && player.isMaster() != isMaster) {
                                isMaster = player.isMaster();
                                if (player.isMaster()) {
                                    master = player.getPlayerId();
                                }
                            }
                            if (player.isMaster() && !wasMaster) {
                                if (playerLeavingNick != null) {
                                    writeMessage(playerLeavingNick + " left the game giving game master to " + player.getName() + ".", Color.GRAY, playerId, -1);
                                    playerLeavingNick = null;
                                } else {
                                    if (master > 0) {
                                        playerLeavingNick = player.getName();
                                    } else {
                                        writeMessage(player.getName() + " is game master.", Color.GRAY, playerId, -1);
                                    }
                                }
                            }
                        }
                    }
                });
            }

            @Override
            public void onPlayerDelete(final int playerId) {
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        boolean deleted = false;
                        for (int i = 0; i < 4; i++) {
                            if (playerIds[i] == playerId) {
                                deleted = true;
                            }
                            if (deleted) {
                                if (i < 3) {
                                    playerIds[i] = playerIds[i + 1];
                                } else {
                                    playerIds[i] = 0;
                                    /*for (int j = 0; j < players.size(); j++) {
                                        int playerIdJ = players.keyAt(j);
                                        boolean foundPlayerJ = false;
                                        for (int k = 0; k < 4; k++) {
                                            if (playerIdJ == playerIds[k] || players.get(playerIdJ).getNick().equals("_metaserver_")) {
                                                foundPlayerJ = true;
                                            }
                                        }
                                        if (!foundPlayerJ) {
                                            playerIds[i] = playerIdJ;
                                            break;
                                        }
                                    }*/
                                }
                            }
                        }
                        if (players.get(playerId) != null) {
                            if (status == GameStatus.CONFIG && players.get(playerId).getGameId() == gameItem.getGameId()) {
                                if (master == playerId) {
                                    playerLeavingNick = players.get(playerId).getName();
                                    master = 0;
                                } else {
                                    if (playerLeavingNick != null) {
                                        writeMessage(players.get(playerId).getName() + " left the game giving game master to " + playerLeavingNick + ".", Color.GRAY, playerId, -1);
                                        playerLeavingNick = null;
                                    } else {
                                        writeMessage(players.get(playerId).getName() + " left the game.", Color.GRAY, playerId, -1);
                                    }
                                }
                            }
                            players.delete(playerId);
                            updatePlayerView();
                        }
                    }
                });
            }

            @Override
            public void onEstateUpdate(final int estateId, final HashMap<String, String> data) {
                Log.v("monopd", "net: Received onEstateUpdate() from MonoProtocolHandler");
                final HashMap<String, String> map = new HashMap<String, String>(data);
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        Estate estate;
                        boolean isNew = false;
                        if (estateId < estates.size()) {
                            estate = estates.get(estateId);
                        } else {
                            isNew = true;
                            estate = new Estate(estateId);
                        }
                        for (String key : map.keySet()) {
                            String value = map.get(key);
                            XmlAttribute attr = BoardActivity.estateAttributes.get(key);
                            if (attr == null) {
                                Log.w("monopd", "estate." + key + " was unknown. Value = " + value);
                            } else {
                                attr.set(estate, value);
                            }
                        }
                        if (isNew) {
                            if (estateId > estates.size()) {
                                estates.add(estates.size(), estate);
                            } else {
                                estates.add(estateId, estate);
                            }
                        } else {
                            estates.set(estateId, estate);
                        }
                        boardView.drawBoardRegions(estates, players);
                        boardView.drawActionRegions(estates, playerIds, players, buttons, playerId);
                        boardView.drawPieces(estates, playerIds, players);
                        boardView.redrawOverlay();
                    }
                });
            }

            @Override
            public void onGameUpdate(final int gameId, final String status) {
                Log.v("monopd", "net: Received onGameUpdate() from MonoProtocolHandler");
                BoardActivity.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        if (gameId > 0) {
                            gameItem.setGameId(gameId);
                        }
                        
                        BoardActivity.this.status = GameStatus.fromString(status);
                        BoardActivity.this.boardView.setStatus(BoardActivity.this.status);
                        redrawRegions();
                        
                        switch (BoardActivity.this.status) {
                        case CONFIG:
                            writeMessage("Entering a game...", Color.YELLOW, -1, -1);
                            clearCookie();
                            break;
                        case INIT:
                            writeMessage("Starting game...", Color.YELLOW, -1, -1);
                            saveCookie();
                            break;
                        case RUN:
                            initPlayerColors();
                            redrawRegions();
                            break;
                        default:
                            break;
                        }
                    }
                });
            }

            @Override
            public void onConfigUpdate(ArrayList<Configurable> configList) {
                Log.v("monopd", "net: Received onConfigUpdate() from MonoProtocolHandler");
                final ArrayList<Configurable> configurables = new ArrayList<Configurable>(configList);
                BoardActivity.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        boolean fullList = false;
                        nextItem: for (Configurable toAdd : configurables) {
                            for (int i = 0; i < BoardActivity.this.configurables.size(); i++) {
                                if (toAdd.getCommand().equals(BoardActivity.this.configurables.get(i).getCommand())) {
                                    BoardActivity.this.configurables.set(i, toAdd);
                                    continue nextItem;
                                }
                            }
                            BoardActivity.this.configurables.add(toAdd);
                            fullList = true;
                        }
                        if (boardView.isRunning()) {
                            if (fullList) {
                                boardView.drawConfigRegions(BoardActivity.this.configurables, isMaster);
                            } else {
                                boardView.redrawConfigRegions(BoardActivity.this.configurables, isMaster);
                            }
                        }
                    }
                });
            }

            @Override
            public void onEstateGroupUpdate(final int estateGroupId, HashMap<String, String> data) {
                Log.v("monopd", "net: Received onEstateGroupUpdate() from MonoProtocolHandler");
                final HashMap<String, String> map = new HashMap<String, String>(data);
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        EstateGroup estateGroup;
                        estateGroup = estateGroups.get(estateGroupId);
                        if (estateGroup == null) {
                            estateGroup = new EstateGroup(estateGroupId);
                        }
                        for (String key : map.keySet()) {
                            String value = map.get(key);
                            XmlAttribute attr = BoardActivity.estateGroupAttributes.get(key);
                            if (attr == null) {
                                Log.w("monopd", "estategroup." + key + " was unknown. Value = " + value);
                            } else {
                                attr.set(estateGroup, value);
                            }
                        }
                        estateGroups.put(estateGroupId, estateGroup);
                        boardView.redrawOverlay();
                    }
                });
            }

            @Override
            public void onAuctionUpdate(final int auctionId, HashMap<String, String> data) {
                Log.v("monopd", "net: Received onEstateGroupUpdate() from MonoProtocolHandler");
                final HashMap<String, String> map = new HashMap<String, String>(data);
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        if (auctionId != 0) {
                            Log.w("monopd", "auction.auctionid was not 0. Value = " + auctionId);
                            return;
                        }
                        boolean isNew = false;
                        int orange = Color.rgb(255, 128, 0);
                        for (String key : map.keySet()) {
                            String value = map.get(key);
                            XmlAttribute attr = BoardActivity.auctionAttributes.get(key);
                            if (attr == null) {
                                Log.w("monopd", "auction." + key + " was unknown. Value = " + value);
                            } else {
                                if (key.equals("estateid")) {
                                    isNew = true;
                                }
                                attr.set(auction, value);
                            }
                        }
                        if (isNew) {
                            writeMessage("AUCTION: " + players.get(auction.getActorId()) + " is auctioning off " + estates.get(auction.getEstateId()).getName() + " (price: $" + estates.get(auction.getEstateId()).getPrice() + ").", orange, -1, auction.getEstateId());
                        } else {
                            switch (auction.getStatus()) {
                            case 0: // new bid
                                writeMessage("AUCTION: " + players.get(auction.getHighBidder()).getName() + " bid $" + auction.getHighBid() + ".", orange, auction.getHighBidder(), -1);
                                break;
                            case 1: // going once
                                writeMessage("AUCTION: Going once!", orange, auction.getHighBidder(), -1);
                                break;
                            case 2: // going twice
                                writeMessage("AUCTION: Going twice!", orange, auction.getHighBidder(), -1);
                                break;
                            case 3: // sold
                                writeMessage("AUCTION: Sold " + estates.get(auction.getEstateId()).getName() + " to " + players.get(auction.getHighBidder()).getName() + " for $" + auction.getHighBid() + ".", orange, auction.getHighBidder(), -1);
                                break;
                            }
                        }
                        boardView.overlayAuctionInfo(auction.getAuctionId());
                        boardView.redrawOverlay();
                    }
                });
            }

            @Override
            public void onChatMessage(final int playerId, final String author, final String text) {
                Log.v("monopd", "net: Received onChatMessage() from MonoProtocolHandler");
                BoardActivity.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        if (author.length() > 0) {
                            writeMessage("<" + author + "> " + text, Color.WHITE, playerId, -1);
                            // handle !ping, !date, !version
                            if (text.startsWith("!")) {
                                boolean doCommand = false;
                                int spacePos = text.indexOf(' ');
                                if (spacePos > 0) {
                                    String requestName = text.substring(spacePos).trim();
                                    if (requestName.length() == 0 || requestName.equals(nickname)) {
                                        doCommand = true;
                                    }
                                } else {
                                    doCommand = true;
                                    spacePos = text.length();
                                }
                                if (doCommand) {
                                    String command = text.substring(1, spacePos);
                                    if (command.equals("ping")) {
                                        sendCommand("pong");
                                    } else if (command.equals("version")) {
                                        sendCommand(clientName + " " + clientVersion);
                                    } else if (command.equals("date")) {
                                        sendCommand(DateFormat.getDateTimeInstance().format(new Date()));
                                    }
                                }
                            }
                        }
                    }
                });
            }

            @Override
            public void onErrorMessage(final String text) {
                Log.v("monopd", "net: Received onErrorMessage() from MonoProtocolHandler");
                BoardActivity.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        writeMessage("ERROR: " + text, Color.RED, -1, -1);
                    }
                });
            }

            @Override
            public void onInfoMessage(final String text) {
                Log.v("monopd", "net: Received onInfoMessage() from MonoProtocolHandler");
                BoardActivity.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        writeMessage("INFO: " + text, Color.LTGRAY, -1, -1);
                    }
                });
            }

            @Override
            public void onDisplayMessage(final int estateId, final String text, boolean clearText,
                    final boolean clearButtons, ArrayList<Button> newButtons) {
                Log.v("monopd", "net: Received onDisplayMessage() from MonoProtocolHandler");
                boolean redrawButtons = false;
                if (clearButtons) {
                    buttons.clear();
                    redrawButtons = true;
                }
                if (newButtons.size() > 0) {
                    buttons.addAll(newButtons);
                    redrawButtons = true;
                }
                if (redrawButtons) {
                    boardView.drawActionRegions(estates, playerIds, players, buttons, playerId);
                }
                if (text == null || text.length() == 0) {
                    return;
                }
                BoardActivity.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        writeMessage("GAME: " + text, Color.CYAN, -1, ((estateId == -1) ? 0 : estateId));
                    }
                });
            }

            @Override
            public void onPlayerListUpdate(String type, ArrayList<Player> list) {
                Log.v("monopd", "net: Received onPlayerListUpdate() from MonoProtocolHandler");
                /*if (type.equals("full")) {
                    //Log.d("monopd", "players: Full list update");
                    final int[] newPlayerIds = new int[4];
                    for (int i = 0; i < list.size() && i < 4; i++) {
                        newPlayerIds[i] = list.get(i).getPlayerId();
                    }
                    BoardActivity.this.runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            setPlayerView(newPlayerIds);
                        }
                    });
                } else if (type.equals("edit")) {
                    // Log.d("monopd", "players: Edit " +
                    // list.get(0).getNick());
                    BoardActivity.this.runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            BoardActivity.this.updatePlayerView();
                        }
                    });
                } else if (type.equals("add")) {
                    // Log.d("monopd", "players: Add " +
                    // list.get(0).getNick());
                    final int[] newPlayerIds = BoardActivity.this.playerIds;
                    for (int i = 0; i < list.size(); i++) {
                        for (int j = 0; j < 4; j++) {
                            final int playerId = list.get(i).getPlayerId();
                            if (newPlayerIds[j] == 0 || newPlayerIds[j] == playerId) {
                                BoardActivity.this.runOnUiThread(new Runnable() {

                                    @Override
                                    public void run() {
                                        writeMessage(players.get(playerId).getNick() + " joined the game.", Color.YELLOW, playerId, -1, false);
                                    }
                                });
                                newPlayerIds[j] = playerId;
                                break;
                            }
                        }
                    }
                    BoardActivity.this.runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            BoardActivity.this.setPlayerView(newPlayerIds);
                        }
                    });
                } else if (type.equals("del")) {
                    //Log.d("monopd", "players: Delete "
                    //        + BoardActivity.this.players.get(list.get(0).getPlayerId()).getNick());
                    final int[] newPlayerIds = BoardActivity.this.playerIds;
                    for (int i = 0; i < list.size(); i++) {
                        boolean moveBack = false;
                        for (int j = 0; j < 4; j++) {
                            if (!moveBack && newPlayerIds[j] == list.get(i).getPlayerId()) {
                                final int playerIdClosure = newPlayerIds[j];
                                BoardActivity.this.runOnUiThread(new Runnable() {

                                    @Override
                                    public void run() {
                                        writeMessage(players.get(playerIdClosure).getNick() + " left the game.", Color.YELLOW, playerIdClosure, -1, false);
                                    }
                                });
                                moveBack = true;
                            }
                            if (moveBack) {
                                newPlayerIds[j] = j < 3 ? newPlayerIds[j + 1] : 0;
                            }
                        }
                    }
                    BoardActivity.this.runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            BoardActivity.this.setPlayerView(newPlayerIds);
                        }
                    });
                } else {
                    Log.w("monopd", "unrecognized playerlistupdate type: " + type + " " + list.get(0).getNick());
                }*/
            }

            @Override
            public void setHandler(Handler netHandler) {
                BoardActivity.this.netHandler = netHandler;
            }

            @Override
            public void onGameItemUpdate(GameItem item) {
                if (gameItem.getGameId() == item.getGameId()) {
                    if (item.getPlayers() > 0) {
                        gameItem.setPlayers(item.getPlayers());
                    }
                    if (item.getType() != null) {
                        gameItem.setType(item.getType());
                    }
                    if (item.getTypeName() != null) {
                        gameItem.setTypeName(item.getTypeName());
                    }
                    if (item.getDescription() != null) {
                        gameItem.setDescription(item.getDescription());
                        final String descr = item.getDescription();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                setTitle(descr);                                
                            }
                        });
                    }
                    gameItem.setCanJoin(item.canJoin());
                }
            }
        });
        
        this.setTitle(String.format(this.getString(R.string.title_activity_board), gameItem.getDescription()));
    }
    
    private String getHtmlColor(int color) {
        String r = Integer.toHexString(Color.red(color));
        if (r.length() == 1) {
            r = '0' + r;
        }
        String g = Integer.toHexString(Color.green(color));
        if (g.length() == 1) {
            g = '0' + g;
        }
        String b = Integer.toHexString(Color.blue(color));
        if (b.length() == 1) {
            b = '0' + b;
        }
        return r + g + b;
    }

    public void onPlayer1Click(View v) {
        this.boardView.overlayPlayerInfo(playerIds[0]);
    }
    
    public void onPlayer2Click(View v) {
        this.boardView.overlayPlayerInfo(playerIds[1]);
    }
    
    public void onPlayer3Click(View v) {
        this.boardView.overlayPlayerInfo(playerIds[2]);
    }
    
    public void onPlayer4Click(View v) {
        this.boardView.overlayPlayerInfo(playerIds[3]);
    }

    @Override
    protected void onStart() {
        super.onStart();
        running = true;
    }
    
    @Override
    protected void onStop() {
        super.onStop();
        running = false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.boardView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.boardView.onResume();
        //redrawRegions();
    }
    
    @Override
    public void onBackPressed() {
        if (boardView.isOverlayOpen()) {
            boardView.closeOverlay();
        } else if (status == GameStatus.RUN) {
            // ask to finish
            finish();
        } else {
            finish();
        }
    }

    private void initPlayerColors() {
        int index = 0;
        for (int playerId : playerIds) {
            if (playerId > 0) {
                BoardViewPiece.pieces[index].setPlayerId(playerId);
            }
            index++;
        }
    }
    
    private void redrawRegions() {
        switch (this.boardView.getStatus()) {
        case ERROR:
            boardView.calculateTextRegions();
            boardView.createTextRegion("An error occured.", true);
            break;
        case CREATE:
            boardView.calculateTextRegions();
            boardView.createTextRegion("Creating game...", false);
            break;
        case JOIN:
            boardView.calculateTextRegions();
            boardView.createTextRegion("Joining game...", false);
            break;
        case RECONNECT:
            boardView.calculateTextRegions();
            boardView.createTextRegion("Reconnecting to game...", false);
            break;
        case INIT:
            boardView.calculateTextRegions();
            boardView.createTextRegion("Starting game...", false);
            break;
        case CONFIG:
            boardView.calculateConfigRegions();
            boardView.drawConfigRegions(configurables, isMaster);
            break;
        case RUN:
            boardView.calculateBoardRegions();
            boardView.drawBoardRegions(estates, players);
            boardView.drawActionRegions(estates, playerIds, players, buttons, playerId);
            boardView.drawPieces(estates, playerIds, players);
            break;
        }
        boardView.redrawOverlay();
    }

    private void animateMove(Player player) {
        int start = player.getLastLocation();
        int end = player.getLocation();
        boolean directMove = player.getDirectMove();
        int playerIndex = 0;
        for (int i = 0; i < 4; i++) {
            if (BoardViewPiece.pieces[i].getPlayerId() == player.getPlayerId()) {
                playerIndex = i;
                break;
            }
        }
        BoardViewPiece.pieces[playerIndex].setCurrentEstate(end);
        BoardViewPiece.pieces[playerIndex].setProgressEstate(start);
        BoardViewPiece.pieces[playerIndex].setProgressEstateDelta(0);
        boardView.drawPieces(estates, playerIds, players);
        if (directMove) {
            Bundle args = new Bundle();
            args.putInt("estateId", end);
            sendToNetThread(BoardNetworkAction.MSG_TURN, args);
            BoardViewPiece.pieces[playerIndex].setProgressEstate(end);
            boardView.drawPieces(estates, playerIds, players);
            boardView.waitDraw();
        } else {
            if (start > end) {
                end += 40;
            }
            for (int i = start + 1; i <= end; i++) {
                Bundle args = new Bundle();
                args.putInt("estateId", (i % 40));
                sendToNetThread(BoardNetworkAction.MSG_TURN, args);

                BoardViewPiece.pieces[playerIndex].setProgressEstate((i - 1) % 40);
                BoardViewPiece.pieces[playerIndex].setCurrentEstate(i % 40);
                for (int j = 0; j < BoardViewSurfaceThread.animationSteps; j++) {
                    BoardViewPiece.pieces[playerIndex].setProgressEstateDelta(j);
                    boardView.drawPieces(estates, playerIds, players);
                    boardView.waitDraw();
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (netHandler != null) {
            if (savingState) {
                this.sendToNetThread(BoardNetworkAction.MSG_PAUSE, null);
            } else {
                this.sendToNetThread(BoardNetworkAction.MSG_STOP, null);
            }
        }
    }
    
    private void storeViews() {
        this.boardView = (BoardView) this.findViewById(R.id.board_ui);
        this.chatSendBox = (EditText) this.findViewById(R.id.board_chat_box);
        this.chatListAdapter = new ChatListAdapter(this, R.layout.chat_item);
        this.chatList = (ListView) this.findViewById(R.id.board_chat_list);
        this.chatList.setAdapter(this.chatListAdapter);
        this.playerView[0] = (LinearLayout) this.findViewById(R.id.board_player_item_1);
        this.playerView[1] = (LinearLayout) this.findViewById(R.id.board_player_item_2);
        this.playerView[2] = (LinearLayout) this.findViewById(R.id.board_player_item_3);
        this.playerView[3] = (LinearLayout) this.findViewById(R.id.board_player_item_4);
    }

    private void sendCommand(String text) {
        Bundle state = new Bundle();
        state.putString("text", text);
        this.sendToNetThread(BoardNetworkAction.MSG_COMMAND, state);
    }

    /**
     * Sends a message to the network thread. Use this and not
     * nethandler.dispatchMessage()
     * 
     * @param action
     *            The message ID.
     * @param arguments
     *            Named arguments of the message. Can be null to specify zero arguments.
     */
    public void sendToNetThread(BoardNetworkAction action, Bundle arguments) {
        Message msg = Message.obtain(netHandler, action.getWhat());
        msg.setData(arguments);
        netHandler.dispatchMessage(msg);
    }

    /**
     * Write a message to the chat list. The chat item will be tappable if
     * either playerId or estateId is positive.
     * 
     * @param msgText
     *            The text.
     * @param color
     *            The color.
     * @param playerId
     *            A player ID associated with this message. Set to 0 or negative to ignore.
     * @param estateId
     *            An estate ID associated with this message. Set to 0 or negative to ignore.
     * @param clearButtons
     *            Whether the buttons should be cleared, if any. TODO: move to anotehr function.
     */
    private void writeMessage(String msgText, int color, int playerId, int estateId) {
        BoardActivity.this.chatListAdapter.add(new ChatItem(msgText, color, playerId, estateId));
        BoardActivity.this.chatListAdapter.notifyDataSetChanged();
    }

    /**
     * Set the player list to show the specified 4 players. Player ID 0 means
     * that slot is empty.
     * 
     * @param playerIds
     *            Player IDs of the players to show.
     */
    /*private void setPlayerView(int[] playerIds) {
        this.playerIds = playerIds;
        this.updatePlayerView();
    }*/

    /**
     * Updates the player view with new data from the player list.
     */
    private void updatePlayerView() {
        for (int i = 0; i < 4; i++) {
            if (this.playerIds[i] == 0) {
                this.playerView[i].setVisibility(View.GONE);
            } else {
                Player player = this.players.get(this.playerIds[i]);
                if (player == null) {
                    this.playerView[i].setVisibility(View.GONE);
                    Log.w("monopd", "players: Unknown player ID " + this.playerIds[i]);
                } else {
                    this.playerView[i].setVisibility(View.VISIBLE);
                    TextView text1 = (TextView) this.playerView[i].findViewById(R.id.player_text_1);
                    TextView text2 = (TextView) this.playerView[i].findViewById(R.id.player_text_2);
                    SpannableString nameText = new SpannableString(player.getName());
                    if (player.getPlayerId() == playerId) {
                        nameText.setSpan(new StyleSpan(Typeface.BOLD), 0, player.getName().length(), SpannedString.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                    text1.setText(nameText);
                    switch (status) {
                    case CONFIG:
                        if (player.isMaster()) {
                            text1.setTextColor(Color.YELLOW);
                        } else {
                            text1.setTextColor(Color.WHITE);
                        }
                        
                        text2.setText(player.getHost());
                        break;
                    case RUN:
                        if (player.isTurn()) {
                            text1.setTextColor(Color.YELLOW);
                        } else {
                            text1.setTextColor(Color.WHITE);
                        }
                        
                        Drawable draw = BoardViewPiece.pieces[i].getDrawable(For.PLAYER_LIST);
                        
                        text2.setText("$" + Integer.toString(player.getMoney()));
                        text2.setCompoundDrawablePadding(5);
                        text2.setCompoundDrawablesWithIntrinsicBounds(draw, null, null, null);
                        break;
                    default:
                        text1.setTextColor(Color.WHITE);
                        text2.setText("");
                        break;
                    }
                }
            }
        }
    }

    public GameStatus getGameStatus() {
        return status;
    }

    public GameItem getGameItem() {
        return gameItem;
    }

    public String getClientName() {
        return clientName;
    }

    public String getClientVersion() {
        return clientVersion;
    }

    public String getNickname() {
        return nickname;
    }

    public String getSavedCookie() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        return prefs.getString("saved_cookie", null);
    }
    
    private void clearCookie() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Editor editor = prefs.edit();

        editor.remove("saved_game_id");
        editor.remove("saved_cookie");
        editor.remove("saved_server");
        editor.remove("saved_port");
        editor.remove("saved_version");
        editor.remove("saved_type");
        editor.remove("saved_type_name");
        editor.remove("saved_descr");
        editor.remove("saved_players");
        
        editor.commit();
    }

    private void saveCookie() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Editor editor = prefs.edit();
        
        int gameId = gameItem.getGameId();
        String server = gameItem.getServer().getHost();
        int port = gameItem.getServer().getPort();
        String version = gameItem.getServer().getVersion();
        String type = gameItem.getType();
        String type_name = gameItem.getTypeName();
        String descr = gameItem.getDescription();
        int players = gameItem.getPlayers();
        
        editor.putInt("saved_game_id", gameId);
        editor.putString("saved_cookie", cookie);
        editor.putString("saved_server", server);
        editor.putInt("saved_port", port);
        editor.putString("saved_version", version);
        editor.putString("saved_type", type);
        editor.putString("saved_type_name", type_name);
        editor.putString("saved_descr", descr);
        editor.putInt("saved_players", players);
        
        editor.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.getMenuInflater().inflate(R.menu.board_activity, menu);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch (item.getItemId()) {
        case R.id.menu_bankrupt:
            if (status == GameStatus.RUN) {
                sendToNetThread(BoardNetworkAction.MSG_DECLARE_BANKRUPCY, null);
            }
            break;
        case R.id.menu_name:
            DialogFragment dialog = new DialogFragment() {
                @Override
                public Dialog onCreateDialog(Bundle savedInstanceState) {
                    final EditText editText = new EditText(BoardActivity.this);
                    editText.setSingleLine(true);
                    editText.setInputType(EditorInfo.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
                    return new AlertDialog.Builder(BoardActivity.this)
                    .setView(editText)
                    .setIcon(android.R.drawable.ic_dialog_info)
                    .setTitle(R.string.pref_player_nick_dialog_title)
                    .setMessage(R.string.pref_player_nick_summary)
                    .setNegativeButton(android.R.string.cancel, new OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Bundle args = new Bundle();
                            args.putString("nick", editText.getText().toString());
                            sendToNetThread(BoardNetworkAction.MSG_NICK, args);
                            dismiss();
                        }
                    })
                    .setPositiveButton(android.R.string.ok, 
                    new OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Bundle args = new Bundle();
                            args.putString("nick", editText.getText().toString());
                            sendToNetThread(BoardNetworkAction.MSG_NICK, args);
                            dismiss();
                        }
                    }).create();
                }
            };
            dialog.show(getSupportFragmentManager(), "nick_change");
            break;
        case R.id.menu_settings:
            Intent settings = new Intent(this, SettingsActivity.class);
            this.startActivity(settings);
            break;
        }
        return true;
    }
}