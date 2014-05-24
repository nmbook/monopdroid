package com.natembook.monopdroid.board;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.text.DateFormat;

import org.xml.sax.XMLReader;

import com.natembook.monopdroid.R;
import com.natembook.monopdroid.SettingsActivity;
import com.natembook.monopdroid.board.surface.BoardView;
import com.natembook.monopdroid.board.surface.BoardViewListener;
import com.natembook.monopdroid.board.surface.BoardViewOverlay;
import com.natembook.monopdroid.board.surface.BoardViewPiece;
import com.natembook.monopdroid.board.surface.BoardViewSurfaceThread;
import com.natembook.monopdroid.board.surface.GestureRegion;
import com.natembook.monopdroid.board.surface.GestureRegionListener;
import com.natembook.monopdroid.board.surface.BoardViewPiece.For;
import com.natembook.monopdroid.gamelist.GameItem;
import com.natembook.monopdroid.gamelist.GameItemType;
import com.natembook.monopdroid.gamelist.ServerItem;
import com.natembook.monopdroid.monopd.MonoProtocolGameListener;

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
import android.text.Editable;
import android.text.SpannableString;
import android.text.SpannedString;
import android.text.Html.TagHandler;
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

/**
 * The primary game Activity, for a game lobby and game.
 * @author Nate
 *
 */
public class BoardActivity extends FragmentActivity {
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
    private LinearLayout[] playerView = new LinearLayout[BoardViewPiece.MAX_PLAYERS];
    
    /**
     * The networking handler. Used to send messages to the networking thread.
     */
    private Handler netHandler = null;
    /**
     * The networking thread runner (created in UI thread). Do not access from
     * networking thread. Save on restart.
     */
    private BoardActivityNetworkThread netRunnable = null;
    
    /**
     * This game item. Save on restart.
     */
    private GameItem gameItem = null;
    /**
     * Array of player's IDs to show up to MAX_PLAYERS. Save on restart.
     */
    private int[] playerIds = new int[BoardViewPiece.MAX_PLAYERS];
    /**
     * List of players. Save on restart.
     */
    private SparseArray<Player> players = new SparseArray<Player>();
    /**
     * List of estates. Save on restart.
     */
    private ArrayList<Estate> estates = new ArrayList<Estate>(40);
    /**
     * List of estate groups. Save on restart.
     */
    private SparseArray<EstateGroup> estateGroups = new SparseArray<EstateGroup>();
    /**
     * Auction. Save on restart.
     * Even though they have an auctionId, it is always 0. 
     */
    private Auction auction = new Auction(0);
    /**
     * List of trades. Save on restart.
     */
    private SparseArray<Trade> trades = new SparseArray<Trade>();
    /**
     * List of options. Save on restart.
     */
    private SparseArray<Configurable> configurables = new SparseArray<Configurable>();
    /**
     * List of buttons. Save on restart.
     */
    private ArrayList<Button> buttons = new ArrayList<Button>();
    /**
     * Current player ID. Save on restart.
     */
    private int playerId = 0;
    /**
     * Current player cookie. Save on restart.
     */
    private String cookie = null;
    /**
     * Game status. Save on restart.
     */
    private GameStatus status = GameStatus.ERROR;
    /**
     * Client name. Save on restart.
     */
    private String clientName;
    /**
     * Client version. Save on restart.
     */
    private String clientVersion;
    /**
     * Current nick name. Save on restart.
     */
    private String nickname;
    /**
     * Whether we are the master of this game lobby. Save on restart.
     */
    private boolean isMaster = false;
    /**
     * The current master of this game lobby.
     */
    private int master = 0;
    /**
     * Whether this onDestroy() occured after saving state.
     */
    private boolean savingState = false;
    /**
     * Whether this onResume() occured with intent info (true) or saved state data (false).
     */
    private boolean firstInit = false;
    
    /**
     * Used to make sure a dialog may open at this time.
     */
    private boolean running = false;
    
    /**
     * Stores the most recent leaving player's nickname, so the leave message is accurate.
     */
    private String playerLeavingNick = null;
    
