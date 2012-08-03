package edu.rochester.nbook.monopdroid;

import java.util.ArrayList;
import java.util.List;

import android.os.AsyncTask;

public final class GameListFetcher extends AsyncTask<Void, Void, List<GameItem>> {
	private GameListFetcherListener callback = null;
	private ArrayList<GameItem> list = new ArrayList<GameItem>();
	private boolean continueReading = true;
	
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
	
	public GameListFetcher(GameListFetcherListener callback) {
		this.callback = callback;
	}
	
	@Override
	protected List<GameItem> doInBackground(Void... params) {
		MonoProtocolHandler monopd = new MonoProtocolHandler(new MonoProtocolMetaListener() {
			@Override
			public void onException(String description, Exception ex) {
				GameListFetcher.this.callback.onException("Unable to get list: " + description, ex);
			}
			
			@Override
			public void onClose() { }
			
			@Override
			public void onServerGameListEnd() {
				GameListFetcher.this.continueReading = false;
			}
			
			@Override
			public void onServerGameList(String host, int port, String version,
					List<GameItem> games) {
				for (GameItem game : games) {
					GameListFetcher.this.list.add(game);
				}
			}
			
			@Override
			public void onMetaServer(String version) { }

			@Override
			public void onServerList(List<ServerItem> servers) { }
		}, "monopd.gradator.net", 1240, "gtkatlantic", "0.4.1");
		
		monopd.sendMetaListGames();
		while (continueReading) { 
			monopd.doReceive();
		}
		
		return this.list;
	}
}
