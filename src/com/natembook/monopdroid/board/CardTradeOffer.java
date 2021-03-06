package com.natembook.monopdroid.board;

import java.util.HashMap;

/**
 * A trade proposal of a card.
 * @author Nate
 *
 */
public final class CardTradeOffer extends TradeOffer {
    public CardTradeOffer(int playerIdFrom, int playerIdTo, int cardId) {
        super(TradeOfferType.CARD, playerIdFrom, playerIdTo, cardId);
    }
    
    @Override
    public TradeOffer merge(HashMap<TradeOfferKey, TradeOffer> currentOffers) {
        TradeOffer o = super.merge(currentOffers);
        if (offerValue < 0 || playerIdFrom == playerIdTo) {
            // remove offers of no card or from player A to player A
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
    
    public int getCardId() {
        return offerValue;
    }
}
