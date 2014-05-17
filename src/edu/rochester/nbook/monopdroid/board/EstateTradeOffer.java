package edu.rochester.nbook.monopdroid.board;

import java.util.HashMap;

public final class EstateTradeOffer extends TradeOffer {
    public EstateTradeOffer(int playerIdFrom, int playerIdTo, int estateId) {
        super(TradeOfferType.ESTATE, playerIdFrom, playerIdTo, estateId);
    }
    
    @Override
    public TradeOffer merge(HashMap<TradeOfferKey, TradeOffer> currentOffers) {
        TradeOffer o = super.merge(currentOffers);
        if (offerValue < 0 || playerIdFrom == playerIdTo) {
            // remove offers of no estate or from player A to player A
            return null;
        } else {
            return o;
        }
    }
    
    /**
     * Override: do not make the key include the sender (generated
     * by UI based on actual current owner).
     */
    @Override
    public TradeOfferKey generateKey() {
        return new TradeOfferKey(type, -1, playerIdTo, offerValue);
    }
    
    public int getPlayerIdFrom() {
        return playerIdFrom;
    }
    
    public int getPlayerIdTo() {
        return playerIdTo;
    }
    
    public int getEstateId() {
        return offerValue;
    }
}
