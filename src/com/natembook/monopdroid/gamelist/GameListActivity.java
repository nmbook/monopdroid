package com.natembook.monopdroid.gamelist;

import java.util.ArrayList;

import com.natembook.monopdroid.R;
import com.natembook.monopdroid.SettingsActivity;
import com.natembook.monopdroid.board.BoardActivity;
import com.natembook.monopdroid.dialogs.MonopolyDialog;
import com.natembook.monopdroid.dialogs.MonopolyDialogHost;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;

public class GameListActivity extends FragmentActivity implements
        OnItemClickListener, OnItemLongClickListener, GameListFetcherListener,
        MonopolyDialogHost {
    private GameListAdapter adapter = null;
    private boolean gettingGameList = false;
    private GameListFetcher fetcher = null;
    private MonopolyDialog dialog = null;
    private boolean running = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ArrayList<GameItem> gameList = new ArrayList<GameItem>();
        adapter = new GameListAdapter(this, R.layout.game_item, gameList);
        addSavedGame();
        adapter.add(new GameItem(GameItemType.READY));
        
        ListView lv = new ListView(this);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(this);
        lv.setOnItemLongClickListener(this);
        
        this.setContentView(lv);
    }

    private void addSavedGame() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        
        int gameId = prefs.getInt("saved_game_id", 0);
        String server = prefs.getString("saved_server", null);
        int port = prefs.getInt("saved_port", 0);
        String version = prefs.getString("saved_version", null);
        String type = prefs.getString("saved_type", null);
        String type_name = prefs.getString("saved_type_name", null);
        String descr = prefs.getString("saved_descr", null);
        int players = prefs.getInt("saved_players", 0);
        
        if (gameId > 0) {
            adapter.add(new GameItem(gameId, server, port, version, type, type_name, descr, players));
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (this.gettingGameList) {
            fetcher.cancel(true);
            this.gettingGameList = false;
        }
        outState.putInt("count", adapter.getCount());
        for (int i = 0; i < adapter.getCount(); i++) {
            GameItem o = adapter.getItem(i);
            outState.putInt("item_" + i + "_id", o.getGameId());
            outState.putInt("item_" + i + "_s_count", o.getServers().size());
            for (int j = 0; j < o.getServers().size(); j++) {
                outState.putString("item_" + i + "_s_" + j + "_host", o.getServers().get(j).getHost());
                outState.putInt("item_" + i + "_s_" + j + "_port", o.getServers().get(j).getPort());
                outState.putString("item_" + i + "_s_" + j + "_version", o.getServers().get(j).getVersion());
                outState.putInt("item_" + i + "_s_" + j + "_players", o.getServers().get(j).getUsers());
            }
            outState.putString("item_" + i + "_type", o.getType());
            outState.putString("item_" + i + "_type_name", o.getTypeName());
            outState.putString("item_" + i + "_descr", o.getDescription());
            outState.putInt("item_" + i + "_players", o.getPlayers());
            outState.putBoolean("item_" + i + "_can_join", o.canJoin());
            outState.putInt("item_" + i + "_item_type", o.getItemType().getIndex());
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        running = false;
    }
    
    @Override
    protected void onStart() {
        super.onStart();
        running = true;
        if (!this.gettingGameList) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            if (prefs.getBoolean("gamelist_auto", false)) {
                this.getGameList();
            }
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle state) {
        super.onRestoreInstanceState(state);
        adapter.clear();
        addSavedGame();
        int count = state.getInt("count");
        for (int i = 0; i < count; i++) {
            int id = state.getInt("item_" + i + "_id");
            int serverCount = state.getInt("item_" + i + "_s_count");
            ArrayList<ServerItem> servers = new ArrayList<ServerItem>();
            for (int j = 0; j < serverCount; j++) {
                String host = state.getString("item_" + i + "_s_" + j + "_host");
                int port = state.getInt("item_" + i + "_s_" + j + "_port");
                String version = state.getString("item_" + i + "_s_" + j + "_version");
                int players = state.getInt("item_" + i + "_s_" + j + "_players");
                servers.add(new ServerItem(host, port, version, players));
            }
            String type = state.getString("item_" + i + "_type");
            String type_name = state.getString("item_" + i + "_type_name");
            String descr = state.getString("item_" + i + "_descr");
            int players = state.getInt("item_" + i + "_players");
            boolean can_join = state.getBoolean("item_" + i + "_can_join");
            GameItemType item_type = GameItemType.fromInt(state.getInt("item_" + i + "_item_type"));
            adapter.add(new GameItem(item_type, id, servers, type, type_name, descr, players, can_join));
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        GameItem item = adapter.getItem(position);
        GameItemType type = item.getItemType();
        switch (type) {
        case ERROR:
        case READY:
        case EMPTY:
            this.getGameList();
            break;
        case JOIN:
        case CREATE:
        case RECONNECT:
            this.joinGame(position);
            break;
        case LOADING:
            break;
        }
    }
    
    @Override
    public boolean onItemLongClick(AdapterView<?> listView, View item, int position, long id) {
        GameItem o = adapter.getItem(position);
        switch (o.getItemType()) {
        case CREATE:
        case JOIN:
        case RECONNECT:
            String hostLine = null;
            if (o.getServers().size() == 1) {
                hostLine = "Host: " + o.getServer().getHost() + ":" + o.getServer().getPort();
            } else {
                hostLine = "Hosts: ";
                for (ServerItem server : o.getServers()) {
                    hostLine += server.getHost() + ":" + server.getPort() + ", ";
                }
                hostLine = hostLine.substring(0, hostLine.length() - 2);
            }
            String info = hostLine + "\n" +
                    "Server version: " + o.getServer().getVersion() + "\n" +
                    "Type: " + o.getTypeName() + " (" + o.getType() + ")\n" +
                    "Description: " + o.getDescription() +
                    (o.getItemType() == GameItemType.CREATE ? "" : "\n" +
                    "Players: " + o.getPlayers() + "\n" +
                    "Allowed to Join: " + o.canJoin());
            
            Bundle args = new Bundle();
            args.putInt("dialogType", R.id.dialog_type_info);
            args.putString("title", String.format(getString(R.string.dialog_server_info), o.getDescription()));
            args.putString("message", info);
            dialog = MonopolyDialog.showNewDialog(this, args);
            return true;
        default:
            return false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.getMenuInflater().inflate(R.menu.game_list_activity, menu);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch (item.getItemId()) {
        case R.id.menu_refresh:
            this.getGameList();
            break;
        case R.id.menu_settings:
            Intent settings = new Intent(this, SettingsActivity.class);
            this.startActivity(settings);
            break;
        }
        return true;
    }

    private void joinGame(int position) {
        GameItem o = adapter.getItem(position);
        if (!o.canJoin()) {
            // not joinable, perhaps because this isn't a real game item
            return;
        }
        // remove all servers but one
        o.chooseServer();
        Intent i = new Intent(this, BoardActivity.class);
        i.putExtra("com.natembook.game_id", o.getGameId());
        i.putExtra("com.natembook.host", o.getServer().getHost());
        i.putExtra("com.natembook.port", o.getServer().getPort());
        i.putExtra("com.natembook.version", o.getServer().getVersion());
        i.putExtra("com.natembook.type", o.getType());
        i.putExtra("com.natembook.type_name", o.getTypeName());
        i.putExtra("com.natembook.descr", o.getDescription());
        i.putExtra("com.natembook.players", o.getPlayers());
        i.putExtra("com.natembook.can_join", o.canJoin());
        i.putExtra("com.natembook.act_type", o.getItemType().getIndex());
        this.startActivity(i);
    }

    private void getGameList() {
        if (this.gettingGameList) {
            return;
        }
        this.gettingGameList = true;
        this.fetcher = new GameListFetcher(this, this);
        this.fetcher.execute();
    }
    

    @Override
    public void onGameListFetching() {
        adapter.clear();
        addSavedGame();
        adapter.add(new GameItem(GameItemType.LOADING));
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onGameListFetched(ArrayList<GameItem> result) {
        adapter.clear();
        addSavedGame();
        for (GameItem item : result) {
            adapter.add(item);
        }
        if (result.size() == 0) {
            adapter.add(new GameItem(GameItemType.EMPTY));
        }
        adapter.notifyDataSetChanged();
        gettingGameList = false;
    }

    @Override
    public void onException(final String description, final Exception ex) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                gettingGameList = false;
                adapter.clear();
                addSavedGame();
                adapter.add(new GameItem(GameItemType.ERROR));
                adapter.notifyDataSetChanged();

                Bundle args = new Bundle();
                args.putInt("dialogType", R.id.dialog_type_error);
                args.putString("title", getString(R.string.dialog_list_error));
                args.putString("message", description + ": " + ex.getMessage());
                dialog = MonopolyDialog.showNewDialog(GameListActivity.this, args);
            }
        });
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public MonopolyDialog getCurrentDialog() {
        return dialog;
    }
}
