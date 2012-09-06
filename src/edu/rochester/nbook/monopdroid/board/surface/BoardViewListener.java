package edu.rochester.nbook.monopdroid.board.surface;

public interface BoardViewListener {
    public void onResize(int width, int height);
    
    public void onConfigChange(String command, String value);

    public void onStartGame();

    public void onEstateClick(int estateId);

    public void onCloseOverlay();

    public void onRoll();

    public void onOpenTradeWindow(int playerId);
    
    public void onPlayerCommandPing(int playerId);
    
    public void onPlayerCommandDate(int playerId);
    
    public void onPlayerCommandVersion(int playerId);

    public void onToggleMortgage(int estateId);

    public void onBuyHouse(int estateId);

    public void onSellHouse(int estateId);
    
    public void onBid(int auctionId, int raise);

    public String getPlayerBodyText(int playerId);

    public String getEstateBodyText(int estateId);

    public String getAuctionBodyText(int auctionId);

    public void onButtonCommand(String command);
}
