package com.natembook.monopdroid.board;

/**
 * Represents a player involved in a trade.
 * @author Nate
 *
 */
public final class TradePlayer implements TradeUpdateSubject {
    // trade player object data
    private int playerId;
    private boolean accepted;
    
    public TradePlayer(int playerId, boolean accepted) {
        this.playerId = playerId;
        this.accepted = accepted;
    }
    
    public int getPlayerId() {
        return playerId;
    }
    
    public boolean hasAccepted() {
        return accepted;
    }
}
