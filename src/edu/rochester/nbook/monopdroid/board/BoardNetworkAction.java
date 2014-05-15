package edu.rochester.nbook.monopdroid.board;

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
     * Send a command/chat message unmodified to the server.
     * Argument String "text": The text to send.
     */
    MSG_COMMAND(10),
    /**
     * Send a new nick to change to.
     * Argument String "nick": The nick to use.
     */
    MSG_NICK(11),
    /**
     * Send to change a configuration option.
     * Argument String "command": The command to change the option.
     * Argument String "value": The new value.
     */
    MSG_CONFIG(12),
    /**
     * Send to roll the dice.
     */
    MSG_ROLL(13),
    /**
     * Send when the animation of a piece has reached the given estate ID.
     * Must be sent to avoid "lag".
     * Argument int "estateId": The estate reached by the piece. 
     */
    MSG_TURN(14),
    /**
     * Send to reconnect to the game.
     * Argument String "cookie": Your connection cookie.
     */
    MSG_RECONNECT(15),
    /**
     * Send to declare bankrupcy.
     */
    MSG_DECLARE_BANKRUPCY(16),
    /**
     * Send to start the game.
     */
    MSG_GAME_START(20),
    /**
     * Send to quit the game.
     */
    MSG_GAME_QUIT(21),
    /**
     * Send when the user presses a server-sent button.
     * Argument String "command": The command to send.
     */
    MSG_BUTTON_COMMAND(30),
    /**
     * Send to toggle mortgage on the specified estate.
     * Argument int "estateId": The estate.
     */
    MSG_ESTATE_MORTGAGE(40),
    /**
     * Send to buy a house on the specified estate.
     * Argument int "estateId": The estate.
     */
    MSG_ESTATE_BUYHOUSE(41),
    /**
     * Send to sell a house on the specified estate.
     * Argument int "estateId": The estate.
     */
    MSG_ESTATE_SELLHOUSE(42),
    /**
     * Send to bid in the auction of an estate.
     * Argument int "auctionId": The auction (always 0).
     * Argument int "bid": The amount to bid.
     */
    MSG_AUCTION_BID(50);
    
    private int msgWhat;
    
    private BoardNetworkAction(int msgWhat) {
        this.msgWhat = msgWhat;
    }
    
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