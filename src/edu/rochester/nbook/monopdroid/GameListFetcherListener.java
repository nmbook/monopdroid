package edu.rochester.nbook.monopdroid;

import java.util.List;

public interface GameListFetcherListener {
    void onGameListFetching();

    void onGameListFetched(List<GameItem> result);

    void onException(String description, Exception ex);
}
