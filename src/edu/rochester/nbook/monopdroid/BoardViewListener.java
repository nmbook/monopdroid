package edu.rochester.nbook.monopdroid;

public interface BoardViewListener {
    public void onConfigChange(String command, String value);

    public void onStartGame();
}
