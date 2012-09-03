package edu.rochester.nbook.monopdroid.gamelist;

import java.util.List;

public interface GameListFetcherListener {
    void onGameListFetching();

    void onGameListFetched(List<GameItem> result);

    void onException(String description, Exception ex);
}
