package edu.rochester.nbook.monopdroid.board;

/**
 * Possible trade proposal types.
 * @author Nate
 *
 */
public enum TradeOfferType {
    /**
     * Money: giving an amount X from player A to player B.
     */
    MONEY,
    /**
     * Estate: giving an estate X from the current owner to player B.
     */
    ESTATE,
    /**
     * Card: giving a card X from the current owner to player B.
     */
    CARD
}
