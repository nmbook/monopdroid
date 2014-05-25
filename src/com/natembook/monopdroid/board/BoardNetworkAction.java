package com.natembook.monopdroid.board;

/**
 * Represents types of messages that can be sent to the network thread from the activity.
 * Many of these are client commands for the MONOPD server.
 * 
 * To properly send a command, you must give the Handler the result of {@link #getWhat()} and 
 * a {@link android.os.Bundle} containing the arguments required by the type of message.
 * @author Nate
 *
 */
public enum BoardNetworkAction {
    /**
     * Internal use only.
     * Used when unrecognized "what" value.
     */
    MSG_UNKNOWN(0),
    /**
     * Sent to stop thread.
     */
    MSG_STOP(1),
    /**
     * Internal use only.
     * Sent by self to call doReceive().
     */
    MSG_RECV(2),
    /**
     * Sent to pause receive (activity restarting).
     */
    MSG_PAUSE(3),
    /**
     * Sent to resume receive (activity restarted).
     */
    MSG_RESUME(4),
    /**
     * Sent to reconnect to the monopd.
     */
    MSG_SOCKET_RECONNECT(5),
    /**
     * Send a command/chat message unmodified to the server.<br>
     * Argument String "text": The text to send.
     */
    MSG_COMMAND(10),
    /**
     * Send a new nick to change to.<br>
     * Argument String "name": The new nick to use.
     */
    MSG_NICK(11),
    /**
     * Send to change a configuration option.<br>
     * Argument String "command": The command to change the option.<br>
     * Argument String "value": The new value.
     */
    MSG_CONFIG(12),
    /**
     * Send to roll the dice.
     */
    MSG_ROLL(13),
    /**
     * Send when the animation of a piece has reached the given estate ID.<br>
     * Must be sent to avoid "lag".<br>
     * Argument int "estateId": The estate reached by the piece. 
     */
    MSG_TURN(14),
    /**
     * Send to reconnect to the game.<br>
     * Argument String "cookie": Your connection cookie.
     */
    MSG_RECONNECT(15),
    /**
     * Send to declare bankrupcy.
     */
    MSG_DECLARE_BANKRUPCY(16),
    /**
     * Send to ask the server to send the full game state to us.
     */
    MSG_SERVER_FORCE_REFRESH(17),
    /**
     * Send to start the game.
     */
    MSG_GAME_START(20),
    /**
     * Send to quit the game.
     */
    MSG_GAME_QUIT(21),
    /**
     * Send to kick a player from the game.<br>
     * Argument int "playerId": The player to kick. 
     */
    MSG_GAME_KICK(22),
    /**
     * Send to set the game description.<br>
     * Argument String "name": The new description. 
     */
    MSG_GAME_DESCRIPTION(23),
    /**
     * Send when the user presses a server-sent button.<br>
     * Argument String "command": The command to send.
     */
    MSG_BUTTON_COMMAND(30),
    /**
     * Send to toggle mortgage on the specified estate.<br>
     * Argument int "estateId": The estate.
     */
    MSG_ESTATE_MORTGAGE(40),
    /**
     * Send to buy a house on the specified estate.<br>
     * Argument int "estateId": The estate.
     */
    MSG_ESTATE_BUYHOUSE(41),
    /**
     * Send to sell a house on the specified estate.<br>
     * Argument int "estateId": The estate.
     */
    MSG_ESTATE_SELLHOUSE(42),
    /**
     * Send to sell an estate back to the bank.<br>
     * Argument int "estateId": The estate.
     */
    MSG_ESTATE_SELL(43),
    /**
     * Send to bid in the auction of an estate.<br>
     * Argument int "auctionId": The auction (always 0).<br>
     * Argument int "bid": The amount to bid.
     */
    MSG_AUCTION_BID(50),
    /**
     * Send to begin a trade with a player.<br>
     * Argument int "playerId": The other player in the trade.
     */
    MSG_TRADE_NEW(60),
    /**
     * Send to reject a trade with a player.<br>
     * Argument int "tradeId": The trade ID.
     */
    MSG_TRADE_REJECT(61),
    /**
     * Send to accept a trade with a player.<br>
     * Argument int "tradeId": The trade ID.<br>
     * Argument int "revision": The current revision, as seen by this client.
     */
    MSG_TRADE_ACCEPT(62),
    /**
     * Send to suggest a money amount to trade.<br>
     * Argument int "tradeId": The trade ID.<br>
     * Argument int "playerIdFrom": The player to send the money.<br>
     * Argument int "playerIdTo": The player to receive the money.<br>
     * Argument int "amount": The amount of money to offer (set to 0 to cancel).
     */
    MSG_TRADE_MONEY(63),
    /**
     * Send to suggest an estate to trade.<br.
     * Argument int "tradeId": The trade ID.<br>
     * Argument int "playerIdTo": The player to receive the estate (set to current owner to cancel).<br>
     * Argument int "estateId": The estate to send.
     */
    MSG_TRADE_ESTATE(64),
    /**
     * Send to suggest a card to trade.<br>
     * Argument int "tradeId": The trade ID.<br>
     * Argument int "playerIdTo": The player to receive the card (set to current owner to cancel).<br>
     * Argument int "cardId": The card to send.
     */
    MSG_TRADE_CARD(65);
    
    private int msgWhat;
    
    private BoardNetworkAction(int msgWhat) {
        this.msgWhat = msgWhat;
    }
    
    /**
     * The value to provide to the {@link BoardActivityNetworkThread} 
     * @return The "what" value.
     */
    public int getWhat() {
        return this.msgWhat;
    }
    
    public static BoardNetworkAction fromWhat(int what) {
        for (BoardNetworkAction action : BoardNetworkAction.values()) {
            if (action.getWhat() == what) {
                return action;
            }
        }
        return BoardNetworkAction.MSG_UNKNOWN;
    }
}
