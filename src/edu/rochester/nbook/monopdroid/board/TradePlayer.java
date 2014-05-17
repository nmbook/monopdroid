package edu.rochester.nbook.monopdroid.board;

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
    
    public boolean isAccepted() {
        return accepted;
    }
}
