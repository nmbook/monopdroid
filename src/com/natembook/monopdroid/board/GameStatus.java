package com.natembook.monopdroid.board;

public enum GameStatus {
    /**
     * The game server encountered an error.
     */
    ERROR,
    /**
     * We are joining a game (not a server game state).
     */
    JOIN,
    /**
     * We are creating a game (not a server game state).
     */
    CREATE,
    /**
     * We are reconnecting to a game (not a server game state).
     */
    RECONNECT,
    /**
     * The game is awaiting the host to select options and press Start Game.
     */
    CONFIG,
    /**
     * The game is starting (this state should end shortly).
     */
    INIT,
    /**
     * The game is in progress.
     */
    RUN,
    /**
     * The game has ended.
     */
    END;

    public static GameStatus fromString(String strStatus) {
        if (strStatus.equals("config")) {
            return CONFIG;
        } else if (strStatus.equals("init")) {
            return INIT;
        } else if (strStatus.equals("run")) {
            return RUN;
        } else if (strStatus.equals("end")) {
            return END;
        } else {
            return ERROR;
        }
    }
}
