package edu.rochester.nbook.monopdroid;

import java.util.List;

public interface MonoProtocolGameListener extends MonoProtocolListener {
    public void onChatMessage(int playerId, String author, String text);

    public void onErrorMessage(String text);

    public void onDisplayMessage(int estateId, String text, boolean clearText, boolean clearButtons);

    public void onServer(String version);

    public void onClient(int playerId, String cookie);

    public void onPlayerUpdate(int playerId, String key, Object value);

    public void onGameUpdate(int gameId, String status);

    public void onConfigUpdate(List<Configurable> configList);

    public void onPlayerListUpdate(String type, List<Player> list);
}
