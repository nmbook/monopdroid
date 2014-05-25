package com.natembook.monopdroid.board;

public enum TradeOfferStep {
    /**
     * Step 1: Select an offer type ({@link TradeOfferType}).
     */
    TYPE(1),
    /**
     * Step 2: Select an offer sender (if {@link TradeOfferType.MONEY}, int playerId)
     */
    FROM(2),
    /**
     * Step 3: Select an offer value (money: int amount, estate: int estateId, or card: int cardId) 
     */
    VALUE(3),
    /**
     * Step 4: Select an offer receiver (int playerId)
     */
    TO(4),
    /**
     * Step 5: Complete offer add.
     */
    COMPLETE(5);
    
    private int index;
    
    private TradeOfferStep(int index) {
        this.index = index;
    }
    
    public int getIndex() {
        return index;
    }

    public static TradeOfferStep fromIndex(int index) {
        for (TradeOfferStep step : values()) {
            if (step.getIndex() == index) {
                return step;
            }
        }
        return null;
    }
}
