package edu.rochester.nbook.monopdroid.monopd;

import java.util.ArrayList;
import java.util.HashMap;

import edu.rochester.nbook.monopdroid.board.Button;
import edu.rochester.nbook.monopdroid.board.Configurable;
import edu.rochester.nbook.monopdroid.board.GameStatus;
import edu.rochester.nbook.monopdroid.board.Player;
import edu.rochester.nbook.monopdroid.gamelist.GameItem;
import android.os.Handler;

public interface MonoProtocolGameListener extends MonoProtocolListener {
    public void onChatMessage(int playerId, String author, String text);

    public void onErrorMessage(String text);

    public void onInfoMessage(String text);

    public void onDisplayMessage(int estateId, String text, boolean clearText, boolean clearButtons, ArrayList<Button> newButtons);

    public void onServer(String version);

    public void onClient(int playerId, String cookie);

    public void onPlayerUpdate(int playerId, HashMap<String, String> data);

    public void onGameUpdate(int gameId, String status);

    public void onConfigUpdate(ArrayList<Configurable> configList);

    public void onPlayerListUpdate(String type, ArrayList<Player> list);

    public void onEstateUpdate(int estateId, HashMap<String, String> data);

    public void setHandler(Handler netHandler);

    public void onGameItemUpdate(GameItem gameItem);

    public void onPlayerDelete(int playerId);

    public void onEstateGroupUpdate(int estateGroupId, HashMap<String, String> data);

    public void onAuctionUpdate(int auctionId, HashMap<String, String> data);

}
