package edu.rochester.nbook.monopdroid.board;

public final class TradePlayer implements TradeUpdateSubject {
    // trade player object data
    private int tradeId;
    private int playerId;
    private boolean accepted;
    
    public TradePlayer(int tradeId, int playerId, boolean accepted) {
        this.tradeId = tradeId;
        this.playerId = playerId;
        this.accepted = accepted;
    }
    
    @Override
    public int getTradeId() {
        return tradeId;
    }
    
    public int getPlayerId() {
        return playerId;
    }
    
    public boolean hasAccepted() {
        return accepted;
    }
}
