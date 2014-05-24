package com.natembook.monopdroid.gamelist;

import java.util.ArrayList;

import com.natembook.monopdroid.R;
import com.natembook.monopdroid.SettingsActivity;
import com.natembook.monopdroid.board.BoardActivity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.TextView;

public class GameListActivity extends ListActivity {
    private GameListAdapter adapter = null;
    private boolean gettingGameList = false;
    private GameListFetcher fetcher = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ArrayList<GameItem> gameList = new ArrayList<GameItem>();
        this.adapter = new GameListAdapter(this, R.layout.game_item, gameList);
        addSavedGame();
        adapter.add(new GameItem(GameItemType.READY));
        this.setListAdapter(this.adapter);
        ListView lv = (ListView) this.findViewById(android.R.id.list);
        lv.setOnItemLongClickListener(new OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> listView, View item, int position, long id) {
                Bundle state = new Bundle();
                GameItem o = GameListActivity.this.adapter.getItem(position);
                switch (o.getItemType()) {
                case CREATE:
                case JOIN:
                case RECONNECT:
                    state.putString("name", o.getDescription());
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
                    state.putString(
                            "info",
                            hostLine
                                    + "\n"
                                    + "Server Version: "
                                    + o.getServer().getVersion()
                                    + "\n"
                                    + "Type: "
                                    + o.getTypeName()
                                    + " ("
                                    + o.getType()
                                    + ")\n"
                                    + "Description: "
                                    + o.getDescription()
                                    + (o.getItemType() == GameItemType.CREATE ? "" : "\n" + "Players: " + o.getPlayers() + "\n"
                                            + "Allowed to Join: " + o.canJoin()));
                    GameListActivity.this.showDialog(R.id.dialog_server_info, state);
                    return true;
                default:
                    return false;
                }
            }
        });
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
        outState.putInt("count", this.adapter.getCount());
        for (int i = 0; i < this.adapter.getCount(); i++) {
            GameItem o = this.adapter.getItem(i);
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
    protected void onResume() {
        super.onResume();
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
        this.adapter.clear();
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
            this.adapter.add(new GameItem(item_type, id, servers, type, type_name, descr, players, can_join));
        }
        this.adapter.notifyDataSetChanged();
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
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

    @Override
    protected Dialog onCreateDialog(int id) {
        AlertDialog.Builder bldr = new AlertDialog.Builder(this);
        OnClickListener doClose = new OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        };
        switch (id) {
        case R.id.dialog_list_error:
            bldr.setTitle(R.string.dialog_list_error);
            bldr.setMessage(R.string.empty);
            bldr.setPositiveButton(android.R.string.ok, doClose);
            return bldr.create();
        case R.id.dialog_server_info:
            bldr.setTitle(R.string.dialog_server_info);
            bldr.setMessage(R.string.empty);
            bldr.setPositiveButton(android.R.string.ok, doClose);
            return bldr.create();
        }
        return null;
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog, Bundle args) {
        super.onPrepareDialog(id, dialog, args);
        TextView message = (TextView) dialog.findViewById(android.R.id.message);
        switch (id) {
        case R.id.dialog_list_error:
            String error = args.getString("error");
            message.setText(error);
            break;
        case R.id.dialog_server_info:
            String info = args.getString("info");
            String name = args.getString("name");
            message.setText(info);
            dialog.setTitle(String.format(this.getString(R.string.dialog_server_info), name));
            break;
        }
    }

    private void joinGame(int position) {
        GameItem o = this.adapter.getItem(position);
        if (!o.canJoin()) {
            // not joinable, perhaps because this isn't a real game item
            return;
        }
        // remove all servers but one
        o.chooseServer();
        Intent i = new Intent(this, BoardActivity.class);
        i.putExtra("edu.rochester.nbook.game_id", o.getGameId());
        i.putExtra("edu.rochester.nbook.host", o.getServer().getHost());
        i.putExtra("edu.rochester.nbook.port", o.getServer().getPort());
        i.putExtra("edu.rochester.nbook.version", o.getServer().getVersion());
        i.putExtra("edu.rochester.nbook.type", o.getType());
        i.putExtra("edu.rochester.nbook.type_name", o.getTypeName());
        i.putExtra("edu.rochester.nbook.descr", o.getDescription());
        i.putExtra("edu.rochester.nbook.players", o.getPlayers());
        i.putExtra("edu.rochester.nbook.can_join", o.canJoin());
        i.putExtra("edu.rochester.nbook.act_type", o.getItemType().getIndex());
        this.startActivity(i);
    }

    private void getGameList() {
        if (this.gettingGameList) {
            return;
        }
        this.gettingGameList = true;
        this.fetcher = new GameListFetcher(this, new GameListFetcherListener() {

            @Override
            public void onGameListFetching() {
                GameListActivity.this.adapter.clear();
                addSavedGame();
                GameListActivity.this.adapter.add(new GameItem(GameItemType.LOADING));
                GameListActivity.this.adapter.notifyDataSetChanged();
            }

            @Override
            public void onGameListFetched(ArrayList<GameItem> result) {
                GameListActivity.this.adapter.clear();
                addSavedGame();
                for (GameItem item : result) {
                    GameListActivity.this.adapter.add(item);
                }
                if (result.size() == 0) {
                    GameListActivity.this.adapter.add(new GameItem(GameItemType.EMPTY));
                }
                GameListActivity.this.adapter.notifyDataSetChanged();
                GameListActivity.this.gettingGameList = false;
            }

            @Override
            public void onException(final String description, final Exception ex) {
                GameListActivity.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        Bundle state = new Bundle();
                        GameListActivity.this.gettingGameList = false;
                        GameListActivity.this.adapter.clear();
                        addSavedGame();
                        GameListActivity.this.adapter.add(new GameItem(GameItemType.ERROR));
                        GameListActivity.this.adapter.notifyDataSetChanged();
                        state.putString("error", description + ": " + ex.getMessage());
                        GameListActivity.this.showDialog(R.id.dialog_list_error, state);
                    }
                });
            }
        });
        this.fetcher.execute();
    }
}
