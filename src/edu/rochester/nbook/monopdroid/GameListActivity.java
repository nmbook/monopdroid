package edu.rochester.nbook.monopdroid;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
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
        gameList.add(new GameItem(0, null, 0, null, null, null, null, 0, false));
        this.adapter = new GameListAdapter(this, R.layout.game_item, gameList);
        this.setListAdapter(this.adapter);
        ListView lv = (ListView) this.findViewById(android.R.id.list);
        lv.setOnItemLongClickListener(new OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> listView, View item, int position, long id) {
                Bundle state = new Bundle();
                GameItem o = GameListActivity.this.adapter.getItem(position);
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
                state.putString("info",
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
                                                + (o.getGameId() == -1 ? "" : "\n" + "Players: " + o.getPlayers()
                                                                + "\n" + "Allowed to Join: " + o.canJoin()));
                GameListActivity.this.showDialog(R.id.dialog_server_info, state);
                return true;
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (this.gettingGameList) {
            Log.e("monopd", "Error! Bad things can happen! We are fetching while rotating!");
        }
        outState.putInt("count", this.adapter.getCount());
        for (int i = 0; i < this.adapter.getCount(); i++) {
            GameItem o = this.adapter.getItem(i);
            outState.putInt("item_" + i + "_id", o.getGameId());
            outState.putInt("item_" + i + "_s_count", o.getServers().size());
            for (int j = 0; j < o.getServers().size(); j++) {
                outState.putString("item_" + i + "_s_" + j + "_host", o.getServers().get(j).getVersion());
                outState.putInt("item_" + i + "_s_" + j + "_port", o.getServers().get(j).getPort());
                outState.putString("item_" + i + "_s_" + j + "_version", o.getServers().get(j).getVersion());
                outState.putInt("item_" + i + "_s_" + j + "_players", o.getServers().get(j).getUsers());
            }
            outState.putString("item_" + i + "_type", o.getType());
            outState.putString("item_" + i + "_type_name", o.getTypeName());
            outState.putString("item_" + i + "_descr", o.getDescription());
            outState.putInt("item_" + i + "_players", o.getPlayers());
            outState.putBoolean("item_" + i + "_can_join", o.canJoin());
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
        int count = state.getInt("count");
        for (int i = 0; i < count; i++) {
            int id = state.getInt("item_" + i + "_id");
            int serverCount = state.getInt("item_" + i + "_s_count");
            List<ServerItem> servers = new ArrayList<ServerItem>();
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
            this.adapter.add(new GameItem(id, servers, type, type_name, descr, players, can_join));
        }
        this.adapter.notifyDataSetChanged();
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        // ids:
        // positive: tap to join
        // 0: tap to refresh
        // -1: tap to create
        // -2: loading... no action available
        // -3: empty, tap to refresh
        // -4: error, tap to retry
        if (id == 0 || id <= -3) {
            this.getGameList();
        } else if (id == -1) {
            this.joinGame(position, false);
        } else {
            this.joinGame(position, true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.getMenuInflater().inflate(R.menu.activity_server_list, menu);
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

    private void joinGame(int position, boolean is_join) {
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
        i.putExtra("edu.rochester.nbook.act_is_join", is_join);
        this.startActivity(i);
    }

    private void getGameList() {
        if (this.gettingGameList) {
            return;
        }
        this.gettingGameList = true;
        this.fetcher = new GameListFetcher(new GameListFetcherListener() {
            @Override
            public void onGameListFetching() {
                GameListActivity.this.adapter.clear();
                GameListActivity.this.adapter.add(new GameItem(-2, null, 0, null, null, null, null, 0, false));
                GameListActivity.this.adapter.notifyDataSetChanged();
            }

            @Override
            public void onGameListFetched(List<GameItem> result) {
                GameListActivity.this.adapter.clear();
                for (GameItem item : result) {
                    GameListActivity.this.adapter.add(item);
                }
                if (result.size() == 0) {
                    GameListActivity.this.adapter.add(new GameItem(-3, null, 0, null, null, null, null, 0, false));
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
                        GameListActivity.this.adapter.add(new GameItem(-4, null, 0, null, null, null, null, 0, false));
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
