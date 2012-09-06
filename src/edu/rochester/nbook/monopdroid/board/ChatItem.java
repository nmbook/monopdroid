package edu.rochester.nbook.monopdroid.board;

public class ChatItem {
    private String text;
    private int color;
    private int playerId;
    private int estateId;
    private boolean clickable;

    public ChatItem(String text, int color, int playerId, int estateId) {
        this.text = text;
        this.color = color;
        this.playerId = playerId;
        this.estateId = estateId;
        this.clickable = (playerId > 0) || (estateId > 0);
    }

    public String getText() {
        return this.text;
    }

    public int getColor() {
        return this.color;
    }

    public int getPlayerId() {
        return this.playerId;
    }

    public int getEstateId() {
        return this.estateId;
    }

    public boolean isClickable() {
        return this.clickable;
    }
}
