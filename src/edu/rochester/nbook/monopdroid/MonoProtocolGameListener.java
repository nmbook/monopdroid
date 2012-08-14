package edu.rochester.nbook.monopdroid;

import java.util.HashMap;
import java.util.List;

import android.os.Handler;

public interface MonoProtocolGameListener extends MonoProtocolListener {
    public void onChatMessage(int playerId, String author, String text);

    public void onErrorMessage(String text);

    public void onDisplayMessage(int estateId, String text, boolean clearText, boolean clearButtons);

    public void onServer(String version);

    public void onClient(int playerId, String cookie);

    public void onPlayerUpdate(int playerId, HashMap<String, String> data);

    public void onGameUpdate(int gameId, String status);

    public void onConfigUpdate(List<Configurable> configList);

    public void onPlayerListUpdate(String type, List<Player> list);

    public void onEstateUpdate(int estateId, HashMap<String, String> data);

    public void setHandler(Handler netHandler);

    public void onGameItemUpdate(GameItem gameItem);

    public void onPlayerDelete(int playerId);

}
