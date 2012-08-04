package edu.rochester.nbook.monopdroid;

import java.util.List;

public interface MonoProtocolMetaListener extends MonoProtocolListener {
    public void onMetaServer(String version);

    public void onServerGameList(String host, int port, String version, List<GameItem> games);

    public void onServerGameListEnd();

    public void onServerList(List<ServerItem> servers);
}
