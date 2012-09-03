package edu.rochester.nbook.monopdroid.board.surface;

import edu.rochester.nbook.monopdroid.board.Estate;
import edu.rochester.nbook.monopdroid.board.Player;

public interface BoardViewListener {
    public void onResize(int width, int height);
    
    public void onConfigChange(String command, String value);

    public void onStartGame();

    public void onEstateClick(int estateId);

    public void onCloseOverlay();

    public void onRoll();
    
    public void onBuyEstate();

    public void onAuctionEstate();
    
    public void onEndTurn();

    public void onOpenTradeWindow(Player player);
    
    public void onPlayerCommandPing(Player player);
    
    public void onPlayerCommandDate(Player player);
    
    public void onPlayerCommandVersion(Player player);

    public String getPlayerBodyText(Player player);

    public String getEstateBodyText(Estate estate);
}
