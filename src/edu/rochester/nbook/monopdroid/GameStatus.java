package edu.rochester.nbook.monopdroid;

public enum GameStatus {
    ERROR, JOIN, CREATE, RECONNECT, CONFIG, INIT, RUN;

    public static GameStatus fromString(String strStatus) {
        if (strStatus.equals("config")) {
            return CONFIG;
        } else if (strStatus.equals("init")) {
            return INIT;
        } else if (strStatus.equals("run")) {
            return RUN;
        } else {
            return ERROR;
        }
    }
}