package edu.rochester.nbook.monopdroid;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
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
        adapter = new GameListAdapter(this, R.layout.game_item, gameList);
        setListAdapter(adapter);
        ListView lv = (ListView) this.findViewById(android.R.id.list);
        lv.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> listView, View item,
					int position, long id) {
				Bundle state = new Bundle();
				GameItem o = adapter.getItem(position);
				state.putString("name", o.getDescription() + " Server");
				state.putString("info",
						"Host: " + o.getHost() + ":" + o.getPort() + "\n" +
						"Server Version: " + o.getVersion() + "\n" +
						"Type: " + o.getTypeName() + " (" + o.getType() + ")\n" +
						"Description: " + o.getDescription() + "\n" +
						"Players: " + o.getPlayers() + "\n" +
						"Allowed to " + (o.getGameId() == -1 ? "Create" : "Join") + ": " + o.canJoin());
				showDialog(R.id.dialog_server_info, state);
				return true;
			}
        });
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
    	super.onSaveInstanceState(outState);
    	if (gettingGameList) {
    		Log.e("monopd", "Error! Bad things can happen! We are fetching while rotating!");
    	}
    	outState.putInt("count", adapter.getCount());
    	for (int i = 0; i < adapter.getCount(); i++) {
    		GameItem o = adapter.getItem(i);
    		outState.putInt("item_" + i + "_id", o.getGameId());
    		outState.putString("item_" + i + "_host", o.getHost());
    		outState.putInt("item_" + i + "_port", o.getPort());
    		outState.putString("item_" + i + "_version", o.getVersion());
    		outState.putString("item_" + i + "_type", o.getType());
    		outState.putString("item_" + i + "_type_name", o.getTypeName());
    		outState.putString("item_" + i + "_descr", o.getDescription());
    		outState.putInt("item_" + i + "_players", o.getPlayers());
    		outState.putBoolean("item_" + i + "_can_join", o.canJoin());
    	}
    }
    
    @Override
    protected void onRestoreInstanceState(Bundle state) {
    	super.onRestoreInstanceState(state);
    	adapter.clear();
    	int count = state.getInt("count");
    	for (int i = 0; i < count; i++) {
    		int id = state.getInt("item_" + i + "_id");
    		String host = state.getString("item_" + i + "_host");
    		int port = state.getInt("item_" + i + "_port");
    		String version = state.getString("item_" + i + "_version");
    		String type = state.getString("item_" + i + "_type");
    		String type_name = state.getString("item_" + i + "_type_name");
    		String descr = state.getString("item_" + i + "_descr");
    		int players = state.getInt("item_" + i + "_players");
    		boolean can_join = state.getBoolean("item_" + i + "_can_join");
    		adapter.add(new GameItem(id, host, port, version, type, type_name, descr, players, can_join));
    	}
    	adapter.notifyDataSetChanged();
    }
    
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
    	super.onListItemClick(l, v, position, id);
    	if (id == 0) {
    		getGameList();
    	} else if (id < 0) {
    		joinGame(position, false);
    	} else {
    		joinGame(position, true);
    	}
    }

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_server_list, menu);
        return true;
    }
	
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_refresh:
			getGameList();
			break;
		case R.id.menu_settings:
			Intent settings = new Intent(this, SettingsActivity.class);
			startActivity(settings);
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
		GameItem o = adapter.getItem(position);
		if (!o.canJoin()) {
			// not joinable, perhaps because this isn't a real game item
			return;
		}
		Intent i = new Intent(this, BoardActivity.class);
		i.putExtra("edu.rochester.nbook.game_id",o.getGameId());
		i.putExtra("edu.rochester.nbook.host", o.getHost());
		i.putExtra("edu.rochester.nbook.port", o.getPort());
		i.putExtra("edu.rochester.nbook.version", o.getVersion());
		i.putExtra("edu.rochester.nbook.type", o.getType());
		i.putExtra("edu.rochester.nbook.type_name", o.getTypeName());
		i.putExtra("edu.rochester.nbook.descr", o.getDescription());
		i.putExtra("edu.rochester.nbook.players", o.getPlayers());
		i.putExtra("edu.rochester.nbook.can_join", o.canJoin());
		i.putExtra("edu.rochester.nbook.act_is_join", is_join);
		startActivity(i);
	}
	
	private void getGameList() {
		if (gettingGameList) {
			return;
		}
		gettingGameList = true;
		fetcher = new GameListFetcher(new GameListFetcherListener() {
			@Override
			public void onGameListFetching() {
		    	adapter.clear();
		        adapter.add(new GameItem(-2, null, 0, null, null, null, null, 0, false));
				adapter.notifyDataSetChanged();
			}
			
			@Override
			public void onGameListFetched(List<GameItem> result) {
				adapter.clear();
				for (GameItem item : result) {
					adapter.add(item);
				}
				if (result.size() == 0) {
			        adapter.add(new GameItem(-3, null, 0, null, null, null, null, 0, false));
				}
				adapter.notifyDataSetChanged();
				gettingGameList = false;
			}

			@Override
			public void onException(String description, Exception ex) {
				Bundle state = new Bundle();
				gettingGameList = false;
		    	adapter.clear();
		        adapter.add(new GameItem(-4, null, 0, null, null, null, null, 0, false));
				adapter.notifyDataSetChanged();
				state.putString("error", description + ": " + ex.getMessage());
				showDialog(R.id.dialog_list_error, state);
			}
		});
		fetcher.execute();
	}
}
