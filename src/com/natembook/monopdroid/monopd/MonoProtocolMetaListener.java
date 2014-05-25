package com.natembook.monopdroid.monopd;

import java.util.ArrayList;

import com.natembook.monopdroid.gamelist.GameItem;
import com.natembook.monopdroid.gamelist.ServerItem;

public interface MonoProtocolMetaListener extends MonoProtocolListener {
    public void onMetaServer(String version);

    public void onServerGameList(String host, int port, String version, ArrayList<GameItem> arrayList);

    public void onServerGameListEnd();

    public void onServerList(ArrayList<ServerItem> servers);
}