    /**
     * Static handler for HTML tag parsing. TODO: use BoardTextFormatter
     */
    public static TagHandler tagHandler = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // STATIC INIT
        if (tagHandler == null) {
            tagHandler = new TagHandler() {
                
                @Override
                public void handleTag(boolean opening, String tag, Editable output, XMLReader xmlReader) {
                    
                }
            };
        }
        
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
            state.restoreState(this);
        }
        
        attachListeners();

        Log.d("monopd", "board: Completed activity set-up");
    }
    
    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        BoardActivityState state = new BoardActivityState();
        state.saveState(this);
        savingState = true;
        return state;
    }
    
    /**
     * Stores the Activity state so it can be re-instated after a restart,
     * such as from a device rotation.
     * @author Nate
     */
    private final class BoardActivityState {
        private BoardViewSurfaceThread surfaceRunner;
        
        private ArrayList<ChatItem> chats;
        private int[] playerIds = new int[4];
        private BoardActivityNetworkThread netRunnable;
        private GameItem gameItem;
        private SparseArray<Player> players;
        private ArrayList<Estate> estates;
        private Auction auction;
        private SparseArray<Trade> trades;
        private SparseArray<Configurable> configurables;
        private int playerId;
        private String cookie;
        private GameStatus status;
        private String clientName;
        private String clientVersion;
        private String nickname;
        private boolean isMaster;
        private int master;
        
        public void saveState(BoardActivity activity) {
            this.surfaceRunner = activity.boardView.saveState();
            this.chats = activity.chatListAdapter.saveState();
            this.playerIds = activity.playerIds;
            this.netRunnable = activity.netRunnable;
            this.gameItem = activity.gameItem;
            this.players = activity.players;
            this.estates = activity.estates;
            this.auction = activity.auction;
            this.trades = activity.trades;
            this.configurables = activity.configurables;
            this.playerId = activity.playerId;
            this.cookie = activity.cookie;
            this.status = activity.status;
            this.clientName = activity.clientName;
            this.clientVersion = activity.clientVersion;
            this.nickname = activity.nickname;
            this.isMaster = activity.isMaster;
            this.master = activity.master;
        }
        
        public void restoreState(BoardActivity activity) {
            activity.boardView.restoreState(this.surfaceRunner);
            activity.chatListAdapter.restoreState(this.chats);
            activity.playerIds = this.playerIds;
            activity.netRunnable = this.netRunnable;
            activity.gameItem = this.gameItem;
            activity.players = this.players;
            activity.estates = this.estates;
            activity.auction = this.auction;
            activity.trades = this.trades;
            activity.configurables = this.configurables;
            activity.playerId = this.playerId;
            activity.cookie = this.cookie;
            activity.status = this.status;
            activity.clientName = this.clientName;
            activity.clientVersion = this.clientVersion;
            activity.nickname = this.nickname;
            activity.isMaster = this.isMaster;
            activity.master = this.master;
        }
    }

    private void showConnectionError(String error) {
        if (running) {
            Bundle state = new Bundle();
            state.putString("error", error);
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
                        writeMessage("Joining game: " + getFullTitle(), Color.YELLOW);
                        break;
                    case CREATE:
                        status = GameStatus.CREATE;
                        boardView.setStatus(GameStatus.CREATE);
                        writeMessage("Creating game: " + getFullTitle(), Color.YELLOW);
                        break;
                    case RECONNECT:
                        status = GameStatus.RECONNECT;
                        boardView.setStatus(GameStatus.RECONNECT);
                        writeMessage("Reconnecting to game: " + getFullTitle(), Color.YELLOW);
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
                boardView.overlayEstateInfo(estateId);
            }

            @Override
            public void onCloseOverlay() {
                boardView.closeOverlay();
            }

            @Override
            public void onButtonCommand(String command) {
                Bundle args = new Bundle();
                args.putString("command", command);
                sendToNetThread(BoardNetworkAction.MSG_BUTTON_COMMAND, args);
            }

            @Override
            public String getPlayerOverlayText(int playerId) {
                Player player = players.get(playerId);
                if (player == null) {
                    return "<i>This player has left and this information is no longer available.</i>";
                }
                StringBuilder sb = new StringBuilder();
                sb.append(makePlayerName(player));
                sb.append("<br>");
                //sb.append("Player ID: " + player.getPlayerId() + "\n");
                if (player.isMaster() && status == GameStatus.CONFIG) {
                    sb.append("<i>This player created this game.</i><br>");
                }
                if (player.isTurn()) {
                    sb.append("<i><font color=\"yellow\">It is this player's turn.</font></i><br>");
                }
                if (player.getHost() != null) {
                    sb.append(makeFieldLine("IP address", 
                            player.getHost()));
                }
                if (status == GameStatus.RUN) {
                    Estate estate = estates.get(player.getLocation());
                    sb.append(makeFieldLine("On estate", 
                            makeEstateName(estate)));
                    sb.append(makeFieldLine("Money", 
                            "$" + player.getMoney()));
                    
                    // assets is: money + sale price of houses + unmortgaged value of estates
                    int assets = player.getMoney();
                    int owned = 0;
                    int mortgaged = 0;
                    int houses = 0;
                    int hotels = 0;
                    int completeGroups = 0;
                    
                    SparseIntArray estateGroupMap = new SparseIntArray();
                    for (Estate est : estates) {
                        int groupId = est.getEstateGroup();
                        estateGroupMap.put(groupId, estateGroupMap.get(groupId) + 1);
                    }
                    for (Estate est : estates) {
                        if (est.getOwner() == player.getPlayerId()) {
                            int groupId = est.getEstateGroup();
                            estateGroupMap.put(groupId, estateGroupMap.get(groupId) - 1);
                            if (estateGroupMap.get(groupId) == 0) {
                                completeGroups++;
                            }
                            owned++;
                            if (est.isMortgaged()) {
                                mortgaged++;
                            } else {
                                assets += est.getMortgagePrice() + (est.getHouses() * est.getSellHousePrice());
                                if (est.getHouses() < 5) {
                                    houses += est.getHouses();
                                } else {
                                    hotels++;
                                }
                            }
                        }
                    }
                    sb.append(makeFieldLine("Assets total", 
                            "$" + assets));
                    sb.append("<br>");
                    sb.append(makeFieldLine("Owned estates", 
                            Integer.toString(owned)));
                    sb.append(makeFieldLine("Owned estates mortgaged", 
                            Integer.toString(mortgaged)));
                    sb.append(makeFieldLine("Owned houses", 
                            Integer.toString(houses)));
                    sb.append(makeFieldLine("Owned hotels", 
                            Integer.toString(hotels)));
                    sb.append(makeFieldLine("Owned complete estate sets", 
                            Integer.toString(completeGroups)));
                }
                return sb.toString();
            }

            @Override
            public String getEstateOverlayText(int estateId) {
                Estate estate = estates.get(estateId);
                if (estate == null) {
                    return null;
                }
                StringBuilder sb = new StringBuilder();
                sb.append(makeEstateName(estate));
                sb.append("<br>");
                //sb.append("Estate ID: " + estate.getEstateId() + "\n");
                //EstateGroup group = estateGroups.get(estate.getEstateGroup());
                if (estate.canBeOwned()) {
                    EstateGroup group = estateGroups.get(estate.getEstateGroup());
                    if (group != null) {
                        sb.append(makeFieldLine("Group", group.getName()));
                    }
                    if (estate.getOwner() <= 0) {
                        sb.append(makeFieldLine("Current owner", "<i>none</i>"));
                    } else {
                        Player player = players.get(estate.getOwner());
                        sb.append(makeFieldLine("Current owner", 
                                makePlayerName(player)));
                        if (estate.getColor() != 0) {
                            sb.append(makeFieldLine("Current houses",
                                    makeHouseCount(estate.getHouses())));
                        }
                        
                        if (estateGroups.get(estate.getEstateGroup()).getEstateGroupId() == 8) { // RAILROAD
                            int railCost = 25;
                            int railMult = 1;
                            for (Estate otherEstate : estates) {
                                if (otherEstate.getEstateGroup() == estate.getEstateGroup() &&
                                        otherEstate.getOwner() == estate.getOwner() &&
                                        otherEstate.getEstateId() != estate.getEstateId()) {
                                    railCost *= 2;
                                    railMult++;
                                }
                            }
                            if (estate.isMortgaged()) {
                                sb.append(makeFieldLine("Current rent",
                                        "<i>mortgaged</i>"));
                            } else {
                                sb.append(makeFieldLine("Current rent",
                                        "$" + railCost));
                            }
                            sb.append(makeFieldLine("Railroads owned by " +
                                    BoardActivity.makePlayerName(player),
                                    Integer.toString(railMult)));
                        } else if (estateGroups.get(estate.getEstateGroup()).getEstateGroupId() == 9) { // UTILITIES
                            int utilCost = 4;
                            int utilMult = 1;
                            for (Estate otherEstate : estates) {
                                if (otherEstate.getEstateGroup() == estate.getEstateGroup() &&
                                        otherEstate.getOwner() == estate.getOwner() &&
                                        otherEstate.getEstateId() != estate.getEstateId()) {
                                    utilCost += 6;
                                    utilMult++;
                                }
                            }
                            if (estate.isMortgaged()) {
                                sb.append(makeFieldLine("Current rent",
                                        "<i>mortgaged</i>"));
                            } else {
                                sb.append(makeFieldLine("Current rent",
                                        "$" + utilCost + " � DICE ROLL TOTAL"));
                            }
                            sb.append(makeFieldLine("Utilities owned by " +
                                    BoardActivity.makePlayerName(player),
                                    Integer.toString(utilMult)));
                        } else {
                            if (estate.isMortgaged()) {
                                sb.append(makeFieldLine("Current rent",
                                        "<i>mortgaged</i>"));
                            } else {
                                sb.append(makeFieldLine("Current rent",
                                        "$" + estate.getRent(estate.getHouses())));
                            }
                        }
                    }
                    
                    sb.append("<br>");
                    sb.append(makeFieldLine("Price to buy",
                            "$" + estate.getPrice()));
                    if (estate.getMortgagePrice() > 0) {
                        sb.append(makeFieldLine("Price to mortgage",
                                "$" + estate.getMortgagePrice()));
                        sb.append(makeFieldLine("Price to unmortgage",
                                "$" + estate.getUnmortgagePrice()));
                    }
                    if (estate.getColor() != 0) {
                        sb.append(makeFieldLine("Price to rent by house number",
                                "$" + estate.getRent(0) + " (" + makeHouseCount(0) + "), " +
                                "$" + estate.getRent(1) + " (" + makeHouseCount(1) + "), " +
                                "$" + estate.getRent(2) + " (" + makeHouseCount(2) + "), " +
                                "$" + estate.getRent(3) + " (" + makeHouseCount(3) + "), " +
                                "$" + estate.getRent(4) + " (" + makeHouseCount(4) + "), " +
                                "$" + estate.getRent(5) + " (" + makeHouseCount(5) + ")"));
                        sb.append(makeFieldLine("Price to buy house",
                                "$" + estate.getHousePrice()));
                        sb.append(makeFieldLine("Price to sell house",
                                "$" + estate.getSellHousePrice()));
                    }
                }
                if (estate.getPassMoney() > 0) {
                    sb.append(makeFieldLine("Money on pass",
                            "$" + estate.getPassMoney()));
                }
                if (estate.getTax() > 0) {
                    sb.append(makeFieldLine("Tax amount to pay",
                            "$" + estate.getTax()));
                }
                if (estate.getTaxPercentage() > 0) {
                    if (estate.getTax() > 0) {
                        sb.append("-- OR --<br>");
                    }
                    sb.append(makeFieldLine("Tax percent to pay",
                            "$" + estate.getTaxPercentage()));
                }
                if (estate.isJail()) {
                    sb.append("<i>Jail</i><br>");
                }
                if (estate.isToJail()) {
                    sb.append("<i>Go to jail</i><br>");
                }
                
                String onThis = "";
                for (int i = 0; i < BoardViewPiece.MAX_PLAYERS; i++) {
                    if (BoardActivity.this.playerIds[i] != 0) {
                        Player player = BoardActivity.this.players.get(BoardActivity.this.playerIds[i]);
                        if (player.getLocation() == estateId) {
                            if (onThis != "") {
                                onThis += ", ";
                            }
                            onThis += makePlayerName(player);
                        }
                    }
                }
                if (onThis != "") {
                    sb.append("<br>");
                    sb.append(makeFieldLine("Players here", onThis));
                }
                return sb.toString();
            }

            @Override
            public String getAuctionOverlayText(int auctionId) {
                if (auction == null) {
                    return null;
                }
                StringBuilder sb = new StringBuilder();
                Player auPlayer = players.get(auction.getActorId());
                Estate estate = estates.get(auction.getEstateId());
                sb.append(makePlayerName(auPlayer))
                .append(" is auctioning off ")
                .append(makeEstateName(estate))
                .append("<br>");
                if (auction.getHighBidder() == 0) {
                    sb.append(makeFieldLine("Current highest bid",
                            "<i>none yet</i>"));
                } else {
                    Player bidPlayer = players.get(auction.getHighBidder());
                    sb.append(makeFieldLine("Current highest bid",
                            "$" + auction.getHighBid() + " by " + makePlayerName(bidPlayer)));
                }
                sb.append(makeFieldLine("Number of bids",
                        Integer.toString(auction.getNumberOfBids())));
                switch (auction.getStatus()) {
                case 0: // ACTIVE
                    sb.append("<font color=\"green\"><b>ACTIVE</b></font><br>");
                    break;
                case 1: // ONCE
                    sb.append("<font color=\"yellow\"><b>GOING ONCE</b></font><br>");
                    break;
                case 2: // TWICE
                    sb.append("<font color=\"#ff8800\"><b>GOING TWICE</b></font><br>");
                    break;
                case 3: // SOLD
                    sb.append("<font color=\"red\"><b>SOLD</b></font><br>");
                    break;
                }
                sb.append("<br>");
                String estateOverlay = getEstateOverlayText(auction.getEstateId());
                if (estateOverlay != null) {
                    sb.append(estateOverlay);
                }
                return sb.toString();
            }

            @Override
            public String getTradeOverlayText(int tradeId) {
                Trade trade = trades.get(tradeId);
                if (trade == null) {
                    return null;
                }
                Collection<TradePlayer> tradePlayers = trade.getPlayers();
                Collection<TradeOffer> offers =  trade.getOffers();
                StringBuilder sb = new StringBuilder();
                switch (trade.getLastUpdateType()) {
                case NEW:
                case UPDATED:
                case ACCEPTED:
                    sb.append("<b><font color=\"green\">Active</font></b> trade with ");
                    break;
                case REJECTED:
                    sb.append("<b><font color=\"red\">Cancelled</font></b> trade with ");
                    break;
                case COMPLETED:
                    sb.append("<b><font color=\"gray\">Completed</font></b> trade with ");
                    break;
                }
                sb.append(tradePlayers.size())
                .append(" participants and ")
                .append(offers.size())
                .append(" proposed trades<br>");
                sb.append("<br>");
                sb.append(makeFieldLine("Trade revision",
                        Integer.toString(trade.getRevision())));
                sb.append("<br>");
                sb.append("<b>Current participants</b>:<br>");
                for (TradePlayer player : tradePlayers) {
                    sb.append(makePlayerName(players.get(player.getPlayerId())))
                    .append(" has ");
                    if (player.hasAccepted()) {
                        sb.append("<font color=\"green\">accepted</font>");
                    } else {
                        sb.append("<font color=\"#ff8800\">not yet accepted</font>");
                    }
                    sb.append(" this trade.<br>");
                }
                sb.append("<br>");
                sb.append("<b>Current trade proposals</b>:<br>");
                for (TradeOffer offer : offers) {
                    sb.append(makePlayerName(players.get(offer.getPlayerIdFrom())))
                    .append(" gives ");
                    switch (offer.getType()) {
                    case CARD:
                        sb.append(" card #")
                        .append(offer.getOfferValue());
                        break;
                    case ESTATE:
                        sb.append(" ")
                        .append(makeEstateName(estates.get(offer.getOfferValue())));
                        break;
                    case MONEY:
                        sb.append(" $")
                        .append(Integer.toString(offer.getOfferValue()));
                        break;
                    default:
                        sb.append(" something of value with id: ")
                        .append(Integer.toString(offer.getOfferValue()));
                        break;
                    }
                    sb.append(" to ")
                    .append(makePlayerName(players.get(offer.getPlayerIdTo())))
                    .append("<br>");
                }
                return sb.toString();
            }
            
            @Override
            public ArrayList<Estate> getEstates() {
                return estates;
            }

            @Override
            public int getSelfPlayerId() {
                return playerId;
            }

            @Override
            public ArrayList<OverlayButton> getPlayerOverlayButtons(final int playerId) {
                ArrayList<OverlayButton> buttons = new ArrayList<OverlayButton>(4);
                if (status == GameStatus.CONFIG) {
                    boolean kickable = isMaster &&
                            playerId != BoardActivity.this.playerId &&
                            players.get(playerId) != null;
                    buttons.add(new OverlayButton(
                            "Kick", kickable, 3,
                            new GestureRegionListener() {
                                @Override
                                public void onGestureRegionClick(GestureRegion gestureRegion) {
                                    Bundle args = new Bundle();
                                    args.putInt("playerId", playerId);
                                    sendToNetThread(BoardNetworkAction.MSG_KICK, args);
                                }
                            }));
                } else {
                    boolean tradable = status == GameStatus.RUN &&
                            playerId != BoardActivity.this.playerId &&
                            players.get(playerId) != null;
                    buttons.add(new OverlayButton(
                            "Trade", tradable, 3,
                            new GestureRegionListener() {
                                @Override
                                public void onGestureRegionClick(GestureRegion gestureRegion) {
                                    Bundle args = new Bundle();
                                    args.putInt("playerId", playerId);
                                    sendToNetThread(BoardNetworkAction.MSG_TRADE_NEW, args);
                                }
                            }));
                }
                buttons.add(new OverlayButton(
                        "!ping", players.get(playerId) != null, 1,
                        new GestureRegionListener() {
                            @Override
                            public void onGestureRegionClick(GestureRegion gestureRegion) {
                                Player player = players.get(playerId);
                                if (player != null) {
                                    sendCommand("!ping " + player.getName());
                                }
                            }
                        }));
                buttons.add(new OverlayButton(
                        "!date", players.get(playerId) != null, 1,
                        new GestureRegionListener() {
                            @Override
                            public void onGestureRegionClick(GestureRegion gestureRegion) {
                                Player player = players.get(playerId);
                                if (player != null) {
                                    sendCommand("!date " + player.getName());
                                }
                            }
                        }));
                buttons.add(new OverlayButton(
                        "!ver", players.get(playerId) != null, 1,
                        new GestureRegionListener() {
                            @Override
                            public void onGestureRegionClick(GestureRegion gestureRegion) {
                                Player player = players.get(playerId);
                                if (player != null) {
                                    sendCommand("!version " + player.getName());
                                }
                            }
                        }));
                return buttons;
            }

            @Override
            public ArrayList<OverlayButton> getEstateOverlayButtons(final int estateId) {
                ArrayList<OverlayButton> buttons = new ArrayList<OverlayButton>(4);
                Estate estate = estates.get(estateId);
                if (estate != null && estate.canBeOwned()) {
                    boolean isOwner = estate.getOwner() == playerId;
                    boolean canMortgage = isOwner && estate.canToggleMortgage();
                    String mortgageText = estate.isMortgaged() ? "Unmortgage" : "Mortgage";
                    boolean canSellEstate = isOwner;
                    boolean canBuyHouse = isOwner && estate.canBuyHouses();
                    boolean canSellHouse = isOwner && estate.canSellHouses();
                    buttons.add(new OverlayButton(
                            mortgageText, canMortgage, 2,
                            new GestureRegionListener() {
                                @Override
                                public void onGestureRegionClick(GestureRegion gestureRegion) {
                                    Bundle args = new Bundle();
                                    args.putInt("estateId", estateId);
                                    sendToNetThread(BoardNetworkAction.MSG_ESTATE_MORTGAGE, args);
                                }
                            }));
                    buttons.add(new OverlayButton(
                            "+ House", canBuyHouse, 1,
                            new GestureRegionListener() {
                                @Override
                                public void onGestureRegionClick(GestureRegion gestureRegion) {
                                    Bundle args = new Bundle();
                                    args.putInt("estateId", estateId);
                                    sendToNetThread(BoardNetworkAction.MSG_ESTATE_BUYHOUSE, args);
                                }
                            }));
                    buttons.add(new OverlayButton(
                            "- House", canSellHouse, 1,
                            new GestureRegionListener() {
                                @Override
                                public void onGestureRegionClick(GestureRegion gestureRegion) {
                                    Bundle args = new Bundle();
                                    args.putInt("estateId", estateId);
                                    sendToNetThread(BoardNetworkAction.MSG_ESTATE_SELLHOUSE, args);
                                }
                            }));
                    buttons.add(new OverlayButton(
                            "Sell to bank", canSellEstate, 2,
                            new GestureRegionListener() {
                                @Override
                                public void onGestureRegionClick(GestureRegion gestureRegion) {
                                    Bundle args = new Bundle();
                                    args.putInt("estateId", estateId);
                                    sendToNetThread(BoardNetworkAction.MSG_ESTATE_SELL, args);
                                }
                            }));
                }
                return buttons;
            }

            @Override
            public ArrayList<OverlayButton> getAuctionOverlayButtons(final int auctionId) {
                ArrayList<OverlayButton> buttons = new ArrayList<OverlayButton>(5);
                final int[] bids = { 1, 10, 50, 100, -1 };
                for (int i = 0; i < bids.length; i++) {
                    // A final copy of "i" to be used in the following code block.
                    final int index = i;
                    int length = 1;
                    String text = "+ $" + bids[index];
                    if (bids[index] < 0) {
                        text = "Custom";
                        length = 2;
                    }
                    buttons.add(new OverlayButton(
                            text, true, length,
                            new GestureRegionListener() {
                                @Override
                                public void onGestureRegionClick(GestureRegion gestureRegion) {
                                    if (bids[index] > 0) {
                                        int amount = auction.getHighBid() + bids[index];
                                        Bundle args = new Bundle();
                                        args.putInt("auctionId", auctionId);
                                        args.putInt("bid", amount);
                                        sendToNetThread(BoardNetworkAction.MSG_AUCTION_BID, args);
                                    } else {
                                        // TODO dialog: "How much do you want to bid? / Enter a bid amount / [ number text field ]
                                        // return to sendToNetThread(MSG_AUCTION_BID)
                                    }
                                }
                            }));
                }
                return buttons;
            }

            @Override
            public ArrayList<OverlayButton> getTradeOverlayButtons(final int tradeId) {
                Trade trade = trades.get(tradeId);
                if (trade == null) {
                    return new ArrayList<OverlayButton>(0);
                }
                // Note: store this now, so that if the trade overlay hasn't updated,
                // then the revision will be out of date on purpose so that the user can
                // always sees the trade revision they are accepting (in theory).
                final int tradeRevision = trade.getRevision();
                ArrayList<OverlayButton> buttons = new ArrayList<OverlayButton>(4);
                boolean isActive = trade.getLastUpdateType() != TradeUpdateType.COMPLETED &&
                        trade.getLastUpdateType() != TradeUpdateType.REJECTED;
                buttons.add(new OverlayButton(
                        "Accept", isActive, 2,
                        new GestureRegionListener() {
                            @Override
                            public void onGestureRegionClick(GestureRegion gestureRegion) {
                                Bundle args = new Bundle();
                                args.putInt("tradeId", tradeId);
                                args.putInt("revision", tradeRevision);
                                sendToNetThread(BoardNetworkAction.MSG_TRADE_ACCEPT, args);
                            }
                        }));
                buttons.add(new OverlayButton(
                        "Reject", isActive, 2,
                        new GestureRegionListener() {
                            @Override
                            public void onGestureRegionClick(GestureRegion gestureRegion) {
                                Bundle args = new Bundle();
                                args.putInt("tradeId", tradeId);
                                sendToNetThread(BoardNetworkAction.MSG_TRADE_REJECT, args);
                            }
                        }));
                buttons.add(new OverlayButton(
                        "Modify", isActive, 1,
                        new GestureRegionListener() {
                            @Override
                            public void onGestureRegionClick(GestureRegion gestureRegion) {
                                // TODO dialog: "What do you want to offer?
                                // 1. Choose an offer type / [ Money | Estate | Card ],
                                // 2. IF MONEY, choose a sender [ list of players ],
                                // 3. IF MONEY, Choose an offer amount / [ number text field ],
                                // 3. IF ESTATE, Choose an offer estate / [ list of estates with owners ],
                                // 3. IF CARD, Choose an offer card / [ list of cards in play ],
                                // 4. Choose a receiver [ list of players not including current owner of value ]
                                // return to sendToNetThread(MSG_TRADE_*)
                            }
                        }));
                buttons.add(new OverlayButton(
                        "Remove", isActive, 1,
                        new GestureRegionListener() {
                            @Override
                            public void onGestureRegionClick(GestureRegion gestureRegion) {
                                // TODO dialog: "What do you want to remove? / Choose a current offer / [ list of offers ]
                                // return to sendToNetThread(MSG_TRADE_*) that cancels this offer
                            }
                        }));
                return buttons;
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
                BoardViewOverlay overlayType = item.getOverlayType();
                if (item.getObjectId() < 0) {
                    overlayType = BoardViewOverlay.NONE;
                }
                switch (overlayType) {
                default:
                case NONE:
                    break;
                case PLAYER:
                    BoardActivity.this.boardView.overlayPlayerInfo(item.getObjectId());
                    break;
                case ESTATE:
                    BoardActivity.this.boardView.overlayEstateInfo(item.getObjectId());
                    break;
                case AUCTION:
                    BoardActivity.this.boardView.overlayAuctionInfo(item.getObjectId());
                    break;
                case TRADE:
                    BoardActivity.this.boardView.overlayTradeInfo(item.getObjectId());
                    break;
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
            public void onPlayerUpdate(final int updatePlayerId, final HashMap<String, String> data) {
                Log.v("monopd", "net: Received onPlayerUpdate() from MonoProtocolHandler");
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
                        Player player = players.get(updatePlayerId);
                        // if not found, add internally
                        if (player == null) {
                            player = new Player(updatePlayerId);
                        }
                        player.setGrayed(false);
                        // update internal player data
                        for (String key : data.keySet()) {
                            String value = data.get(key);
                            XmlAttribute attr = Player.playerAttributes.get(key);
                            if (attr == null) {
                                Log.w("monopd", "player." + key + " was unknown. Value = " + value);
                            } else {
                                if (key.equals("location")) {
                                    changingLocation = true;
                                }
                                if (key.equals("name")) {
                                    if (!value.equals(player.getName())) {
                                        oldNick = makePlayerName(player);
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
                        players.put(updatePlayerId, player);
                        // stop here if this player is not in this game (could be in the future)
                        if (player.getGameId() != gameItem.getGameId() || player.getGameId() < 0) {
                            // this player is not in this game
                            return;
                        }
                        // first player in list is master on joining a game
                        if (players.size() == 0) {
                            player.setMaster(true);
                        }
                        // find in player list
                        for (int i = 0; i < BoardViewPiece.MAX_PLAYERS; i++) {
                            if (playerIds[i] == updatePlayerId) {
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
                                case CONFIG:
                                case INIT:
                                    // add to player list
                                    for (int i = 0; i < BoardViewPiece.MAX_PLAYERS; i++) {
                                        if (playerIds[i] == 0) {
                                            playerIds[i] = updatePlayerId;
                                            isJoin = true;
                                            break;
                                        }
                                    }
                                    break;
                                case RUN:
                                case END:
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
                                    writeMessage(makePlayerName(player) + " is in the game as game master.", Color.GRAY, BoardViewOverlay.PLAYER, updatePlayerId);
                                    wasMaster = true;
                                } else {
                                    writeMessage(makePlayerName(player) + " is in the game.", Color.GRAY, BoardViewOverlay.PLAYER, updatePlayerId);
                                }
                            } else {
                                // normal join
                                writeMessage(makePlayerName(player) + " joined the game.", Color.GRAY, BoardViewOverlay.PLAYER, updatePlayerId);
                            }
                        } else if (oldNick != null) {
                            // nick changed!
                            writeMessage(oldNick + " is now known as " + makePlayerName(player) + ".", Color.GRAY, BoardViewOverlay.PLAYER, updatePlayerId);
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
                                    writeMessage(playerLeavingNick + " left the game giving game master to " + player.getName() + ".", Color.GRAY, BoardViewOverlay.PLAYER, updatePlayerId);
                                    playerLeavingNick = null;
                                } else {
                                    if (master > 0) {
                                        playerLeavingNick = makePlayerName(player);
                                    } else {
                                        writeMessage(makePlayerName(player) + " is now game master.", Color.GRAY, BoardViewOverlay.PLAYER, updatePlayerId);
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
                        if (status != GameStatus.RUN && status != GameStatus.END) {
                            boolean deleted = false;
                            for (int i = 0; i < BoardViewPiece.MAX_PLAYERS; i++) {
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
                                            for (int k = 0; k < BoardViewPiece.MAX_PLAYERS; k++) {
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
                        }
                        if (players.get(playerId) != null) {
                            if (status == GameStatus.CONFIG && players.get(playerId).getGameId() == gameItem.getGameId()) {
                                if (master == playerId) {
                                    playerLeavingNick = makePlayerName(players.get(playerId));
                                    master = 0;
                                } else {
                                    if (playerLeavingNick != null) {
                                        writeMessage(makePlayerName(players.get(playerId)) + " left the game giving game master to " + playerLeavingNick + ".", Color.GRAY, BoardViewOverlay.PLAYER, playerId);
                                        playerLeavingNick = null;
                                    } else {
                                        writeMessage(makePlayerName(players.get(playerId)) + " left the game.", Color.GRAY, BoardViewOverlay.PLAYER, playerId);
                                    }
                                }
                            } else if (status == GameStatus.RUN || status == GameStatus.END) {
                                players.get(playerId).setGrayed(true);
                                updatePlayerView();
                                return;
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
                        for (String key : data.keySet()) {
                            String value = data.get(key);
                            XmlAttribute attr = Estate.estateAttributes.get(key);
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
                            
                            // check for players already in game
                            //for (int i = 0; i < players.size(); i++) {
                            //    int playerId = players.keyAt(i);
                            //    Player player = players.get(playerId);
                            //    if (player.getGameId() == gameId) {
                            //        HashMap<String, String> data = new HashMap<String, String>();
                            //        data.put("game", Integer.toString(gameId));
                            //        onPlayerUpdate(playerId, data);
                            //    }
                            //}
                        }
                        
                        BoardActivity.this.status = GameStatus.fromString(status);
                        BoardActivity.this.boardView.setStatus(BoardActivity.this.status);
                        redrawRegions();
                        
                        switch (BoardActivity.this.status) {
                        case CONFIG:
                            //writeMessage("Entering a game...", Color.YELLOW, -1, -1);
                            clearCookie();
                            break;
                        case INIT:
                            //writeMessage("Starting game...", Color.YELLOW, -1, -1);
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
            public void onConfigUpdate(final int configId, final HashMap<String, String> data) {
                Log.v("monopd", "net: Received onConfigUpdate() from MonoProtocolHandler");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Configurable config = configurables.get(configId);
                        if (config == null) {
                            config = new Configurable(configId);
                        }
                        for (String key : data.keySet()) {
                            String value = data.get(key);
                            XmlAttribute attr = Configurable.configurableAttributes.get(key);
                            if (attr == null) {
                                Log.w("monopd", "configurable." + key + " was unknown. Value = " + value);
                            } else {
                                attr.set(config, value);
                            }
                        }
                        // XXX this is a hack to override editable field
                        // XXX because they say all "new style" configurables are editable by you
                        config.setEditable(isMaster);
                        configurables.put(configId, config);
                        if (boardView.isRunning()) {
                            boardView.drawConfigRegions(BoardActivity.this.configurables, isMaster);
                        }
                    }
                });                
            }

            @Override
            public void onConfigUpdate(final ArrayList<Configurable> configList) {
                Log.v("monopd", "net: Received onConfigUpdate() from MonoProtocolHandler");
                BoardActivity.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        //boolean fullList = false;
                        for (Configurable toAdd : configList) {
                            BoardActivity.this.configurables.put(toAdd.getConfigId(), toAdd);
                            //fullList = true;
                        }
                        if (boardView.isRunning()) {
                            //if (fullList) {
                            boardView.drawConfigRegions(BoardActivity.this.configurables, isMaster);
                            //} else {
                            //    boardView.redrawConfigRegions(BoardActivity.this.configurables, isMaster);
                            //}
                        }
                    }
                });
            }

            @Override
            public void onEstateGroupUpdate(final int estateGroupId, final HashMap<String, String> data) {
                Log.v("monopd", "net: Received onEstateGroupUpdate() from MonoProtocolHandler");
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        EstateGroup estateGroup = estateGroups.get(estateGroupId);
                        if (estateGroup == null) {
                            estateGroup = new EstateGroup(estateGroupId);
                        }
                        for (String key : data.keySet()) {
                            String value = data.get(key);
                            XmlAttribute attr = EstateGroup.estateGroupAttributes.get(key);
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
            public void onAuctionUpdate(final int auctionId, final HashMap<String, String> data) {
                Log.v("monopd", "net: Received onEstateGroupUpdate() from MonoProtocolHandler");
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        if (auctionId != 0) {
                            Log.w("monopd", "auction.auctionid was not 0. Value = " + auctionId);
                            return;
                        }
                        boolean isNew = false;
                        int orange = Color.rgb(255, 128, 0);
                        for (String key : data.keySet()) {
                            String value = data.get(key);
                            XmlAttribute attr = Auction.auctionAttributes.get(key);
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
                            writeMessage("AUCTION: " + makePlayerName(players.get(auction.getActorId())) +
                                    " is auctioning off " + makeEstateName(estates.get(auction.getEstateId())) +
                                    ".", orange, BoardViewOverlay.AUCTION, auction.getAuctionId());
                        } else {
                            switch (auction.getStatus()) {
                            case 0: // new bid
                                writeMessage("AUCTION: " + makePlayerName(players.get(auction.getHighBidder())) + " bid $" + auction.getHighBid() + ".", orange, BoardViewOverlay.PLAYER, auction.getHighBidder());
                                break;
                            case 1: // going once
                                writeMessage("AUCTION: Going once!", orange, BoardViewOverlay.AUCTION, auction.getAuctionId());
                                break;
                            case 2: // going twice
                                writeMessage("AUCTION: Going twice!", orange, BoardViewOverlay.AUCTION, auction.getAuctionId());
                                break;
                            case 3: // sold
                                writeMessage("AUCTION: Sold " + makeEstateName(estates.get(auction.getEstateId())) + " to " + makePlayerName(players.get(auction.getHighBidder())) + " for $" + auction.getHighBid() + ".", orange, BoardViewOverlay.AUCTION, auction.getAuctionId());
                                break;
                            }
                        }
                        boardView.overlayAuctionInfo(auction.getAuctionId());
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
                            
                            writeMessage(highlightPlayer(author) + ": " + text, Color.WHITE, BoardViewOverlay.PLAYER, playerId);
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
                        writeMessage("ERROR: " + text, Color.RED);
                    }
                });
            }

            @Override
            public void onInfoMessage(final String text) {
                Log.v("monopd", "net: Received onInfoMessage() from MonoProtocolHandler");
                BoardActivity.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        writeMessage("INFO: " + text, Color.LTGRAY);
                    }
                });
            }

            @Override
            public void onDisplayMessage(final int estateId, final String text, boolean clearText,
                    final boolean clearButtons, final ArrayList<Button> newButtons) {
                Log.v("monopd", "net: Received onDisplayMessage() from MonoProtocolHandler");
                BoardActivity.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
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
                        writeMessage("GAME: " + text, Color.CYAN, BoardViewOverlay.ESTATE, ((estateId == -1) ? -1 : estateId));
                    }
                });
            }

            @Override
            public void onPlayerListUpdate(String type, ArrayList<Player> list) {
                Log.v("monopd", "net: Received onPlayerListUpdate() from MonoProtocolHandler");
                /*if (type.equals("full")) {
                    //Log.d("monopd", "players: Full list update");
                    final int[] newPlayerIds = new int[4];
                    for (int i = 0; i < list.size() && i < BoardViewPiece.MAX_PLAYERS; i++) {
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
                        for (int j = 0; j < BoardViewPiece.MAX_PLAYERS; j++) {
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
                        for (int j = 0; j < BoardViewPiece.MAX_PLAYERS; j++) {
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
                    }
                    gameItem.setCanJoin(item.canJoin());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            BoardActivity.this.setTitle(getFullTitle());
                        }
                    });
                }
            }

            @Override
            public void onTradeUpdate(final int tradeId, final HashMap<String, String> data) {
                Log.v("monopd", "net: Received onTradeUpdate() from MonoProtocolHandler");
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        Trade trade = trades.get(tradeId);
                        if (trade == null) {
                            trade = new Trade(tradeId);
                        }
                        for (String key : data.keySet()) {
                            String value = data.get(key);
                            XmlAttribute attr = Trade.tradeAttributes.get(key);
                            if (attr == null) {
                                Log.w("monopd", "trade." + key + " was unknown. Value = " + value);
                            } else {
                                attr.set(trade, value);
                            }
                        }
                        if (!data.containsKey("type")) {
                            trade.setType("updated");
                        }
                        switch (trade.getLastUpdateType()) {
                        case NEW:
                            if (trade.getActorId() == playerId) {
                                writeMessage("TRADE: You initiate a trade.", Color.MAGENTA, BoardViewOverlay.TRADE, tradeId);
                            } else {
                                writeMessage("TRADE: " + makePlayerName(players.get(trade.getActorId())) +
                                        " initiated a trade.", Color.MAGENTA, BoardViewOverlay.TRADE, tradeId);
                            }
                            trade.setCreatorId(trade.getActorId());
                            break;
                        default:
                            // updated (changes made in other onTrade* calls)
                            break;
                        case ACCEPTED:
                            if (trade.getActorId() == playerId) {
                                writeMessage("TRADE: You accept the trade.", Color.MAGENTA, BoardViewOverlay.TRADE, tradeId);
                            } else {
                                writeMessage("TRADE: " + makePlayerName(players.get(trade.getActorId())) + " accepts the trade.", Color.MAGENTA, BoardViewOverlay.TRADE, tradeId);
                            }
                            break;
                        case COMPLETED:
                            writeMessage("TRADE: The trade has completed successfully.", Color.MAGENTA, BoardViewOverlay.TRADE, tradeId);
                            break;
                        case REJECTED:
                            if (trade.getActorId() == playerId) {
                                writeMessage("TRADE: You rejected the trade.", Color.MAGENTA, BoardViewOverlay.TRADE, tradeId);
                            } else {
                                writeMessage("TRADE: " + makePlayerName(players.get(trade.getActorId())) + " rejected the trade.", Color.MAGENTA, BoardViewOverlay.TRADE, tradeId);
                            }
                            break;
                        }
                        trades.put(tradeId, trade);
                        boardView.overlayTradeInfo(tradeId);
                    }
                });
            }

            @Override
            public void onTradePlayer(final int tradeId, final TradePlayer player) {
                Log.v("monopd", "net: Received onTradePlayer() from MonoProtocolHandler");
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        Trade trade = trades.get(tradeId);
                        if (trade == null) {
                            trade = new Trade(tradeId);
                        }
                        trade.setPlayer(player);
                        trades.put(tradeId, trade);
                        boardView.overlayTradeInfo(tradeId);
                    }
                });
            }

            @Override
            public void onTradeMoney(final int tradeId, final MoneyTradeOffer offer) {
                Log.v("monopd", "net: Received onTradeMoney() from MonoProtocolHandler");
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        Trade trade = trades.get(tradeId);
                        if (trade == null) {
                            trade = new Trade(tradeId);
                        }
                        trade.mergeMoneyOffer(offer);
                        if (offer.getAmount() == 0) {
                            writeMessage("TRADE: Proposal update: " +
                                        makePlayerName(players.get(offer.getPlayerIdFrom())) +
                                        " no longer gives money to " + makePlayerName(players.get(offer.getPlayerIdTo())) +
                                        ".", Color.MAGENTA, BoardViewOverlay.TRADE, tradeId);
                        } else {
                            writeMessage("TRADE: Proposal update: " +
                                        makePlayerName(players.get(offer.getPlayerIdFrom())) +
                                        " gives $" + offer.getAmount() + " to " +
                                        makePlayerName(players.get(offer.getPlayerIdTo())) + ".", Color.MAGENTA, BoardViewOverlay.TRADE, tradeId);
                        }
                        trades.put(tradeId, trade);
                        boardView.overlayTradeInfo(tradeId);
                    }
                });
            }

            @Override
            public void onTradeEstate(final int tradeId, final EstateTradeOffer offer) {
                Log.v("monopd", "net: Received onTradeEstate() from MonoProtocolHandler");
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        Trade trade = trades.get(tradeId);
                        if (trade == null) {
                            trade = new Trade(tradeId);
                        }
                        trade.mergeEstateOffer(offer);
                        if (offer.getPlayerIdFrom() == offer.getPlayerIdTo()) {
                            writeMessage("TRADE: Proposal update: " +
                                    makePlayerName(players.get(offer.getPlayerIdFrom())) +
                                    " no longer gives " + makeEstateName(estates.get(offer.getEstateId())) +
                                    " away.", Color.MAGENTA, BoardViewOverlay.TRADE, tradeId);
                        } else {
                            writeMessage("TRADE: Proposal update: " +
                                    makePlayerName(players.get(offer.getPlayerIdFrom())) +
                                    " gives " + makeEstateName(estates.get(offer.getEstateId())) +
                                    " to " + makePlayerName(players.get(offer.getPlayerIdTo())) + ".", Color.MAGENTA, BoardViewOverlay.TRADE, tradeId);
                        }
                        trades.put(tradeId, trade);
                        boardView.overlayTradeInfo(tradeId);
                    }
                });
            }

            @Override
            public void onTradeCard(final int tradeId, final CardTradeOffer offer) {
                Log.v("monopd", "net: Received onTradeCard() from MonoProtocolHandler");
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        Trade trade = trades.get(tradeId);
                        if (trade == null) {
                            trade = new Trade(tradeId);
                        }
                        trade.mergeCardOffer(offer);
                        if (offer.getPlayerIdFrom() == offer.getPlayerIdTo()) {
                            writeMessage("TRADE: Proposal update: " +
                                    makePlayerName(players.get(offer.getPlayerIdFrom())) +
                                    " no longer gives card #" + offer.getCardId() +
                                    " away.", Color.MAGENTA, BoardViewOverlay.TRADE, tradeId);
                        } else {
                            writeMessage("TRADE: Proposal update: " +
                                    makePlayerName(players.get(offer.getPlayerIdFrom())) +
                                    " gives card #" + offer.getCardId() +
                                    " to " + makePlayerName(players.get(offer.getPlayerIdTo())) + ".", Color.MAGENTA, BoardViewOverlay.TRADE, tradeId);
                        }
                        trades.put(tradeId, trade);
                        boardView.overlayTradeInfo(tradeId);
                    }
                });
            }

            @Override
            public int getEstateOwner(int estateId) {
                return estates.get(estateId).getOwner();
            }

            @Override
            public int getCardOwner(int cardId) {
                // TODO: Card Object NYI.
                // return cards.get(cardId).getOwner();
                return -1;
            }
        });
        
        this.setTitle(getFullTitle());
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
    
    public void onPlayer5Click(View v) {
        this.boardView.overlayPlayerInfo(playerIds[4]);
    }
    
    public void onPlayer6Click(View v) {
        this.boardView.overlayPlayerInfo(playerIds[5]);
    }
    
    public void onPlayer7Click(View v) {
        this.boardView.overlayPlayerInfo(playerIds[6]);
    }
    
    public void onPlayer8Click(View v) {
        this.boardView.overlayPlayerInfo(playerIds[7]);
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
        case END:
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
        for (int i = 0; i < BoardViewPiece.MAX_PLAYERS; i++) {
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
                for (int j = 0; j <= BoardViewSurfaceThread.animationSteps; j++) {
                    if (j == BoardViewSurfaceThread.animationSteps) {
                        BoardViewPiece.pieces[playerIndex].setProgressEstateDelta(0);
                    } else {
                        BoardViewPiece.pieces[playerIndex].setProgressEstateDelta(j);
                    }
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
        this.playerView[4] = (LinearLayout) this.findViewById(R.id.board_player_item_5);
        this.playerView[5] = (LinearLayout) this.findViewById(R.id.board_player_item_6);
        this.playerView[6] = (LinearLayout) this.findViewById(R.id.board_player_item_7);
        this.playerView[7] = (LinearLayout) this.findViewById(R.id.board_player_item_8);
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
     *            The text. Supports simple HTML formatting.
     * @param color
     *            The color.
     * @param overlayType
     *            The type of associated overlay. Set to NONE to make this message not tappable.
     * @param objectId
     *            The object ID associated with this message. Set to negative to ignore, or will be ignored if overlayType is NONE.
     * @param clearButtons
     *            Whether the buttons should be cleared, if any. TODO: move to another function.
     */
    private void writeMessage(String msgText, int color, BoardViewOverlay overlayType, int objectId) {
        BoardActivity.this.chatListAdapter.add(new ChatItem(msgText, color, overlayType, objectId));
        BoardActivity.this.chatListAdapter.notifyDataSetChanged();
    }
    
    private void writeMessage(String msgText, int color) {
        writeMessage(msgText, color, BoardViewOverlay.NONE, -1);
    }

    /*
     * Set the player list to show the specified 4 players. Player ID 0 means
     * that slot is empty.
     * 
     * @param playerIds
     *            Player IDs of the players to show.
     *
    private void setPlayerView(int[] playerIds) {
        this.playerIds = playerIds;
        this.updatePlayerView();
    }*/

    /**
     * Updates the player view with new data from the player list.
     */
    private void updatePlayerView() {
        for (int i = 0; i < BoardViewPiece.MAX_PLAYERS; i++) {
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
                    case ERROR:
                    case JOIN:
                    case CREATE:
                    case CONFIG:
                        if (player.isMaster()) {
                            text1.setTextColor(Color.YELLOW);
                        } else {
                            text1.setTextColor(Color.WHITE);
                        }
                        
                        text2.setText(player.getHost());
                        break;
                    case RECONNECT:
                    case INIT:
                    case RUN:
                    case END:
                        if (player.isGrayed()) {
                            text1.setTextColor(Color.GRAY);
                        } else if (player.isTurn()) {
                            text1.setTextColor(Color.YELLOW);
                        } else {
                            text1.setTextColor(Color.WHITE);
                        }
                        
                        Drawable draw = BoardViewPiece.pieces[i].getDrawable(For.PLAYER_LIST);
                        
                        text2.setText("$" + Integer.toString(player.getMoney()));
                        text2.setCompoundDrawablePadding(5);
                        text2.setCompoundDrawablesWithIntrinsicBounds(draw, null, null, null);
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
        MenuItem mi = (MenuItem) menu.findItem(R.id.menu_bankrupt);
        mi.setEnabled(status == GameStatus.RUN && players.get(playerId).isInDebt());
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch (item.getItemId()) {
        case R.id.menu_bankrupt:
            if (status == GameStatus.RUN && players.get(playerId).isInDebt()) {
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

    private String getFullTitle() {
        return gameItem.getDescription() + "  #" + gameItem.getGameId() + ": " + gameItem.getTypeName() + " (" + gameItem.getType() + ")";
    }

    /**
     * Make a universal string used for representing a number of houses.
     * @param houses The number of houses.
     * @return An HTML formatted string.
     */
    public static String makeHouseCount(int houses) {
        return "<b><font color=\"" + Estate.getHouseHtmlColor(houses) + "\">" +
        (houses == 5 ? "H" : houses) + "</font></b>";
    }

    /**
     * Make a universal string used for representing an Estate by name and color.
     * @param estate The estate.
     * @return An HTML formatted string.
     */
    public static String makeEstateName(Estate estate) {
        if (estate == null) return "";
        return "<b><font color=\"" + estate.getHtmlColor() + "\">" +
        estate.getName() + "</font></b>";
    }

    /**
     * Make a universal string used for representing a Player by name and color.
     * @param player The player.
     * @return An HTML formatted string.
     */
    public static String makePlayerName(Player player) {
        if (player == null) return "";
        BoardViewPiece piece = BoardViewPiece.getPiece(player.getPlayerId());
        int color = Color.WHITE;
        if (piece != null) {
            color = piece.getColor();
        }
        
        return "<b><font color=\"" + Estate.getHtmlColor(color) + "\">" +
        player.getName() + "</font></b>";
    }

    /**
     * Make a key-value pair line used universally.
     * @param key The bolded part before the colon.
     * @param value Any text to put after the colon.
     * @return An HTML formatted string. Ends with a line break.
     */
    public static String makeFieldLine(String key, String value) {
        return "<b>" + key + "</b>: " + value + "<br>";
    }
    
    private String highlightPlayer(String playerName) {
        for (int i = 0; i < BoardViewPiece.MAX_PLAYERS; i++) {
            if (playerIds[i] != 0) {
                Player player = players.get(playerIds[i]);
                if (player.getName().equals(playerName)) {
                    return makePlayerName(player);
                }
            }
        }
        return playerName;
    }
}