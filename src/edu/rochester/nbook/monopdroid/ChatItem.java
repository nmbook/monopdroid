package edu.rochester.nbook.monopdroid;

public class ChatItem {
    private String text;
    private int color;
    private int playerId;
    private int estateId;
    private boolean underline;
    private boolean clickable;
    
    public ChatItem(String text, int color, int playerId, int estateId, boolean clearButtons) {
        this.text = text;
        this.color = color;
        this.playerId = playerId;
        this.estateId = estateId;
        this.underline = clearButtons;
        this.clickable = playerId > 0 || estateId > 0;
    }

    public String getText() {
        return text;
    }

    public int getColor() {
        return color;
    }

    public int getPlayerId() {
        return playerId;
    }

    public int getEstateId() {
        return estateId;
    }

    public boolean isUnderline() {
        return underline;
    }

    public boolean isClickable() {
        return clickable;
    }
}
