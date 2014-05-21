package edu.rochester.nbook.monopdroid.board.surface;

public enum BoardViewOverlay {
    /**
     * No overlay visible
     */
    NONE,
    /**
     * Player overlay (player ID, [TRADE][P!][D?][V?])
     */
    PLAYER,
    /**
     * Estate overlay (estate ID, [M/unM][+H][-H][SE])
     */
    ESTATE,
    /**
     * Auction overlay (auction ID, [+1][10][50][100][?])
     */
    AUCTION,
    /**
     * Trade overlay (trade ID, [ACCEPT][REJECT])
     */
    TRADE
}
