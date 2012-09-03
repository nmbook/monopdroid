package edu.rochester.nbook.monopdroid.gamelist;

public enum GameItemType {
    ERROR(0),
    READY(1),
    EMPTY(2),
    LOADING(3),
    JOIN(4),
    CREATE(5),
    RECONNECT(6);
    
    private int index = 0;
    
    private GameItemType(int index) {
        this.index = index;
    }
    
    public static GameItemType fromInt(int index) {
        for (GameItemType type : GameItemType.values()) {
            if (type.index == index) {
                return type;
            }
        }
        return GameItemType.ERROR;
    }

    public int getIndex() {
        return this.index;
    } 
}
