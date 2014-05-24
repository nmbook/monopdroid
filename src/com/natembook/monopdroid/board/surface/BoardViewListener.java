package com.natembook.monopdroid.board.surface;

import java.util.ArrayList;

import com.natembook.monopdroid.board.Estate;
import com.natembook.monopdroid.board.OverlayButton;

public interface BoardViewListener {
    /**
     * Sent from the BoardView when it is resized.
     * @param width The new width
     * @param height The new height
     */
    public void onResize(int width, int height);
    
    /**
     * Sent from the BoardView when a configurable is changed by the user.
     * @param command The command to use
     * @param value The value to adopt
     */
    public void onConfigChange(String command, String value);

    /**
     * Sent from the BoardView when the user clicks "start game".
     */
    public void onStartGame();

    /**
     * Sent from the BoardView when an Estate is tapped.
     * @param estateId The Estate ID
     */
    public void onEstateClick(int estateId);

    /**
     * Sent from the BoardView when the overlay is closed.
     */
    public void onCloseOverlay();

    /**
     * Sent from the BoardView when the user clicks "roll".
     */
    public void onRoll();

    /**
     * Sent from the BoardView when the user clicks a custom command button
     * @param command The command to send to the monopoly server
     */
    public void onButtonCommand(String command);

    /**
     * Generate the rich text to display in the Player overlay.
     * @param playerId The Player ID
     * @return An HTML string to be rendered in the Player overlay
     */
    public String getPlayerOverlayText(int playerId);

    /**
     * Generate the rich text to display in the Estate overlay.
     * @param estateId The Estate ID
     * @return An HTML string to be rendered in the Estate overlay
     */
    public String getEstateOverlayText(int estateId);

    /**
     * Generate the rich text to display in the Auction overlay.
     * @param auctionId The Auction ID
     * @return An HTML string to be rendered in the Auction overlay
     */
    public String getAuctionOverlayText(int auctionId);

    /**
     * Generate the rich text to display in the Trade overlay.
     * @param playerId The Player ID
     * @return An HTML string to be rendered in the Player overlay
     */
    public String getTradeOverlayText(int tradeId);

    /**
     * Generate the buttons for the Player overlay.
     * @param playerId The Player ID
     * @return A list of buttons
     */
    public ArrayList<OverlayButton> getPlayerOverlayButtons(int playerId);

    /**
     * Generate the buttons for the Estate overlay.
     * @param estateId The Estate ID
     * @return A list of buttons
     */
    public ArrayList<OverlayButton> getEstateOverlayButtons(int estateId);

    /**
     * Generate the buttons for the Auction overlay.
     * @param auctionId The Auction ID
     * @return A list of buttons
     */
    public ArrayList<OverlayButton> getAuctionOverlayButtons(int auctionId);

    /**
     * Generate the buttons for the Trade overlay.
     * @param tradeId The Trade ID
     * @return A list of buttons
     */
    public ArrayList<OverlayButton> getTradeOverlayButtons(int tradeId);

    /**
     * Get the list of Estate objects.
     * @return The list of estates
     */
    public ArrayList<Estate> getEstates();

    /**
     * Get the current player ID of this client.
     * @return The current player ID
     */
    public int getSelfPlayerId();
}
