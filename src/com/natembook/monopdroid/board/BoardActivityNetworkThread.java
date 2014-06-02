package com.natembook.monopdroid.board;

import com.natembook.monopdroid.gamelist.GameItem;
import com.natembook.monopdroid.monopd.MonoProtocolGameListener;
import com.natembook.monopdroid.monopd.MonoProtocolHandler;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

/**
 * A thread for handling the network.
 * It should stay running while the Activity may restart.
 * @author Nate
 *
 */
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
    /**
     * The event listener that the protocol handler will use to talk to the activity. 
     */
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
                //Log.v("monopd", "net: Current msg = " + action.toString() + " Current thread = " + Thread.currentThread().getName());
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
                        monopd.sendGameJoin(gameId);
                        break;
                    case INIT:
                    case RUN:
                    case END:
                        monopd.sendReconnect(cookie);
                        break;
                    default:
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
                            rState.getString("name"));
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
                case MSG_SERVER_FORCE_REFRESH:
                    monopd.sendForceRefresh();
                    break;
                case MSG_GAME_START:
                    monopd.sendGameStart();
                    break;
                case MSG_GAME_QUIT:
                    monopd.sendGameQuit();
                    break;
                case MSG_GAME_KICK:
                    monopd.sendGameKick(rState.getInt("playerId"));
                    break;
                case MSG_GAME_DESCRIPTION:
                    monopd.sendGameSetDescription(rState.getString("name"));
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
                case MSG_ESTATE_SELL:
                    monopd.sendSellEstate(
                            rState.getInt("estateId"));
                    break;
                case MSG_AUCTION_BID:
                    monopd.sendAuctionBid(
                            rState.getInt("auctionId"),
                            rState.getInt("bid"));
                    break;
                case MSG_TRADE_NEW:
                    monopd.sendTradeNew(
                            rState.getInt("playerId"));
                    break;
                case MSG_TRADE_REJECT:
                    monopd.sendTradeReject(
                            rState.getInt("tradeId"));
                    break;
                case MSG_TRADE_ACCEPT:
                    monopd.sendTradeAccept(
                            rState.getInt("tradeId"),
                            rState.getInt("revision"));
                    break;
                case MSG_TRADE_MONEY:
                    monopd.sendTradeMoney(
                            rState.getInt("tradeId"),
                            rState.getInt("playerIdFrom"),
                            rState.getInt("playerIdTo"),
                            rState.getInt("amount"));
                    break;
                case MSG_TRADE_ESTATE:
                    monopd.sendTradeEstate(
                            rState.getInt("tradeId"),
                            rState.getInt("estateId"),
                            rState.getInt("playerIdTo"));
                    break;
                case MSG_TRADE_CARD:
                    monopd.sendTradeCard(
                            rState.getInt("tradeId"),
                            rState.getInt("cardId"),
                            rState.getInt("playerIdTo"));
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
            monopd.sendGameJoin(gameId);
            break;
        case CREATE:
            monopd.sendClientHello();
            monopd.sendChangeNick(nick, false);
            monopd.sendGameCreate(type);
            break;
        case RECONNECT:
            monopd.sendReconnect(cookie);
            break;
        }
        // do not continuously receive so that we can get messages from
        // the ui thread to send
        Log.d("monopd", "net: Completed thread set-up");
        Log.v("monopd", "net: Current thread = " + Thread.currentThread().getName());
        netHandler.postDelayed(doDelayedReceive, 250);
        // await messages on network thread
        Looper.loop();
    }
}
