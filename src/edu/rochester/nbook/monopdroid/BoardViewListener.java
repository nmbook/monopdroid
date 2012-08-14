package edu.rochester.nbook.monopdroid;

public interface BoardViewListener {
    public void onResize(int width, int height);
    
    public void onConfigChange(String command, String value);

    public void onStartGame();
}
