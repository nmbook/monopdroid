package com.natembook.monopdroid.board;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
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
import com.natembook.monopdroid.dialogs.MonopolyDialog;
import com.natembook.monopdroid.dialogs.MonopolyDialogHost;
import com.natembook.monopdroid.dialogs.MonopolyDialogListener;
import com.natembook.monopdroid.gamelist.GameItem;
import com.natembook.monopdroid.gamelist.GameItemType;
import com.natembook.monopdroid.gamelist.ServerItem;
import com.natembook.monopdroid.monopd.MonoProtocolGameListener;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * The primary game Activity, for a game lobby and game.
 * @author Nate
 *
 */
public class BoardActivity extends FragmentActivity implements
        BoardViewListener, MonoProtocolGameListener,
        OnKeyListener, OnItemClickListener,
        MonopolyDialogHost, MonopolyDialogListener {
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
     * List of auctions. Save on restart. 
     */
    private SparseArray<Auction> auctions = new SparseArray<Auction>();
    /**
     * List of trades. Save on restart.
     */
    private SparseArray<Trade> trades = new SparseArray<Trade>();
    /**
     * List of cards. Save on restart.
     */
    private SparseArray<Card> cards = new SparseArray<Card>();
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
    private int selfPlayerId = -1;
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
     * Device name. Save on restart.
     */
    private String deviceName;
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
    private int master = -1;
    /**
     * Whether this onDestroy() occured after saving state.
     */
    private boolean savingState = false;
    /**
     * Whether this onResume() occured with intent info (true) or saved state data (false).
     */
    private boolean firstInit = false;
    
    /**
     * Currently saved configuration.
     */
    private HashMap<String, String> savedConfig = null;
    
    /**
     * Used to make sure a dialog may open at this time.
     */
    private boolean running = false;
    /**
     * Stores a single item backlog of dialogs to show when this Activity
     * is brought back to the front.
     */
    private MonopolyDialog dialog = null;
    
    /**
     * A task to run to roll after two seconds.
     */
    private Timer rollTimeout = null;
    
    /**
     * A task to run to turn after 300ms.
     */
    private Timer turnTimer = null;
    
    /**
     * Static handler for HTML tag parsing. TODO: use BoardTextFormatter
     */
    public static TagHandler tagHandler = null;
    
    /**
     * Time from roll button appearance to auto-roll action.
     */
    private static final int autoRollTimeout = 2000;
    
    /**
     * Time to wait per turn step.
     */
    private static final int turnTimeout = 300;
    
    public BoardActivity() {
        super();
        for (int i = 0; i < BoardViewPiece.MAX_PLAYERS; i++) {
            playerIds[i] = -1;
        }
    }

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
            int game_id = i.getIntExtra("com.natembook.game_id", 0);
            String host = i.getStringExtra("com.natembook.host");
            int port = i.getIntExtra("com.natembook.port", 0);
            String version = i.getStringExtra("com.natembook.version");
            String type = i.getStringExtra("com.natembook.type");
            String type_name = i.getStringExtra("com.natembook.type_name");
            String descr = i.getStringExtra("com.natembook.descr");
            int playerCount = i.getIntExtra("com.natembook.players", 0);
            boolean can_join = i.getBooleanExtra("com.natembook.can_join", false);
            GameItemType item_type = GameItemType.fromInt(i.getIntExtra("com.natembook.act_type", 0));
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
            this.deviceName = Build.MODEL + " (Android " + Build.VERSION.RELEASE + ")";
            this.nickname = prefs.getString("player_nick", "anonymous");
            
            int savedConfigEntries = prefs.getInt("scfgCount", 0);
            if (savedConfigEntries > 0) {
                savedConfig = new HashMap<String, String>();
                for (int k = 0; k < savedConfigEntries; k++) {
                    savedConfig.put(prefs.getString("scfgCmd" + k, ""),
                            prefs.getString("scfgVal" + k, ""));
                }
            }
            
            this.netRunnable = new BoardActivityNetworkThread();
        } else {
            state.restoreState(this);
        }
        
        this.boardView.setBoardViewListener(this);
        this.chatSendBox.setOnKeyListener(this);
        this.chatList.setOnItemClickListener(this);
        this.netRunnable.setActivity(this, this);
        this.setTitle(getFullTitle());

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
        private SparseArray<Auction> auctions;
        private SparseArray<Trade> trades;
        private SparseArray<Card> cards;
        private SparseArray<Configurable> configurables;
        private int selfPlayerId;
        private String cookie;
        private GameStatus status;
        private String clientName;
        private String clientVersion;
        private String deviceName;
        private String nickname;
        private boolean isMaster;
        private int master;
        private MonopolyDialog dialog;
        
        public void saveState(BoardActivity activity) {
            Log.v("monopd", "board save");
            this.surfaceRunner = activity.boardView.saveState();
            this.chats = activity.chatListAdapter.saveState();
            this.playerIds = activity.playerIds;
            this.netRunnable = activity.netRunnable;
            this.gameItem = activity.gameItem;
            this.players = activity.players;
            this.estates = activity.estates;
            this.auctions = activity.auctions;
            this.trades = activity.trades;
            this.cards = activity.cards;
            this.configurables = activity.configurables;
            this.selfPlayerId = activity.selfPlayerId;
            this.cookie = activity.cookie;
            this.status = activity.status;
            this.clientName = activity.clientName;
            this.clientVersion = activity.clientVersion;
            this.deviceName = activity.deviceName;
            this.nickname = activity.nickname;
            this.isMaster = activity.isMaster;
            this.master = activity.master;
            this.dialog = activity.dialog;
        }
        
        public void restoreState(BoardActivity activity) {
            Log.v("monopd", "board restore");
            activity.boardView.restoreState(this.surfaceRunner);
            activity.chatListAdapter.restoreState(this.chats);
            activity.playerIds = this.playerIds;
            activity.netRunnable = this.netRunnable;
            activity.gameItem = this.gameItem;
            activity.players = this.players;
            activity.estates = this.estates;
            activity.auctions = this.auctions;
            activity.trades = this.trades;
            activity.configurables = this.configurables;
            activity.selfPlayerId = this.selfPlayerId;
            activity.cookie = this.cookie;
            activity.status = this.status;
            activity.clientName = this.clientName;
            activity.clientVersion = this.clientVersion;
            activity.deviceName = this.deviceName;
            activity.nickname = this.nickname;
            activity.isMaster = this.isMaster;
            activity.master = this.master;
            activity.dialog = this.dialog;
        }
    }

    private void showConnectionError(String error) {
        Bundle args = new Bundle();
        args.putInt("dialogType", R.id.dialog_type_reconnect);
        args.putString("title", getString(R.string.dialog_conn_error));
        args.putString("message", error);
        dialog = MonopolyDialog.showNewDialog(this, args);
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
    public boolean isRunning() {
        return running;
    }

    @Override
    public MonopolyDialog getCurrentDialog() {
        return dialog;
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
            Bundle args = new Bundle();
            args.putInt("dialogType", R.id.dialog_type_confirmquit);
            args.putString("title", getString(R.string.dialog_confirm_quit));
            args.putString("message", getString(R.string.confirm_quit));
            MonopolyDialog.showNewDialog(this, args);
        } else {
            finish();
        }
    }

    private int initPlayerColors() {
        int count = 0;
        for (int playerId : playerIds) {
            if (playerId >= 0) {
                BoardViewPiece piece = BoardViewPiece.pieces[count];
                piece.setPlayerId(playerId);
                piece.setMoving(false);
                piece.setCurrentEstate(0);
                piece.setProgressEstate(0);
                piece.setProgressEstateDelta(0);
                count++;
            }
        }
        return count;
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
            for (int playerId : playerIds) {
                if (playerId >= 0 && players.get(playerId).isTurn()) {
                    boardView.drawActionRegions(playerId);
                    break;
                }
            }
            boardView.drawPieces(estates, playerIds, players);
            break;
        case END:
            boardView.calculateBoardRegions();
            boardView.drawBoardRegions(estates, players);
            boardView.drawActionRegions(-1);
            boardView.drawPieces(estates, playerIds, players);
            break;
        }
        boardView.redrawOverlay();
    }

    private void animateMove(final int playerId) {
        Player player = players.get(playerId);
        final int start = player.getLastLocation();
        final int end = player.getLocation() +
                (player.getLocation() < start ? 40 : 0);
        boolean directMove = player.getDirectMove();
        final int pieceIndex = BoardViewPiece.getIndexOf(playerId);
        BoardViewPiece piece = BoardViewPiece.pieces[pieceIndex];
        piece.setMoving(true);
        piece.setCurrentEstate(end % 40);
        piece.setProgressEstate(start);
        piece.setProgressEstateDelta(0);
        if (directMove) {
            Bundle args = new Bundle();
            args.putInt("estateId", end % 40);
            sendToNetThread(BoardNetworkAction.MSG_TURN, args);
            piece.setProgressEstate(end % 40);
            piece.setMoving(false);
            boardView.drawActionRegions(playerId);
            boardView.drawPieces(estates, playerIds, players);
            boardView.redrawOverlay();
        } else {
            if (turnTimer == null) {
                turnTimer = new Timer();
            }
            turnTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    animateMoveStep(playerId, pieceIndex, start, end, start + 1);
                }
            }, turnTimeout);
        }
    }
    
    private void animateMoveStep(final int playerId, final int pieceIndex, final int start, final int end, final int index) {
        Bundle args = new Bundle();
        args.putInt("estateId", (index % 40));
        sendToNetThread(BoardNetworkAction.MSG_TURN, args);

        BoardViewPiece piece = BoardViewPiece.pieces[pieceIndex];
        piece.setProgressEstate((index - 1) % 40);
        piece.setCurrentEstate(index % 40);
        /*for (int j = 0; j <= BoardViewSurfaceThread.animationSteps; j++) {
            piece.setProgressEstateDelta(j);
            boardView.drawPieces(estates, playerIds, players);
            boardView.redrawOverlay();
            boardView.waitDraw();
        }*/
        boardView.drawActionRegions(playerId);
        boardView.drawPieces(estates, playerIds, players);
        boardView.redrawOverlay();
        
        if (index < end) {
            turnTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    animateMoveStep(playerId, pieceIndex, start, end, index + 1);
                }
            }, turnTimeout);
        } else {
            piece.setMoving(false);
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
        sendToNetThread(action, arguments, 0);
    }


    /**
     * Sends a message to the network thread. Use this and not
     * nethandler.dispatchMessage()
     * 
     * @param action
     *            The message ID.
     * @param arguments
     *            Named arguments of the message. Can be null to specify zero arguments.
     * @param msDelay
     *            Delay for some posts.
     */
    private void sendToNetThread(BoardNetworkAction action, Bundle arguments, int msDelay) {
        if (netHandler == null) {
            return;
        }
        final Message msg = Message.obtain(netHandler, action.getWhat());
        msg.setData(arguments);
        if (msDelay > 0) {
            netHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    netHandler.dispatchMessage(msg);                
                } 
            }, msDelay);
        } else {
            netHandler.post(new Runnable() {
                @Override
                public void run() {
                    netHandler.dispatchMessage(msg);                
                } 
            });
        }
    }

    /**
     * Write a message to the chat list. The chat item will be tappable if
     * overlayType is not NONE and objectId >= 0
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
     *            Whether the buttons should be cleared, if any.
     */
    private void writeMessage(String msgText, int color, BoardViewOverlay overlayType, int objectId) {
        chatListAdapter.add(new ChatItem(msgText, color, overlayType, objectId));
        chatListAdapter.notifyDataSetChanged();
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
            if (this.playerIds[i] < 0) {
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
                    if (player.getPlayerId() == selfPlayerId) {
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
        return true;
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean shown = super.onPrepareOptionsMenu(menu);
        
        // only show declare bankruptcy if mode is RUN; only allow if we are in debt
        MenuItem declareBankrupt = (MenuItem) menu.findItem(R.id.menu_bankrupt);
        if (status == GameStatus.RUN) {
            declareBankrupt.setVisible(true);
            declareBankrupt.setEnabled(players.get(selfPlayerId).isInDebt());
        } else {
            declareBankrupt.setVisible(false);
        }

        // only show set description if mode is CONFIG; only allow if we are game master
        MenuItem setDescription = (MenuItem) menu.findItem(R.id.menu_descr);
        if (status == GameStatus.CONFIG) {
            setDescription.setVisible(true);
            setDescription.setEnabled(isMaster);
        } else {
            setDescription.setVisible(false);
        }

        // only show save config if mode is CONFIG
        MenuItem saveCfg = (MenuItem) menu.findItem(R.id.menu_save_cfg);
        saveCfg.setVisible(status == GameStatus.CONFIG);
        
        // only show load config if mode is CONFIG; only allow if we are game master & a config exists
        MenuItem loadCfg = (MenuItem) menu.findItem(R.id.menu_load_cfg);
        if (status == GameStatus.CONFIG) {
            loadCfg.setVisible(true);
            loadCfg.setEnabled(isMaster && savedConfig != null);
        } else {
            loadCfg.setVisible(false);
        }
        
        return shown;
        
        
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        Bundle args = new Bundle();
        switch (item.getItemId()) {
        case R.id.menu_descr:
            if (status == GameStatus.CONFIG) {
                args.putInt("dialogType", R.id.dialog_type_prompt_name);
                args.putString("title", getString(R.string.pref_game_descr_dialog_title));
                args.putString("message", getString(R.string.pref_game_descr_summary));
                args.putString("default", gameItem.getDescription());
                args.putInt("minLength", 1);
                args.putInt("action", BoardNetworkAction.MSG_GAME_DESCRIPTION.getWhat());
                dialog = MonopolyDialog.showNewDialog(this, args);
            }
            break;
        case R.id.menu_save_cfg:
            if (status == GameStatus.CONFIG) {
                saveCurrentConfiguration();
            }
            break;
        case R.id.menu_load_cfg:
            if (status == GameStatus.CONFIG && savedConfig != null && isMaster) {
                loadConfiguration();
            }
            break;
        case R.id.menu_bankrupt:
            if (status == GameStatus.RUN && players.get(selfPlayerId).isInDebt()) {
                sendToNetThread(BoardNetworkAction.MSG_DECLARE_BANKRUPCY, null);
            }
            break;
        case R.id.menu_name:
            args.putInt("dialogType", R.id.dialog_type_prompt_name);
            args.putString("title", getString(R.string.pref_player_nick_dialog_title));
            args.putString("message", getString(R.string.pref_player_nick_summary));
            args.putString("default", nickname);
            args.putInt("minLength", 1);
            args.putInt("action", BoardNetworkAction.MSG_NICK.getWhat());
            dialog = MonopolyDialog.showNewDialog(this, args);
            break;
        case R.id.menu_settings:
            Intent settings = new Intent(this, SettingsActivity.class);
            this.startActivity(settings);
            break;
        case R.id.menu_quit:
            if (status == GameStatus.RUN) {
                args.putInt("dialogType", R.id.dialog_type_confirmquit);
                args.putString("title", getString(R.string.dialog_confirm_quit));
                args.putString("message", getString(R.string.confirm_quit));
                MonopolyDialog.showNewDialog(this, args);
            } else {
                finish();
            }
            break;
        }
        return true;
    }

    private void saveCurrentConfiguration() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Editor editor = prefs.edit();
        
        editor.putInt("scfgCount", configurables.size());
        savedConfig = new HashMap<String, String>();
        for (int i = 0; i < configurables.size(); i++) {
            int index = configurables.keyAt(i);
            Configurable config = configurables.get(index);
            editor.putString("scfgCmd" + i, config.getCommand());
            editor.putString("scfgVal" + i, config.getValue());
            savedConfig.put(config.getCommand(), config.getValue());
        }
        
        editor.commit();
        Toast.makeText(this, "Saved current configuration.", Toast.LENGTH_SHORT).show();
    }

    private String getFullTitle() {
        String gameNumber = "";
        if (gameItem.getGameId() >= 0) {
            gameNumber = "#" + gameItem.getGameId() + ": ";
        }
        return gameItem.getDescription() + " / " + gameNumber + gameItem.getTypeName() + " (" + gameItem.getType() + ")";
    }

    private void loadConfiguration() {
        Bundle args = new Bundle();
        int updatedCount = 0;
        for (String key : savedConfig.keySet()) {
            boolean needsUpdating = false;
            for (int i = 0; i < configurables.size(); i++) {
                int index = configurables.keyAt(i);
                Configurable config = configurables.get(index);
                if (config.getCommand().equals(key)) {
                    if (!config.getValue().equals(savedConfig.get(key))) {
                        needsUpdating = true;
                    }
                    break;
                }
            }
            if (needsUpdating) {
                Log.v("monopd", "scfg: " + key + " = " + savedConfig.get(key));
                args.putString("command", key);
                args.putString("value", savedConfig.get(key));
                sendToNetThread(BoardNetworkAction.MSG_CONFIG, args, 200 * updatedCount);
                updatedCount++;
            }
        }
        Toast.makeText(this, "Loaded configuration.", Toast.LENGTH_SHORT).show();
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
        String estateName = estate.getName();
        if (estateName == null) {
            estateName = "#" + estate.getEstateId();
        }
        return "<b><font color=\"" + estate.getHtmlColor() + "\">" +
        escapeHtml(estateName) + "</font></b>";
    }

    /**
     * Make a universal string used for representing a Player by name and color.
     * @param player The player.
     * @return An HTML formatted string.
     */
    public static String makePlayerName(Player player) {
        if (player == null) return "";
        String playerName = player.getName();
        BoardViewPiece piece = BoardViewPiece.getPiece(player.getPlayerId());
        int color = Color.WHITE;
        if (piece != null) {
            color = piece.getColor();
        }
        
        if (playerName == null) {
            playerName = "#" + player.getPlayerId();
        }
        
        return "<b><font color=\"" + Estate.getHtmlColor(color) + "\">" +
        escapeHtml(playerName) + "</font></b>";
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

    /**
     * Escapes the given string for the places we use HTML formatting here (very few).
     * @param value
     * @return
     */
    public static String escapeHtml(String value) {
        return value.replace("&", "&amp;").replace("<", "&lt;");
    }
    
    /**
     * Given a String representing a player name, find the player's ID and thus object to send to {@link makePlayerName}.
     * @param playerName
     * @return
     */
    private String highlightPlayer(String playerName) {
        for (int playerId : playerIds) {
            if (playerId >= 0) {
                Player player = players.get(playerId);
                if (player.getName().equals(playerName)) {
                    return makePlayerName(player);
                }
            }
        }
        return playerName;
    }


    @Override
    public void onConfigChange(String command, String value) {
        Log.d("monopd", "BoardView tapped config change " + command + " = " + value);
        Bundle state = new Bundle();
        state.putString("command", command);
        state.putString("value", value);
        sendToNetThread(BoardNetworkAction.MSG_CONFIG, state);
    }

    @Override
    public void onStartGame() {
        Log.d("monopd", "BoardView tapped start game");
        sendToNetThread(BoardNetworkAction.MSG_GAME_START, null);
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
                writeMessage("Joining game: " + escapeHtml(getFullTitle()), Color.YELLOW);
                break;
            case CREATE:
                status = GameStatus.CREATE;
                boardView.setStatus(GameStatus.CREATE);
                writeMessage("Creating game: " + escapeHtml(getFullTitle()), Color.YELLOW);
                break;
            case RECONNECT:
                status = GameStatus.RECONNECT;
                boardView.setStatus(GameStatus.RECONNECT);
                writeMessage("Reconnecting to game: " + escapeHtml(getFullTitle()), Color.YELLOW);
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
        buttons.clear();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (rollTimeout != null) {
                    rollTimeout.cancel();
                    rollTimeout = null;
                }
            }
        });
        Bundle args = new Bundle();
        args.putString("command", command);
        sendToNetThread(BoardNetworkAction.MSG_BUTTON_COMMAND, args);
    }

    @Override
    public String getActionText(int playerId) {
        if (status == GameStatus.RUN) {
            Player player = players.get(playerId);
            Estate estate = estates.get(player.getLocation());
            String location = "On " + BoardActivity.makeEstateName(estate);
            if (player.isJailed()) {
                location = "In <b><font color=\"red\">Jail</font></b>";
            }
            BoardViewPiece piece = BoardViewPiece.getPiece(playerId);
            if (piece != null) {
                Estate pieceEstate = estates.get(piece.getCurrentEstate());
                if (piece.isMoving() && pieceEstate != null) {
                    location = "Moving over " + makeEstateName(pieceEstate);
                }
            }
            
            String actionText = "Current turn is " + BoardActivity.makePlayerName(player) + "<br>" + location + "<br>";
            if (player.canRoll()) {
                if (player.getPlayerId() == selfPlayerId) {
                    actionText += "Roll the dice:";
                } else {
                    actionText += "Player may roll the dice.";
                }
            } else if (player.canBuyEstate()) {
                if (player.getPlayerId() == selfPlayerId) {
                    actionText += "Buy estate for $" + estates.get(player.getLocation()).getPrice() + "?:";
                } else {
                    actionText += "Player may buy the estate.";
                }
            } else if (buttons.size() > 0) {
                if (player.getPlayerId() == selfPlayerId) {
                    actionText += "Choose an action:";
                } else {
                    actionText += "Player may choose an action.";
                }
            } else if (player.isInDebt()) {
                if (player.getPlayerId() == selfPlayerId) {
                    actionText += "You are in debt. You must pay it off by mortgaging properties or selling assets.";
                } else {
                    actionText += "Player is in debt.";
                }
            }
            
            return actionText;
        } else if (status == GameStatus.END) {
            return "The game has ended.";
        } else {
            return "<font color=\"red\">An error occurred.</font>";
        }
    }

    @Override
    public ArrayList<Button> getActionButtons(final int turnPlayerId) {
        if (status == GameStatus.RUN) {
            Player player = players.get(turnPlayerId);
            
            if (player.canRoll() && turnPlayerId == selfPlayerId) {
                buttons.clear();
                buttons.add(new Button("Roll", true, ".r"));
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                if (prefs.getBoolean("gameboard_autoroll", false)) {
                    if (rollTimeout == null) {
                        rollTimeout = new Timer();
                        rollTimeout.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                buttons.clear();
                                rollTimeout = null;
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        writeMessage("Automatically rolling the dice...", Color.YELLOW);
                                    }
                                });
                                sendToNetThread(BoardNetworkAction.MSG_ROLL, null);
                            }
                        }, autoRollTimeout);
                    }
                }
            } else {
                if (rollTimeout != null) {
                    rollTimeout.cancel();
                    rollTimeout = null;
                }
            }
            
            return buttons;
        } else {
            ArrayList<Button> buttons = new ArrayList<Button>(1);
            buttons.add(new Button("New game", true, null));
            return buttons;
        }
    }

    @Override
    public String getPlayerOverlayText(int playerId) {
        Player player = players.get(playerId);
        if (player == null) {
            return "<i>This player's information is no longer available.</i>";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(makePlayerName(player));
        sb.append("<br>");
        boolean inGame = false; 
        for (int aPlayerId : playerIds) {
            if (aPlayerId == playerId) {
                inGame = true;
            }
        }
        //sb.append("Player ID: " + player.getPlayerId() + "\n");
        if (!inGame) {
            sb.append("<i><font color=\"gray\">This player is not in the game.</font></i><br>");
        }
        if (player.isMaster() && status == GameStatus.CONFIG) {
            sb.append("<i><font color=\"yellow\">This player created this game.</font></i><br>");
        }
        if (player.isTurn()) {
            sb.append("<i><font color=\"yellow\">It is this player's turn.</font></i><br>");
        }
        if (player.getHost() != null) {
            sb.append(makeFieldLine("IP address", 
                    player.getHost()));
        }
        if (status == GameStatus.RUN) {
            BoardViewPiece piece = BoardViewPiece.getPiece(playerId);
            if (piece != null) {
                sb.append(makeFieldLine("Color",  piece.getColorName()));
                Estate estate = estates.get(piece.getCurrentEstate());
                if (estate != null) {
                    sb.append(makeFieldLine("On estate", 
                        makeEstateName(estate)));
                }
            }
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
            sb.append(makeFieldLine("Owned monopolies", 
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
                sb.append(makeFieldLine("Group", escapeHtml(group.getName())));
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
                                "$" + utilCost + " &times; DICE ROLL TOTAL"));
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
        for (int playerId : playerIds) {
            if (playerId >= 0) {
                Player player = players.get(playerId);
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
        Auction auction = auctions.get(auctionId);
        if (auction == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        Player auPlayer = players.get(auction.getActorId());
        Estate estate = estates.get(auction.getEstateId());
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
        sb.append(makePlayerName(auPlayer))
        .append(" is auctioning off ")
        .append(makeEstateName(estate))
        .append("<br>");
        if (auction.getHighBidder() == 0) {
            sb.append(makeFieldLine("Current highest bid",
                    "<i>no bids yet</i>"));
        } else {
            Player bidPlayer = players.get(auction.getHighBidder());
            sb.append(makeFieldLine("Current highest bid",
                    "$" + auction.getHighBid() + " by " + makePlayerName(bidPlayer)));
        }
        sb.append(makeFieldLine("Number of bids",
                Integer.toString(auction.getNumberOfBids())));
        String estateOverlay = getEstateOverlayText(auction.getEstateId());
        if (estateOverlay != null) {
            sb.append("<br><b>Estate being auctioned off</b>:<br>");
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
            sb.append("<font color=\"green\"><b>ACTIVE</b></font> trade with ");
            break;
        case REJECTED:
            sb.append("<font color=\"red\"><b>CANCELLED</b></font> trade with ");
            break;
        case COMPLETED:
            sb.append("<font color=\"gray\"><b>COMPLETED</b></font> trade with ");
            break;
        }
        sb.append(tradePlayers.size())
        .append(" participant");
        if (tradePlayers.size() != 1) { sb.append('s'); }
        sb.append(" and ")
        .append(offers.size())
        .append(" proposed trade");
        if (offers.size() != 1) { sb.append('s'); }
        sb.append("<br>");
        sb.append("<br>");
        sb.append(makeFieldLine("Trade revision",
                Integer.toString(trade.getRevision())));
        sb.append("<br>");
        sb.append("<b>Current participants</b>:<br>");
        if (tradePlayers.size() == 0) {
            sb.append("<i>no participants</i>");
        } else {
            for (TradePlayer player : tradePlayers) {
                sb.append(makePlayerName(players.get(player.getPlayerId())))
                .append(" has <b>");
                if (player.hasAccepted()) {
                    sb.append("<font color=\"green\">accepted</font>");
                } else {
                    sb.append("<font color=\"#ff8800\">not yet accepted</font>");
                }
                sb.append("</b> this trade.<br>");
            }
        }
        sb.append("<br>");
        sb.append("<b>Current trade proposals</b>:<br>");
        if (offers.size() == 0) {
            sb.append("<i>no proposals yet</i>");
        } else {
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
        }
        return sb.toString();
    }
    
    @Override
    public ArrayList<Estate> getEstates() {
        return estates;
    }

    @Override
    public int getSelfPlayerId() {
        return selfPlayerId;
    }

    @Override
    public ArrayList<OverlayButton> getPlayerOverlayButtons(final int overlayPlayerId) {
        ArrayList<OverlayButton> buttons = new ArrayList<OverlayButton>(4);
        boolean inGame = false;
        if (overlayPlayerId < 0) {
            return buttons;
        } else {
            for (int playerId : playerIds) {
                if (playerId == overlayPlayerId) {
                    inGame = true;
                }
            }
        }
        boolean pingable = inGame && players.get(overlayPlayerId) != null;
        if (status == GameStatus.CONFIG) {
            boolean kickable = pingable && isMaster &&
                    overlayPlayerId != selfPlayerId;
            buttons.add(new OverlayButton(
                    "Kick", kickable, 3,
                    new GestureRegionListener() {
                        @Override
                        public void onGestureRegionClick(GestureRegion gestureRegion) {
                            Bundle args = new Bundle();
                            args.putInt("playerId", overlayPlayerId);
                            sendToNetThread(BoardNetworkAction.MSG_GAME_KICK, args);
                        }
                    }));
        } else {
            boolean tradable = pingable && status == GameStatus.RUN &&
                    overlayPlayerId != selfPlayerId;
            buttons.add(new OverlayButton(
                    "Trade", tradable, 3,
                    new GestureRegionListener() {
                        @Override
                        public void onGestureRegionClick(GestureRegion gestureRegion) {
                            Bundle args = new Bundle();
                            args.putInt("playerId", overlayPlayerId);
                            sendToNetThread(BoardNetworkAction.MSG_TRADE_NEW, args);
                        }
                    }));
        }
        buttons.add(new OverlayButton(
                "!ping", pingable, 1,
                new GestureRegionListener() {
                    @Override
                    public void onGestureRegionClick(GestureRegion gestureRegion) {
                        Player player = players.get(overlayPlayerId);
                        if (player != null) {
                            sendCommand("!ping " + player.getName());
                        }
                    }
                }));
        buttons.add(new OverlayButton(
                "!date", pingable, 1,
                new GestureRegionListener() {
                    @Override
                    public void onGestureRegionClick(GestureRegion gestureRegion) {
                        Player player = players.get(overlayPlayerId);
                        if (player != null) {
                            sendCommand("!date " + player.getName());
                        }
                    }
                }));
        buttons.add(new OverlayButton(
                "!ver", pingable, 1,
                new GestureRegionListener() {
                    @Override
                    public void onGestureRegionClick(GestureRegion gestureRegion) {
                        Player player = players.get(overlayPlayerId);
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
            boolean isOwner = estate.getOwner() == selfPlayerId;
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
        final Auction auction = auctions.get(auctionId);
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
                                Bundle args = new Bundle();
                                args.putInt("dialogType", R.id.dialog_type_prompt_money);
                                args.putString("title", getString(R.string.dialog_enter_bid));
                                args.putString("message", String.format(getString(R.string.enter_custom_bid_amount), auction.getHighBid()));
                                args.putInt("default", auction.getHighBid() + 1);
                                args.putInt("min", auction.getHighBid() + 1);
                                dialog = MonopolyDialog.showNewDialog(BoardActivity.this, args);
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
        ArrayList<OverlayButton> buttons = new ArrayList<OverlayButton>(3);
        boolean isActive = trade.getLastUpdateType() != TradeUpdateType.COMPLETED &&
                trade.getLastUpdateType() != TradeUpdateType.REJECTED;
        buttons.add(new OverlayButton(
                "Add proposal", isActive, 2,
                new GestureRegionListener() {
                    @Override
                    public void onGestureRegionClick(GestureRegion gestureRegion) {
                        Bundle args = new Bundle();
                        args.putInt("tradeId", tradeId);
                        tradeAddOfferPrompt(TradeOfferStep.TYPE, args);
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
        return buttons;
    }

    private void tradeAddOfferPrompt(TradeOfferStep step, Bundle finalTrade) {
        Log.v("monopd", "tradeAddOfferPrompt step=" + step.toString() + " vlaues={" + finalTrade + " args}");

        TradeOfferType type = TradeOfferType.fromIndex(finalTrade.getInt("offerType"));
        Bundle args = new Bundle();
        switch (step) {
        case TYPE:
            args.putInt("dialogType", R.id.dialog_type_prompt_tradetype);
            args.putString("title", getString(R.string.dialog_choose_tradetype));
            args.putString("message", getString(R.string.choose_tradetype));
            args.putBoolean("isTrade", true);
            args.putInt("offerStep", step.getIndex());
            args.putInt("tradeId", finalTrade.getInt("tradeId"));
            dialog = MonopolyDialog.showNewDialog(this, args);
            break;
        case FROM:
            if (type == TradeOfferType.MONEY) {
                args.putInt("dialogType", R.id.dialog_type_prompt_objectlist);
                args.putString("title", getString(R.string.dialog_choose_tradeplayerfrom));
                args.putString("message", getString(R.string.empty_dialog_prompt_objectlist_player));
                args.putBoolean("isTrade", true);
                args.putInt("offerType", type.getIndex());
                args.putInt("offerStep", step.getIndex());
                args.putInt("tradeId", finalTrade.getInt("tradeId"));
                serializePlayers(args, -1, type);
                dialog = MonopolyDialog.showNewDialog(this, args);
            } else {
                tradeAddOfferPrompt(TradeOfferStep.VALUE, finalTrade);
            }
            break;
        case VALUE:
            switch (type) {
            default:
            case MONEY:
                args.putInt("dialogType", R.id.dialog_type_prompt_money);
                args.putString("title", getString(R.string.dialog_choose_trademoney));
                args.putString("message", getString(R.string.choose_trademoney));
                args.putInt("default", 1);
                args.putInt("min", 1);
                args.putInt("max", players.get(finalTrade.getInt("playerIdFrom")).getMoney());
                args.putBoolean("isTrade", true);
                args.putInt("offerType", type.getIndex());
                args.putInt("offerStep", step.getIndex());
                args.putInt("tradeId", finalTrade.getInt("tradeId"));
                args.putInt("playerIdFrom", finalTrade.getInt("playerIdFrom"));
                dialog = MonopolyDialog.showNewDialog(this, args);
                break;
            case ESTATE:
                args.putInt("dialogType", R.id.dialog_type_prompt_objectlist);
                args.putString("title", getString(R.string.dialog_choose_tradeestate));
                args.putString("message", getString(R.string.empty_dialog_prompt_objectlist_estate));
                serializeOwnedEstates(args);
                if (args.getInt("itemCount") == 1) {
                    // only one estate, use it now and skip dialog!
                    int estateId = args.getInt("itemId_0");
                    Estate estate = estates.get(estateId);
                    if (estate != null) {
                        finalTrade.putInt("playerIdFrom", estate.getOwner());
                    }
                    finalTrade.putInt("estateId", estateId);
                    tradeAddOfferPrompt(TradeOfferStep.TO, finalTrade);
                } else {
                    args.putBoolean("isTrade", true);
                    args.putInt("offerType", type.getIndex());
                    args.putInt("offerStep", step.getIndex());
                    args.putInt("tradeId", finalTrade.getInt("tradeId"));
                    dialog = MonopolyDialog.showNewDialog(this, args);
                }
                break;
            case CARD:
                args.putInt("dialogType", R.id.dialog_type_prompt_objectlist);
                args.putString("title", getString(R.string.dialog_choose_tradecard));
                args.putString("message", getString(R.string.empty_dialog_prompt_objectlist_card));
                args.putInt("itemCount", 0);
                args.putString("itemType", "CARD");
                serializeOwnedCards(args);
                if (args.getInt("itemCount") == 1) {
                    // only one card, use it now and skip dialog!
                    int cardId = args.getInt("itemId_0");
                    Card card = cards.get(cardId);
                    if (card != null) {
                        finalTrade.putInt("playerIdFrom", card.getOwner());
                    }
                    finalTrade.putInt("cardId", cardId);
                    tradeAddOfferPrompt(TradeOfferStep.TO, finalTrade);
                } else {
                    args.putBoolean("isTrade", true);
                    args.putInt("offerType", type.getIndex());
                    args.putInt("offerStep", step.getIndex());
                    args.putInt("tradeId", finalTrade.getInt("tradeId"));
                    dialog = MonopolyDialog.showNewDialog(this, args);
                }
                break;
            }
            break;
        case TO:
            args.putInt("dialogType", R.id.dialog_type_prompt_objectlist);
            args.putString("title", getString(R.string.dialog_choose_tradeplayerto));
            args.putString("message", getString(R.string.empty_dialog_prompt_objectlist_player));
            serializePlayers(args, finalTrade.getInt("playerIdFrom", -1), type);
            if (args.getInt("itemCount") == 1) {
                // only one possible recipient, use it now and skip dialog!
                int playerId = args.getInt("itemId_0");
                finalTrade.putInt("playerIdTo", playerId);
                tradeAddOfferPrompt(TradeOfferStep.COMPLETE, finalTrade);
            } else {
                args.putBoolean("isTrade", true);
                args.putInt("offerType", type.getIndex());
                args.putInt("offerStep", step.getIndex());
                args.putInt("tradeId", finalTrade.getInt("tradeId"));
                args.putInt("playerIdFrom", finalTrade.getInt("playerIdFrom"));
                args.putInt("amount", finalTrade.getInt("amount"));
                args.putInt("estateId", finalTrade.getInt("estateId"));
                args.putInt("cardId", finalTrade.getInt("cardId"));
                dialog = MonopolyDialog.showNewDialog(this, args);
            }
            break;
        case COMPLETE:
            switch (type) {
            default:
            case MONEY:
                sendToNetThread(BoardNetworkAction.MSG_TRADE_MONEY, finalTrade);
                break;
            case ESTATE:
                sendToNetThread(BoardNetworkAction.MSG_TRADE_ESTATE, finalTrade);
                break;
            case CARD:
                sendToNetThread(BoardNetworkAction.MSG_TRADE_CARD, finalTrade);
                break;
            }
            break;
        }
    }

    private void serializePlayers(Bundle args, int playerIdExclude, TradeOfferType type) {
        int count = 0;
        for (int i = 0; i < players.size(); i++) {
            int playerId = players.keyAt(i);
            Player player = players.get(playerId);
            if (playerIdExclude != player.getPlayerId() && player.getGameId() == gameItem.getGameId()) {
                args.putInt("itemId_" + count, playerId);
                args.putString("itemName_" + count, makePlayerName(player));
                switch (type) {
                default:
                case MONEY:
                    args.putString("itemSubtext_" + count, "$" + player.getMoney());
                    break;
                case ESTATE:
                    int ownedE = 0;
                    for (Estate est : estates) {
                        if (est.getOwner() == playerId) {
                            ownedE++;
                        }
                    }
                    args.putString("itemSubtext_" + count, ownedE + " estates owned");
                    break;
                case CARD:
                    args.putString("itemSubtext_" + count, "0 cards owned");
                    break;
                }
                count++;
            }
        }
        args.putInt("itemCount", count);
        args.putString("itemType", "PLAYER");
    }

    private void serializeOwnedEstates(Bundle args) {
        int count = 0;
        for (int i = 0; i < estates.size(); i++) {
            Estate estate = estates.get(i);
            if (estate.getOwner() >= 0) {
                args.putInt("itemId_" + count, estate.getEstateId());
                args.putString("itemName_" + count, makeEstateName(estate));
                Player owner = players.get(estate.getOwner());
                if (owner != null) {
                    args.putString("itemSubtext_" + count, "Owned by " + makePlayerName(owner));
                }
                count++;
            }
        }
        args.putInt("itemCount", count);
        args.putString("itemType", "ESTATE");
    }

    private void serializeOwnedCards(Bundle args) {
        int count = 0;
        for (int i = 0; i < cards.size(); i++) {
            Card card = cards.get(i);
            if (card.getOwner() >= 0) {
                args.putInt("itemId_" + count, card.getCardId());
                args.putString("itemName_" + count, "<b>" + card.getTitle() + "</b>");
                Player owner = players.get(card.getOwner());
                if (owner != null) {
                    args.putString("itemSubtext_" + count, "Owned by " + makePlayerName(owner));
                }
                count++;
            }
        }
        args.putInt("itemCount", count);
        args.putString("itemType", "CARD");
    }

    @Override
    public void onException(final String description, final Exception ex) {
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
    public void onClient(int selfPlayerId, String cookie) {
        Log.v("monopd", "net: Received onClient() from MonoProtocolHandler");
        this.selfPlayerId = selfPlayerId;
        this.cookie = cookie;
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
                            if (player.getPlayerId() == selfPlayerId) {
                                nickname = value;
                            }
                        }
                        if (key.equals("master")) {
                            changingMaster = true;
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
                // find in player list
                for (int playerId : playerIds) {
                    if (playerId == updatePlayerId) {
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
                                if (playerIds[i] < 0) {
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
                for (int playerId : playerIds) {
                    if (playerId >= 0 && players.get(playerId).isTurn()) {
                        boardView.drawActionRegions(playerId);
                        break;
                    }
                }
                // redraw overlay for possible new data
                boardView.redrawOverlay();
                // join message
                if (isJoin) {
                    // is in-game list?
                    if (status == GameStatus.CONFIG) {
                        writeMessage(makePlayerName(player) + " joined the game.", Color.GRAY, BoardViewOverlay.PLAYER, updatePlayerId);
                    } else {
                        writeMessage(makePlayerName(player) + " is in the game.", Color.GRAY, BoardViewOverlay.PLAYER, updatePlayerId);
                    }
                } else if (oldNick != null) {
                    // nick changed!
                    writeMessage(oldNick + " is now known as " + makePlayerName(player) + ".", Color.GRAY, BoardViewOverlay.PLAYER, updatePlayerId);
                }
                if (changingLocation) {
                    animateMove(updatePlayerId);
                }
                if (changingMaster) {
                    if (player.getPlayerId() == selfPlayerId && player.isMaster() != isMaster) {
                        isMaster = player.isMaster();
                        if (player.isMaster()) {
                            master = player.getPlayerId();
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
                Player player = players.get(playerId);
                if (status == GameStatus.RUN || status == GameStatus.END) {
                    if (player != null) {
                        player.setGrayed(true);
                    }
                } else {
                    boolean deleted = false;
                    for (int i = 0; i < BoardViewPiece.MAX_PLAYERS; i++) {
                        if (playerIds[i] == playerId) {
                            deleted = true;
                        }
                        if (deleted) {
                            if (i < 3) {
                                playerIds[i] = playerIds[i + 1];
                            } else {
                                playerIds[i] = -1;
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
                if (player != null) {
                    if (status == GameStatus.CONFIG && player.getGameId() == gameItem.getGameId()) {
                        if (master == playerId) {
                            master = -1;
                        }
                        writeMessage(makePlayerName(player) + " left the game.", Color.GRAY, BoardViewOverlay.PLAYER, playerId);
                    }
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
                for (int playerId : playerIds) {
                    if (playerId >= 0 && players.get(playerId).isTurn()) {
                        boardView.drawActionRegions(playerId);
                        break;
                    }
                }
                boardView.drawPieces(estates, playerIds, players);
                boardView.redrawOverlay();
            }
        });
    }

    @Override
    public void onGameUpdate(final int gameId, final String newStatus) {
        Log.v("monopd", "net: Received onGameUpdate() from MonoProtocolHandler");
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (gameId > 0) {
                    gameItem.setGameId(gameId);
                    setTitle(getFullTitle());
                }
                
                GameStatus oldStatus = status;
                status = GameStatus.fromString(newStatus);
                boolean statusChanged = (status != oldStatus);
                boardView.setStatus(status);
                
                switch (status) {
                case CONFIG:
                    if (statusChanged) {
                        writeMessage("Entering the game lobby...", Color.YELLOW);
                    }
                    clearCookie();
                    break;
                case INIT:
                    if (statusChanged) {
                        writeMessage("The game is starting...", Color.YELLOW);
                    }
                    saveCookie();
                    break;
                case RUN:
                    int playerCount = initPlayerColors();
                    if (statusChanged) {
                        writeMessage("The game started with " + playerCount + " players.", Color.YELLOW);
                    }
                    break;
                case END:
                    if (statusChanged) {
                        writeMessage("The game has ended.", Color.YELLOW);
                    }
                    clearCookie();
                    break;
                default:
                    break;
                }
                
                redrawRegions();
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
                    boardView.drawConfigRegions(configurables, isMaster);
                }
            }
        });                
    }

    @Override
    public void onConfigUpdate(final ArrayList<Configurable> configList) {
        Log.v("monopd", "net: Received onConfigUpdate() from MonoProtocolHandler");
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                //boolean fullList = false;
                for (Configurable toAdd : configList) {
                    configurables.put(toAdd.getConfigId(), toAdd);
                    //fullList = true;
                }
                if (boardView.isRunning()) {
                    //if (fullList) {
                    boardView.drawConfigRegions(configurables, isMaster);
                    //} else {
                    //    boardView.redrawConfigRegions(configurables, isMaster);
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
        Log.v("monopd", "net: Received onAuctionUpdate() from MonoProtocolHandler");
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                Auction auction = auctions.get(auctionId);
                if (auction == null) {
                    auction = new Auction(auctionId);
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
                    auction.setHighBid(0);
                    auction.setHighBidder(0);
                    auction.setNumberOfBids(0);
                    writeMessage("AUCTION: " + makePlayerName(players.get(auction.getActorId())) +
                            " is auctioning off " + makeEstateName(estates.get(auction.getEstateId())) +
                            ".", orange, BoardViewOverlay.AUCTION, auction.getAuctionId());
                } else {
                    switch (auction.getStatus()) {
                    case 0: // new bid
                        auction.setNumberOfBids(auction.getNumberOfBids() + 1);
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
                auctions.put(auctionId, auction);
                boardView.overlayAuctionInfo(auction.getAuctionId());
            }
        });
    }
    
    @Override
    public void onCardUpdate(final int cardId, final HashMap<String, String> data) {
        Log.v("monopd", "net: Received onCardUpdate() from MonoProtocolHandler");
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                Card card = cards.get(cardId);
                if (card == null) {
                    card = new Card(cardId);
                }
                for (String key : data.keySet()) {
                    String value = data.get(key);
                    XmlAttribute attr = Card.cardAttributes.get(key);
                    if (attr == null) {
                        Log.w("monopd", "card." + key + " was unknown. Value = " + value);
                    } else {
                        attr.set(card, value);
                    }
                }
                cards.put(cardId, card);
            }
        });
    }

    @Override
    public void onChatMessage(final int playerId, final String author, final String text) {
        Log.v("monopd", "net: Received onChatMessage() from MonoProtocolHandler");
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (author.length() > 0) {
                    
                    writeMessage(highlightPlayer(author) + ": " + escapeHtml(text), Color.WHITE, BoardViewOverlay.PLAYER, playerId);
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
                                sendCommand(clientName + " " + clientVersion + " on " + deviceName);
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
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                writeMessage("ERROR: " + escapeHtml(text), Color.RED);
            }
        });
    }

    @Override
    public void onInfoMessage(final String text) {
        Log.v("monopd", "net: Received onInfoMessage() from MonoProtocolHandler");
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                writeMessage("INFO: " + escapeHtml(text), Color.LTGRAY);
            }
        });
    }

    @Override
    public void onDisplayMessage(final int estateId, final String text, boolean clearText,
            final boolean clearButtons, final ArrayList<Button> newButtons) {
        Log.v("monopd", "net: Received onDisplayMessage() from MonoProtocolHandler");
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                boolean redrawButtons = false;
                if (clearButtons) {
                    buttons.clear();
                    if (rollTimeout != null) {
                        rollTimeout.cancel();
                        rollTimeout = null;
                    }
                    redrawButtons = true;
                }
                if (newButtons.size() > 0) {
                    buttons.addAll(newButtons);
                    redrawButtons = true;
                }
                if (redrawButtons) {
                    for (int playerId : playerIds ) {
                        if (playerId >= 0 && players.get(playerId).isTurn()) {
                            boardView.drawActionRegions(playerId);
                            break;
                        }
                    }
                }
                if (text == null || text.length() == 0) {
                    return;
                }
                writeMessage("GAME: " + escapeHtml(text), Color.CYAN, BoardViewOverlay.ESTATE, ((estateId == -1) ? -1 : estateId));
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
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    setPlayerView(newPlayerIds);
                }
            });
        } else if (type.equals("edit")) {
            // Log.d("monopd", "players: Edit " +
            // list.get(0).getNick());
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    updatePlayerView();
                }
            });
        } else if (type.equals("add")) {
            // Log.d("monopd", "players: Add " +
            // list.get(0).getNick());
            final int[] newPlayerIds = playerIds;
            for (int i = 0; i < list.size(); i++) {
                for (int j = 0; j < BoardViewPiece.MAX_PLAYERS; j++) {
                    final int playerId = list.get(i).getPlayerId();
                    if (newPlayerIds[j] == 0 || newPlayerIds[j] == playerId) {
                        runOnUiThread(new Runnable() {

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
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    setPlayerView(newPlayerIds);
                }
            });
        } else if (type.equals("del")) {
            //Log.d("monopd", "players: Delete "
            //        + players.get(list.get(0).getPlayerId()).getNick());
            final int[] newPlayerIds = playerIds;
            for (int i = 0; i < list.size(); i++) {
                boolean moveBack = false;
                for (int j = 0; j < BoardViewPiece.MAX_PLAYERS; j++) {
                    if (!moveBack && newPlayerIds[j] == list.get(i).getPlayerId()) {
                        final int playerIdClosure = newPlayerIds[j];
                        runOnUiThread(new Runnable() {

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
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    setPlayerView(newPlayerIds);
                }
            });
        } else {
            Log.w("monopd", "unrecognized playerlistupdate type: " + type + " " + list.get(0).getNick());
        }*/
    }

    @Override
    public void setHandler(Handler netHandler) {
        this.netHandler = netHandler;
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
                    setTitle(getFullTitle());
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
                    if (trade.getActorId() == selfPlayerId) {
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
                    if (trade.getActorId() == selfPlayerId) {
                        writeMessage("TRADE: You accept the trade.", Color.MAGENTA, BoardViewOverlay.TRADE, tradeId);
                    } else {
                        writeMessage("TRADE: " + makePlayerName(players.get(trade.getActorId())) + " accepts the trade.", Color.MAGENTA, BoardViewOverlay.TRADE, tradeId);
                    }
                    break;
                case COMPLETED:
                    writeMessage("TRADE: The trade has completed successfully.", Color.MAGENTA, BoardViewOverlay.TRADE, tradeId);
                    break;
                case REJECTED:
                    if (trade.getActorId() == selfPlayerId) {
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
        return cards.get(cardId).getOwner();
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_ENTER) {
            String text = chatSendBox.getText().toString();
            if (text.length() > 0) {
                sendCommand(text);
                chatSendBox.setText("");
            }
        }
        return false;
    }

    @Override
    public void onItemClick(AdapterView<?> listView, View itemView, int position, long id) {
        // chat item click
        ChatItem item = chatListAdapter.getItem(position);
        BoardViewOverlay overlayType = item.getOverlayType();
        if (item.getObjectId() < 0) {
            overlayType = BoardViewOverlay.NONE;
        }
        switch (overlayType) {
        default:
        case NONE:
            break;
        case PLAYER:
            boardView.overlayPlayerInfo(item.getObjectId());
            break;
        case ESTATE:
            boardView.overlayEstateInfo(item.getObjectId());
            break;
        case AUCTION:
            boardView.overlayAuctionInfo(item.getObjectId());
            break;
        case TRADE:
            boardView.overlayTradeInfo(item.getObjectId());
            break;
        }
    }

    @Override
    public void onDialogReconnect(Bundle dialogArgs) {
        sendToNetThread(BoardNetworkAction.MSG_SOCKET_RECONNECT, null);
    }
    
    @Override
    public void onDialogQuit(Bundle dialogArgs) {
        finish();
    }

    @Override
    public void onDialogConfirmQuit(Bundle dialogArgs) {
        sendToNetThread(BoardNetworkAction.MSG_GAME_QUIT, null);
        clearCookie();
        finish();
    }
    
    @Override
    public void onDialogEnterName(String value, Bundle dialogArgs) {
        BoardNetworkAction action = BoardNetworkAction.fromWhat(dialogArgs.getInt("action"));
        Bundle args = new Bundle();
        args.putString("name", value);
        sendToNetThread(action, args);
    }
    
    @Override
    public void onDialogEnterMoney(int value, Bundle dialogArgs) {
        if (dialogArgs.getBoolean("isTrade")) { // TradeOfferType.MONEY
            Bundle finalTrade = new Bundle();
            finalTrade.putInt("tradeId", dialogArgs.getInt("tradeId"));
            finalTrade.putInt("offerType", dialogArgs.getInt("offerType"));
            finalTrade.putInt("playerIdFrom", dialogArgs.getInt("playerIdFrom"));
            finalTrade.putInt("amount", value);
            tradeAddOfferPrompt(TradeOfferStep.TO, finalTrade);
        } else {
            int auctionId = dialogArgs.getInt("auctionId");
            Bundle args = new Bundle();
            args.putInt("auctionId", auctionId);
            args.putInt("bid", value);
            sendToNetThread(BoardNetworkAction.MSG_AUCTION_BID, args);
        }
    }
    
    @Override
    public void onDialogChooseTradeType(TradeOfferType offerType, Bundle dialogArgs) {
        // choose TradeOfferType
        Bundle finalTrade = new Bundle();
        finalTrade.putInt("tradeId", dialogArgs.getInt("tradeId"));
        finalTrade.putInt("offerType", offerType.getIndex());
        tradeAddOfferPrompt(TradeOfferStep.FROM, finalTrade);
    }
    
    @Override
    public void onDialogChooseItem(int objectId, Bundle dialogArgs) {
        TradeOfferType type = TradeOfferType.fromIndex(dialogArgs.getInt("offerType"));
        TradeOfferStep step = TradeOfferStep.fromIndex(dialogArgs.getInt("offerStep"));
        if (step == TradeOfferStep.FROM) { // TradeOfferType.MONEY
            Bundle finalTrade = new Bundle();
            finalTrade.putInt("tradeId", dialogArgs.getInt("tradeId"));
            finalTrade.putInt("offerType", type.getIndex());
            finalTrade.putInt("playerIdFrom", objectId);
            tradeAddOfferPrompt(TradeOfferStep.VALUE, finalTrade);
        } else if (step == TradeOfferStep.VALUE) {
            if (type == TradeOfferType.ESTATE) {
                Bundle finalTrade = new Bundle();
                finalTrade.putInt("tradeId", dialogArgs.getInt("tradeId"));
                finalTrade.putInt("offerType", type.getIndex());
                Estate estate = estates.get(objectId);
                if (estate != null) {
                    finalTrade.putInt("playerIdFrom", estate.getOwner());
                }
                finalTrade.putInt("estateId", objectId);
                tradeAddOfferPrompt(TradeOfferStep.TO, finalTrade);
            } else if (type == TradeOfferType.CARD) {
                Bundle finalTrade = new Bundle();
                finalTrade.putInt("tradeId", dialogArgs.getInt("tradeId"));
                finalTrade.putInt("offerType", type.getIndex());
                Card card = cards.get(objectId);
                if (card != null) {
                    finalTrade.putInt("playerIdFrom", card.getOwner());
                }
                finalTrade.putInt("cardId", objectId);
                tradeAddOfferPrompt(TradeOfferStep.TO, finalTrade);
            }
        } else if (step == TradeOfferStep.TO) { // any TradeOfferType
            Bundle finalTrade = new Bundle();
            finalTrade.putInt("tradeId", dialogArgs.getInt("tradeId"));
            finalTrade.putInt("offerType", type.getIndex());
            finalTrade.putInt("playerIdFrom", dialogArgs.getInt("playerIdFrom"));
            finalTrade.putInt("amount", dialogArgs.getInt("amount"));
            finalTrade.putInt("estateId", dialogArgs.getInt("esateId"));
            finalTrade.putInt("cardId", dialogArgs.getInt("cardId"));
            finalTrade.putInt("playerIdTo", objectId);
            tradeAddOfferPrompt(TradeOfferStep.COMPLETE, finalTrade);
        }
    }

    @Override
    public void onDialogDismiss() {
        dialog = null;
    }
}
