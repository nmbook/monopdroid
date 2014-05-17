package edu.rochester.nbook.monopdroid.monopd;

import java.util.ArrayList;

import edu.rochester.nbook.monopdroid.gamelist.GameItem;
import edu.rochester.nbook.monopdroid.gamelist.ServerItem;

public interface MonoProtocolMetaListener extends MonoProtocolListener {
    public void onMetaServer(String version);

    public void onServerGameList(String host, int port, String version, ArrayList<GameItem> arrayList);

    public void onServerGameListEnd();

    public void onServerList(ArrayList<ServerItem> servers);
}
