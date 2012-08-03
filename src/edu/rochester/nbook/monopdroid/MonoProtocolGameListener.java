package edu.rochester.nbook.monopdroid;

import java.util.List;

public interface MonoProtocolGameListener extends MonoProtocolListener {
	public void onServer(String version);
	public void onClient(int playerId, String cookie);
	public void onPlayerUpdate(int playerId, String key, Object value);
	public void onGameUpdate(int gameId, String status);
	public void onConfigUpdate(List<Configurable> configList);
}
