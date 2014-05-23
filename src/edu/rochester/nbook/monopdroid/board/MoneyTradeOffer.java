package edu.rochester.nbook.monopdroid.board;

import java.util.HashMap;

public final class MoneyTradeOffer extends TradeOffer {
    public MoneyTradeOffer(int playerIdFrom, int playerIdTo, int amount) {
        super(TradeOfferType.MONEY, playerIdFrom, playerIdTo, amount);
    }
    
    @Override
    public TradeOffer merge(HashMap<TradeOfferKey, TradeOffer> currentOffers) {
        TradeOffer o = super.merge(currentOffers);
        if (offerValue <= 0 || playerIdFrom == playerIdTo) {
            // remove offers of no money or from player A to player A
            return null;
        } else {
            return o;
        }
    }

    /**
     * Override: do not make the key include the amount (so that
     * there can only be one offer per send-receive pair for money).
     */
    @Override
    public TradeOfferKey generateKey() {
        return new TradeOfferKey(type, playerIdFrom, playerIdTo, -1);
    }
    
    public int getAmount() {
        return offerValue;
    }
}
