package edu.rochester.nbook.monopdroid.monopd;

import java.util.List;

import edu.rochester.nbook.monopdroid.gamelist.GameItem;
import edu.rochester.nbook.monopdroid.gamelist.ServerItem;

public interface MonoProtocolMetaListener extends MonoProtocolListener {
    public void onMetaServer(String version);

    public void onServerGameList(String host, int port, String version, List<GameItem> games);

    public void onServerGameListEnd();

    public void onServerList(List<ServerItem> servers);
}
