package com.natembook.monopdroid.board;

/**
 * Possible trade proposal types.
 * @author Nate
 *
 */
public enum TradeOfferType {
    /**
     * Money: giving an amount X from player A to player B.
     */
    MONEY(1),
    /**
     * Estate: giving an estate X from the current owner to player B.
     */
    ESTATE(2),
    /**
     * Card: giving a card X from the current owner to player B.
     */
    CARD(3);
    
    public String toString() {
        switch (this) {
        default:
        case MONEY:
            return "Money";
        case ESTATE:
            return "Estate";
        case CARD:
            return "Card";
        }
    };
    
    private int index;
    
    private TradeOfferType(int index) {
        this.index = index;
    }
    
    public int getIndex() {
        return index;
    }

    public static TradeOfferType fromIndex(int index) {
        for (TradeOfferType type : values()) {
            if (type.getIndex() == index) {
                return type;
            }
        }
        return null;
    }
}
