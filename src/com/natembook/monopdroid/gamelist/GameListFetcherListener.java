package com.natembook.monopdroid.gamelist;

import java.util.ArrayList;

public interface GameListFetcherListener {
    void onGameListFetching();

    void onGameListFetched(ArrayList<GameItem> result);

    void onException(String description, Exception ex);
}
