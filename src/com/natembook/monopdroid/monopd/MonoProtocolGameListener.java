package com.natembook.monopdroid.monopd;

import java.util.ArrayList;
import java.util.HashMap;

import com.natembook.monopdroid.board.Button;
import com.natembook.monopdroid.board.CardTradeOffer;
import com.natembook.monopdroid.board.Configurable;
import com.natembook.monopdroid.board.EstateTradeOffer;
import com.natembook.monopdroid.board.MoneyTradeOffer;
import com.natembook.monopdroid.board.Player;
import com.natembook.monopdroid.board.TradePlayer;
import com.natembook.monopdroid.gamelist.GameItem;

import android.os.Handler;

public interface MonoProtocolGameListener extends MonoProtocolListener {
    /**
     * Sent by the server when someone sends a chat message.
     * @param playerId The Player ID of the sender.
     * @param author The author's current nickname.
     * @param text The chat message.
     */
    public void onChatMessage(int playerId, String author, String text);

    /**
     * Sent by the server on error.
     * @param text Error message.
     */
    public void onErrorMessage(String text);

    /**
     * Sent by the server on info.
     * @param text Information message.
     */
    public void onInfoMessage(String text);

    /**
     * Sent by the server to display a prompt and some buttons about the current turn.
     * @param estateId The relevant Estate ID.
     * @param text The message to display below.
     * @param clearText Whether to clear the text before display.
     * @param clearButtons Whether to clear previous buttons.
     * @param newButtons New buttons to display.
     */
    public void onDisplayMessage(int estateId, String text, boolean clearText, boolean clearButtons, ArrayList<Button> newButtons);

    /**
     * Sent by the server to indicate its version.
     * @param version Server version string.
     */
    public void onServer(String version);

    /**
     * Sent by the server to identify this client.
     * @param playerId The Player ID to adopt.
     * @param cookie The reconnect cookie to adopt. 
     */
    public void onClient(int playerId, String cookie);

    /**
     * Sent by the server to update a Player object.
     * @param playerId Player ID.
     * @param data Key-Value set of object data.
     */
    public void onPlayerUpdate(int playerId, HashMap<String, String> data);

    /**
     * Sent by the server to change the game status (running, configuration, starting, ended...).
     * @param gameId The current game ID.
     * @param status The status to adopt.
     */
    public void onGameUpdate(int gameId, String status);

    /**
     * Sent by the server to update a Configurable object.
     * @param configId Configurable ID.
     * @param data Key-Value set of object data.
     */
    public void onConfigUpdate(int configId, HashMap<String, String> data);

    /**
     * Sent by the server to update a set of legacy Configurable objects.
     * @param configList The entire set of Configurables with all associated data.
     */
    public void onConfigUpdate(ArrayList<Configurable> configList);

    /**
     * Sent by the server to give a list of current players in this game.
     * @param type Type of update.
     * @param list The list of players.
     */
    public void onPlayerListUpdate(String type, ArrayList<Player> list);

    /**
     * Sent by the server to update an Estate object.
     * @param estateId Estate ID.
     * @param data Key-Value set of object data.
     */
    public void onEstateUpdate(int estateId, HashMap<String, String> data);

    /**
     * Call this to set the Handler object for callbacks.
     * @param netHandler
     */
    public void setHandler(Handler netHandler);

    /**
     * Sent by the server to describe a game item in the game list.
     * @param gameItem The game in the game list.
     */
    public void onGameItemUpdate(GameItem gameItem);

    /**
     * Sent by the server to delete a player from the player list.
     * @param playerId The Player ID.
     */
    public void onPlayerDelete(int playerId);

    /**
     * Sent by the server to update an Estate Group object.
     * @param estateGroupId Estate Group ID.
     * @param data Key-Value set of object data.
     */
    public void onEstateGroupUpdate(int estateGroupId, HashMap<String, String> data);

    /**
     * Sent by the server to update an Auction object.
     * @param auctionId Auction ID.
     * @param data Key-Value set of object data.
     */
    public void onAuctionUpdate(int auctionId, HashMap<String, String> data);
    
    /**
     * Sent by the server to update a Card object. 
     * @param cardId Card ID.
     * @param dataKey-Value set of object data.
     */
    public void onCardUpdate(int cardId, HashMap<String, String> data);

    /**
     * Sent by the server to update a Trade object.
     * @param tradeId Trade ID.
     * @param data Key-Value set of object data.
     */
    public void onTradeUpdate(int tradeId, HashMap<String, String> data);

    /**
     * Sent by the server to update a player in a trade.
     * @param tradeId The Trade ID.
     * @param player The new TradePlayer object.
     */
    public void onTradePlayer(int tradeId, TradePlayer player);

    /**
     * Sent by the server to update a money offer in a trade. 
     * @param tradeId The Trade ID.
     * @param offer The new MoneyTradeOffer object.
     */
    public void onTradeMoney(int tradeId, MoneyTradeOffer offer);

    /**
     * Sent by the server to update an estate offer in a trade. 
     * @param tradeId The Trade ID.
     * @param offer The new EstateTradeOffer object.
     */
    public void onTradeEstate(int tradeId, EstateTradeOffer offer);

    /**
     * Sent by the server to update a card offer in a trade. 
     * @param tradeId The Trade ID.
     * @param offer The new CardTradeOffer object.
     */
    public void onTradeCard(int tradeId, CardTradeOffer offer);

    /**
     * The XML parser will call this during a received trade update to create
     * a complete EstateTradeOffer object.
     * @param estateId The estate to look up.
     * @return The current owner of this estate.
     */
    public int getEstateOwner(int estateId);
    
    /**
     * The XML parser will call this during a received trade update to create
     * a complete CardTradeOffer object.
     * @param cardId The card to look up.
     * @return The current owner of this card.
     */
    public int getCardOwner(int cardId);

}
