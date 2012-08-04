package edu.rochester.nbook.monopdroid;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import edu.rochester.nbook.monopdroid.BoardView.BoardViewListener;
import edu.rochester.nbook.monopdroid.BoardView.DrawState;

public class BoardActivity extends Activity {

    private static final int MSG_NICK = 2;
    private static final int MSG_STOP = 5;
    private static final int MSG_RECV = 6;
    private static final int MSG_COMMAND = 7;

    private enum GameStatus {
        ERROR, CONFIG, INIT, RUN;

        public static GameStatus fromString(String strStatus) {
            if (strStatus.equals("config")) {
                return CONFIG;
            } else if (strStatus.equals("init")) {
                return INIT;
            } else if (strStatus.equals("run")) {
                return RUN;
            } else {
                return ERROR;
            }
        }
    }

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
    private ArrayAdapter<String> chatListAdapter = null;
    /**
     * The chat send box. Do not access from networking thread.
     */
    private EditText chatSendBox = null;
    /**
     * The player views. Do not access from networking thread.
     */
    private LinearLayout[] playerView = new LinearLayout[4];
    /**
     * The networking thread (created in UI thread). Do not access from
     * networking thread.
     */
    private Thread netThread = null;

    // cross-thread objects
    /**
     * Used to send messages to networking thread.
     */
    private static volatile Handler netHandler = null;

    /**
     * This game item.
     */
    private volatile GameItem gameItem = null;
    /**
     * Whether we joined or created this game.
     */
    private volatile boolean isJoin = false;
    /**
     * Array of players to show in the 4 slots.
     */
    private volatile int[] playerIds = new int[4];
    /**
     * List of players.
     */
    private volatile SparseArray<Player> players = new SparseArray<Player>();
    /**
     * List of estates.
     */
    private volatile HashMap<String, Estate> estates = new HashMap<String, Estate>();
    /**
     * List of options.
     */
    private volatile List<Configurable> configurables = new ArrayList<Configurable>();
    /**
     * Current player ID.
     */
    private volatile int playerId = 0;
    /**
     * Current player cookie.
     */
    private volatile String cookie = null;
    /**
     * Game status.
     */
    private volatile GameStatus status = GameStatus.ERROR;
    /**
     * Client name.
     */
    private volatile String clientNameSetting;
    /**
     * Client version.
     */
    private volatile String versionSetting;
    /**
     * Current nick name.
     */
    private volatile String nickSetting;

    // network thread only
    /**
     * The monopd protocol handler.
     */
    private MonoProtocolHandler monopd = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d("monopd", "board: BoardActivity start");

        this.setContentView(R.layout.board);

        Intent i = this.getIntent();
        int game_id = i.getIntExtra("edu.rochester.nbook.game_id", 0);
        String host = i.getStringExtra("edu.rochester.nbook.host");
        int port = i.getIntExtra("edu.rochester.nbook.port", 0);
        String version = i.getStringExtra("edu.rochester.nbook.version");
        String type = i.getStringExtra("edu.rochester.nbook.type");
        String type_name = i.getStringExtra("edu.rochester.nbook.type_name");
        String descr = i.getStringExtra("edu.rochester.nbook.descr");
        int players = i.getIntExtra("edu.rochester.nbook.players", 0);
        boolean can_join = i.getBooleanExtra("edu.rochester.nbook.can_join", false);
        boolean is_join = i.getBooleanExtra("edu.rochester.nbook.act_is_join", false);
        if (!can_join || game_id < -1 || game_id == 0 || host == "" || port <= 0) {
            this.finish();
            return;
        }
        this.gameItem = new GameItem(game_id, host, port, version, type, type_name, descr, players, can_join);
        this.isJoin = is_join;
        SharedPreferences shPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        this.clientNameSetting = "monopdroid";
        try {
            this.versionSetting = this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName;
        } catch (NameNotFoundException e) {
            this.versionSetting = "0.0.0";
        }
        this.nickSetting = shPrefs.getString("player_nick", "anonymous");

        this.netThread = new Thread(new NetThreadStart());
        this.netThread.start();

        this.setTitle(String.format(this.getString(R.string.title_activity_board), descr));

