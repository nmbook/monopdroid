package edu.rochester.nbook.monopdroid.board;

import edu.rochester.nbook.monopdroid.gamelist.GameItem;
import edu.rochester.nbook.monopdroid.monopd.MonoProtocolGameListener;
import edu.rochester.nbook.monopdroid.monopd.MonoProtocolHandler;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public class BoardActivityNetworkThread implements Runnable {
    /**
     * An object to call to cause a monopd.doReceive() after 250ms.
     */
    private final Runnable doDelayedReceive = new Runnable() {

        @Override
        public void run() {
            if (continueReceive) {
                act.sendToNetThread(BoardNetworkAction.MSG_RECV, null);
            }
        }
    };

    // cross-thread objects
    /**
     * Used to send messages to networking thread.
     */
    private static volatile Handler netHandler = null;
    
    /**
     * The activity to call back to. This may change!
     */
    private BoardActivity act;
    /**
     * Whether we can contiunue reading (still connected).
     */
    private boolean continueReceive = true;
    /**
     * The monopd protocol handler.
     */
    private MonoProtocolHandler monopd = null;
    
    private MonoProtocolGameListener listener;
    
    public BoardActivity getActivity() {
        return act;
    }
    
    public void setActivity(BoardActivity act, MonoProtocolGameListener listener) {
        this.act = act;
        this.listener = listener;
        this.listener.setHandler(netHandler);
        if (this.monopd != null) {
            this.monopd.setListener(listener);
        }
    }
    
    public Handler getHandler() {
        return netHandler;
    }

    @Override
    public void run() {
        Log.d("monopd", "net: Network thread start.");

        Looper.prepare();
        netHandler = new Handler() {
            /**
             * Where UI-to-network messages are received and processed.
             */
            @Override
            public void handleMessage(Message msg) {
                Bundle rState = msg.getData();
                BoardNetworkAction action = BoardNetworkAction.fromWhat(msg.what);
                Log.v("monopd", "net: Received message " + action.toString());
                switch (action) {
                // error
                case MSG_UNKNOWN:
                    Log.w("monopd", "net: Received unknown message with ID " + msg.what);
                    break;
                // stop thread
                case MSG_STOP:
                    continueReceive = false;
                    if (monopd != null) {
                        monopd.disconnect();
                    }
                    netHandler.getLooper().quit();
                    break;
                // receive message callback
                case MSG_RECV:
                    if (continueReceive) {
                        // receive if there is data
                        monopd.doReceive();
                        // the only place we message ourself:
                        // receive every 250ms
                        netHandler.postDelayed(doDelayedReceive, 250);
                    }
                    break;
                // pause recv
                case MSG_PAUSE:
                    continueReceive = false;
                    break;
                // resume recv
                case MSG_RESUME:
                    continueReceive = true;
                    netHandler.postDelayed(doDelayedReceive, 250);
                    break;
                case MSG_SOCKET_RECONNECT:
                    monopd.close();
                    GameItem gameItem = act.getGameItem();
                    int gameId = gameItem.getGameId();
                    String host = gameItem.getServer().getHost();
                    int port = gameItem.getServer().getPort();
                    String client = act.getClientName();
                    String version = act.getClientVersion();
                    String nick = act.getNickname();
                    String cookie = act.getSavedCookie();
                    monopd = new MonoProtocolHandler(listener, host, port, client, version);
                    switch (act.getGameStatus()) {
                    case ERROR:
                    case CREATE:
                    case JOIN:
                    case RECONNECT:
                    case CONFIG:
                        monopd.sendClientHello();
                        monopd.sendChangeNick(nick, false);
                        monopd.sendJoinGame(gameId);
                        break;
                    case INIT:
                    case RUN:
                        monopd.sendReconnect(cookie);
                        break;
                    }
                    break;
                // send message
                case MSG_COMMAND:
                    monopd.sendCommand(
                            rState.getString("text"));
                    break;
                // request to change nick
                case MSG_NICK:
                    monopd.sendChangeNick(
                            rState.getString("nick"));
                    break;
                case MSG_CONFIG:
                    monopd.sendChangeConfiguration(
                            rState.getString("command"),
                            rState.getString("value"));
                    break;
                case MSG_ROLL:
                    monopd.sendRoll();
                    break;
                case MSG_TURN:
                    monopd.sendTurnIndicator(
                            rState.getInt("estateId"));
                    break;
                case MSG_RECONNECT:
                    monopd.sendReconnect(
                            rState.getString("cookie"));
                    break;
                case MSG_DECLARE_BANKRUPCY:
                    monopd.sendDeclareBankrupcy();
                    break;
                case MSG_GAME_START:
                    monopd.sendStartGame();
                    break;
                case MSG_GAME_QUIT:
                    monopd.sendQuitGame();
                    break;
                case MSG_BUTTON_COMMAND:
                    monopd.sendButtonCommand(
                            rState.getString("command"));
                    break;
                case MSG_ESTATE_MORTGAGE:
                    monopd.sendToggleMortgage(
                            rState.getInt("estateId"));
                    break;
                case MSG_ESTATE_BUYHOUSE:
                    monopd.sendBuyHouse(
                            rState.getInt("estateId"));
                    break;
                case MSG_ESTATE_SELLHOUSE:
                    monopd.sendSellHouse(
                            rState.getInt("estateId"));
                    break;
                case MSG_AUCTION_BID:
                    monopd.sendAuctionBid(
                            rState.getInt("auctionId"),
                            rState.getInt("bid"));
                    break;
                }
                msg.recycle();
            }
        };
        
        listener.setHandler(netHandler);

        GameItem gameItem = act.getGameItem();
        int gameId = gameItem.getGameId();
        String host = gameItem.getServer().getHost();
        int port = gameItem.getServer().getPort();
        String type = gameItem.getType();
        String client = act.getClientName();
        String version = act.getClientVersion();
        String nick = act.getNickname();
        String cookie = act.getSavedCookie();

        monopd = new MonoProtocolHandler(listener, host, port, client, version);
        switch (gameItem.getItemType()) {
        default:
            break;
        case JOIN:
            monopd.sendClientHello();
            monopd.sendChangeNick(nick, false);
            monopd.sendJoinGame(gameId);
            break;
        case CREATE:
            monopd.sendClientHello();
            monopd.sendChangeNick(nick, false);
            monopd.sendCreateGame(type);
            break;
        case RECONNECT:
            monopd.sendReconnect(cookie);
            break;
        }
        // do not continuously receive so that we can get messages from
        // the ui thread to send
        monopd.doReceive();
        Log.d("monopd", "postDelay result = " + netHandler.postDelayed(doDelayedReceive, 250));
        Log.d("monopd", "net: Completed thread set-up");
        // await messages on network thread
        Looper.loop();
    }
}
