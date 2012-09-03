package edu.rochester.nbook.monopdroid.gamelist;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import edu.rochester.nbook.monopdroid.monopd.MonoProtocolHandler;
import edu.rochester.nbook.monopdroid.monopd.MonoProtocolMetaListener;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;

public final class GameListFetcher extends AsyncTask<Void, Void, List<GameItem>> {
    private GameListFetcherListener callback = null;
    private ArrayList<GameItem> list = new ArrayList<GameItem>();
    private boolean continueReading = true;
    private Activity act = null;

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        this.callback.onGameListFetching();
    }

    @Override
    protected void onPostExecute(List<GameItem> result) {
        super.onPostExecute(result);
        if (result != null) {
            this.callback.onGameListFetched(result);
        }
    }

    public GameListFetcher(Activity act, GameListFetcherListener callback) {
        this.act = act;
        this.callback = callback;
    }

    @Override
    protected List<GameItem> doInBackground(Void... params) {
        SharedPreferences prefs = act.getPreferences(Activity.MODE_PRIVATE);
        String server = prefs.getString("gamelist_server", "monopd.gradator.net");
        int port = prefs.getInt("gamelist_port", 1240);
        String clientName = "monopdroid";
        String clientVersion = "0.0.0";
        try {
            clientVersion = act.getPackageManager().getPackageInfo(act.getPackageName(), 0).versionName;
        } catch (NameNotFoundException e) { }
        MonoProtocolHandler monopd = new MonoProtocolHandler(new MonoProtocolMetaListener() {

            @Override
            public void onException(String description, Exception ex) {
                GameListFetcher.this.callback.onException("Unable to get list: " + description, ex);
            }

            @Override
            public void onClose(boolean remote) {
            }

            @Override
            public void onServerGameListEnd() {
                GameListFetcher.this.continueReading = false;
            }

            @Override
            public void onServerGameList(String host, int port, String version, List<GameItem> games) {
                for (GameItem game : games) {
                    GameListFetcher.this.list.add(game);
                }
            }

            @Override
            public void onMetaServer(String version) {
            }

            @Override
            public void onServerList(List<ServerItem> servers) {
            }
        }, server, port, clientName, clientVersion);

        monopd.sendMetaListGames();
        while (this.continueReading && !isCancelled()) {
            monopd.doReceive();
        }

        this.combineList(this.list);

        return this.list;
    }

    /**
     * Combines the games in the list that are of the same game type. Modifies
     * the list in place.
     * 
     * @param list
     *            The game list to combine.
     */
    private void combineList(ArrayList<GameItem> list) {
        HashMap<String, GameItem> types = new HashMap<String, GameItem>();
        int count = list.size();
        for (int i = 0; i < count;) {
            GameItem item = list.get(i);
            if (item.getGameId() < 0) {
                if (types.containsKey(item.getType())) {
                    // add on to the servers by reference
                    types.get(item.getType()).getServers().add(item.getServers().get(0));
                    list.remove(i);
                    count--;
                } else {
                    // reference the item in the list
                    types.put(item.getType(), item);
                    i++;
                }
            } else {
                i++;
            }
        }
        types.clear();
    }
}
