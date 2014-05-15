package edu.rochester.nbook.monopdroid.monopd;

import java.util.ArrayList;
import java.util.HashMap;

import edu.rochester.nbook.monopdroid.board.Button;
import edu.rochester.nbook.monopdroid.board.Configurable;
import edu.rochester.nbook.monopdroid.board.Player;
import edu.rochester.nbook.monopdroid.gamelist.GameItem;
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

}