        this.boardView = (BoardView) this.findViewById(R.id.board_ui);
        this.chatSendBox = (EditText) this.findViewById(R.id.chat_box);
        this.chatList = (ListView) this.findViewById(R.id.chat_contents);
        this.playerView[0] = (LinearLayout) this.findViewById(R.id.player_item1);
        this.playerView[1] = (LinearLayout) this.findViewById(R.id.player_item2);
        this.playerView[2] = (LinearLayout) this.findViewById(R.id.player_item3);
        this.playerView[3] = (LinearLayout) this.findViewById(R.id.player_item4);

        this.boardView.setBoardViewListener(new BoardViewListener() {
            @Override
            public void onConfigChange(String command, String value) {
                BoardActivity.this.sendCommand(command + value);
            }
            
            @Override
            public void onStartGame() {
                BoardActivity.this.sendCommand(".gs");
            }
        });

        if (is_join) {
            this.boardView.setState(DrawState.WAIT_JOIN);
        } else {
            this.boardView.setState(DrawState.WAIT_CREATE);
        }

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

        this.chatListAdapter = new ArrayAdapter<String>(this, R.layout.chat_item);
        this.chatList.setAdapter(this.chatListAdapter);

        Log.d("monopd", "board: Completed activity set-up");
    }
    
    private void sendCommand(String text) {
        Bundle state = new Bundle();
        state.putString("text", text);
        this.sendToNetThread(MSG_COMMAND, state);
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
    }

    @Override
    protected void onStop() {
        super.onStop();
        this.sendToNetThread(MSG_STOP, null);
    }

    /**
     * Sends a message to the network thread. Use this and not
     * nethandler.dispatchMessage()
     * 
     * @param what
     *            The message ID.
     * @param state
     *            State data. Can be null if it isn't going to be read.
     */
    private void sendToNetThread(int what, Bundle state) {
        Message msg = Message.obtain(netHandler, what);
        msg.setData(state);
        netHandler.dispatchMessage(msg);
    }

    private class NetThreadStart implements Runnable {
        private boolean continueReceive = true;

        @Override
        public void run() {
            Log.d("monopd", "net: Network thread start.");

            /**
             * An object to call to cause a monopd.doReceive() after 250ms.
             */
            final Runnable doDelayedReceive = new Runnable() {
                @Override
                public void run() {
                    if (NetThreadStart.this.continueReceive) {
                        BoardActivity.this.sendToNetThread(MSG_RECV, null);
                    }
                }
            };

            Looper.prepare();
            netHandler = new Handler() {
                /**
                 * Where UI-to-network messages are received and processed.
                 */
                @Override
                public void handleMessage(Message msg) {
                    Bundle rState = msg.getData();
                    switch (msg.what) {
                    // request to change nick by UI
                    case MSG_NICK:
                        Log.d("monopd", "net: Received MSG_NICK from BoardActivity");
                        String newNick = rState.getString("nick");
                        BoardActivity.this.monopd.sendChangeNick(newNick);
                        BoardActivity.this.nickSetting = newNick;
                        break;
                    // stop thread
                    case MSG_STOP:
                        Log.d("monopd", "net: Received MSG_STOP from BoardActivity");
                        NetThreadStart.this.continueReceive = false;
                        BoardActivity.this.monopd.disconnect();
                        netHandler.getLooper().quit();
                        break;
                    // receive message callback
                    case MSG_RECV:
                        Log.v("monopd", "net: Received MSG_RECV from BoardActivity");
                        if (NetThreadStart.this.continueReceive) {
                            // receive if there is data
                            BoardActivity.this.monopd.doReceive();
                            // the only place we message ourself:
                            // receive every 250ms
                            netHandler.postDelayed(doDelayedReceive, 250);
                        }
                        break;
                    // send message
                    case MSG_COMMAND:
                        Log.d("monopd", "net: Received MSG_COMMAND from BoardActivity");
                        String text = rState.getString("text");
                        BoardActivity.this.monopd.sendCommand(text);
                    }
                    msg.recycle();
                }
            };
            int gameId = BoardActivity.this.gameItem.getGameId();
            String host = BoardActivity.this.gameItem.getServer().getHost();
            int port = BoardActivity.this.gameItem.getServer().getPort();
            String type = BoardActivity.this.gameItem.getType();
            boolean is_join = BoardActivity.this.isJoin;
            String client = BoardActivity.this.clientNameSetting;
            String version = BoardActivity.this.versionSetting;
            String nick = BoardActivity.this.nickSetting;

            BoardActivity.this.monopd = new MonoProtocolHandler(new MonoProtocolGameListener() {
                @Override
                public void onException(String description, Exception ex) {
                    Log.v("monopd", "net: Received onException() from MonoProtocolHandler");
                    Bundle state = new Bundle();
                    state.putString("descr", description);
                    state.putString("ex", ex.getMessage());
                }

                @Override
                public void onClose() {
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
                public void onPlayerUpdate(final int playerId, String key, Object value) {
                    Log.v("monopd", "net: Received onPlayerUpdate() from MonoProtocolHandler");
                    final Player player = BoardActivity.this.players.get(playerId, new Player(playerId));
                    if (key.equals("name")) {
                        player.setNick(value.toString());
                    } else if (key.equals("host")) {
                        player.setHost(value.toString());
                    } else if (key.equals("master")) {
                        player.setMaster(!value.toString().equals("0"));
                    } else if (key.equals("money")) {
                        player.setMoney(Integer.parseInt(value.toString()));
                    } else if (key.equals("doublecount")) {
                        player.setDoubleCount(Integer.parseInt(value.toString()));
                    } else if (key.equals("jailcount")) {
                        player.setJailCount(Integer.parseInt(value.toString()));
                    } else if (key.equals("bankrupt")) {
                        player.setBankrupt(!value.toString().equals("0"));
                    } else if (key.equals("jailed")) {
                        player.setJailed(!value.toString().equals("0"));
                    } else if (key.equals("hasturn")) {
                        player.setHasTurn(!value.toString().equals("0"));
                    } else if (key.equals("spectator")) {
                        player.setSpectator(!value.toString().equals("0"));
                    } else if (key.equals("can_roll")) {
                        player.setCanRoll(!value.toString().equals("0"));
                    } else if (key.equals("canrollagain")) {
                        player.setCanRollAgain(!value.toString().equals("0"));
                    } else if (key.equals("can_buyestate")) {
                        player.setCanBuyEstate(!value.toString().equals("0"));
                    } else if (key.equals("canauction")) {
                        player.setCanAuction(!value.toString().equals("0"));
                    } else if (key.equals("hasdebt")) {
                        player.setHasDebt(!value.toString().equals("0"));
                    } else if (key.equals("canusecard")) {
                        player.setCanUseCard(!value.toString().equals("0"));
                    } else if (key.equals("location")) {
                        player.setLocation(Integer.parseInt(value.toString()));
                    } else if (key.equals("directmove")) {
                        player.setDirectMove(!value.toString().equals("0"));
                    } else if (key.equals("game")) {
                        // ignore
                    } else if (key.equals("cookie")) {
                        // ignore
                    } else if (key.equals("image")) {
                        // ignore
                    } else {
                        Log.w("monopd", "playerupdate with unknown attribute " + key);
                    }
                    BoardActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            BoardActivity.this.players.put(playerId, player);
                            updatePlayerView();
                        }
                    });
                }

                @Override
                public void onGameUpdate(int gameId, final String status) {
                    Log.v("monopd", "net: Received onGameUpdate() from MonoProtocolHandler");
                    BoardActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            BoardActivity.this.status = GameStatus.fromString(status);
                            switch (BoardActivity.this.status) {
                            case CONFIG:
                                BoardActivity.this.boardView.setState(DrawState.CONFIG);
                                break;
                            case INIT:
                                BoardActivity.this.boardView.setState(DrawState.INIT);
                                break;
                            case RUN:
                                BoardActivity.this.boardView.setState(DrawState.RUN);
                                break;
                            }
                        }
                    });
                }

                @Override
                public void onConfigUpdate(final List<Configurable> configList) {
                    Log.v("monopd", "net: Received onConfigUpdate() from MonoProtocolHandler");
                    nextItem: for (final Configurable toAdd : configList) {
                        for (int i = 0; i < BoardActivity.this.configurables.size(); i++) {
                            if (toAdd.getCommand().equals(BoardActivity.this.configurables.get(i).getCommand())) {
                                final int iClosure = i;
                                BoardActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        BoardActivity.this.configurables.set(iClosure, toAdd);
                                    }
                                });
                                continue nextItem;
                            }
                        }
                        BoardActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                BoardActivity.this.configurables.add(toAdd);
                            }
                        });
                    }
                    BoardActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            BoardActivity.this.boardView.setConfigurables(BoardActivity.this.configurables, players.get(playerId).isMaster());
                        }
                    });
                }

                @Override
                public void onChatMessage(final int playerId, final String author, final String text) {
                    Log.v("monopd", "net: Received onChatMessage() from MonoProtocolHandler");
                    BoardActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            writeMessage("<" + author + "> " + text, Color.WHITE, playerId, -1, false);
                        }
                    });
                }

                private void writeMessage(String msgText, int color, int playerId, int estateId, boolean clearButtons) {
                    BoardActivity.this.chatListAdapter.add(msgText);
                    BoardActivity.this.chatListAdapter.notifyDataSetChanged();
                }

                @Override
                public void onErrorMessage(final String text) {
                    Log.v("monopd", "net: Received onErrorMessage() from MonoProtocolHandler");
                    BoardActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            writeMessage("ERROR: " + text, Color.RED, -1, -1, false);
                        }
                    });
                }

                @Override
                public void onDisplayMessage(final int estateId, final String text, final boolean clearText,
                                final boolean clearButtons) {
                    Log.v("monopd", "net: Received onDisplayMessage() from MonoProtocolHandler");
                    BoardActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            writeMessage("GAME: " + text, Color.CYAN, -1, ((estateId == -1) ? 0 : estateId),
                                            clearButtons);
                        }
                    });
                }

                @Override
                public void onPlayerListUpdate(String type, List<Player> list) {
                    Log.v("monopd", "net: Received onPlayerListUpdate() from MonoProtocolHandler");
                    if (type.equals("full")) {
                        Log.d("monopd", "players: Full list update");
                        final int[] playerIds = new int[4];
                        for (int i = 0; i < list.size() && i < 4; i++) {
                            playerIds[i] = list.get(i).getPlayerId();
                            BoardActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    setPlayerView(playerIds);
                                }
                            });
                        }
                    } else if (type.equals("edit")) {
                        Log.d("monopd", "players: Edit " + list.get(0).getNick());
                    } else {
                        Log.w("monopd", "players: " + type + " " + list.get(0).getNick());
                    }
                }
            }, host, port, client, version);
            BoardActivity.this.monopd.sendClientHello();
            BoardActivity.this.monopd.sendChangeNick(nick, false);
            if (is_join) {
                BoardActivity.this.monopd.sendJoinGame(gameId);
            } else {
                BoardActivity.this.monopd.sendCreateGame(type);
            }
            // do not continuously receive so that we can get messages from
            // the ui thread to send
            BoardActivity.this.monopd.doReceive();
            Log.d("monopd", "postDelay result = " + netHandler.postDelayed(doDelayedReceive, 250));
            Log.d("monopd", "net: Completed thread set-up");
            // await messages on network thread
            Looper.loop();
        }
    }

    /**
     * Set the player list to show the specified 4 players. Player ID 0 means that slot is empty.
     * @param playerIds Player IDs of the players to show.
     */
    private void setPlayerView(int[] playerIds) {
        this.playerIds = playerIds;
        updatePlayerView();
    }

    /**
     * Updates the player view with new data from the player list.
     */
    private void updatePlayerView() {
        for (int i = 0; i < 4; i++) {
            if (playerIds[i] == 0) {
                playerView[i].setVisibility(LinearLayout.GONE);
            } else {
                Player player = this.players.get(this.playerIds[i]);
                if (player == null) {
                    playerView[i].setVisibility(LinearLayout.GONE);
                    Log.w("monopd", "players: Unknown player ID " + playerIds[i]);
                } else {
                    playerView[i].setVisibility(LinearLayout.VISIBLE);
                    TextView text1 = (TextView) playerView[i].findViewById(R.id.player_text_1);
                    text1.setText(player.getNick());
                    TextView text2 = (TextView) playerView[i].findViewById(R.id.player_text_2);
                    text2.setText("$" + player.getMoney());
                }
            }
        }
    }
}