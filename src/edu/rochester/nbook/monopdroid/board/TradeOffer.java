package edu.rochester.nbook.monopdroid.board;

import java.util.HashMap;

public abstract class TradeOffer implements TradeUpdateSubject {
    protected TradeOfferType type;
    protected int playerIdFrom;
    protected int playerIdTo;
    protected int offerValue;
    
    protected TradeOffer(TradeOfferType type, int playerIdFrom, int playerIdTo, int offerValue) {
        this.type = type;
        this.playerIdFrom = playerIdFrom;
        this.playerIdTo = playerIdTo;
        this.offerValue = offerValue;
    }

    /**
     * Call this to merge this offer into a current list of trade offers.
     * @param currentOffers A list of current offers. Offers that conflict with this one should be removed from this list.
     * @return A trade offer to add to the final list. If this returns {@code null}, no new offer should be added (this trade offer was a removal).
     */
    public TradeOffer merge(HashMap<TradeOfferKey, TradeOffer> currentOffers) {
        TradeOfferKey[] keys = (TradeOfferKey[]) currentOffers.keySet().toArray();
        for (int i = 0; i < keys.length; i++) {
            if (currentOffers.equals(keys[i])) {
                currentOffers.remove(keys[i]);
            }
        }
        return this;
    }

    /**
     * Call to generate a TradeOfferKey object. It must be unique to an offer line, and is used to make the current offer list only non-repeated offers (i.e. only one trade of type money between party A and B or only one trade of type estate for estate X).
     * @return A key to represent a unique trade offer type.
     */
    public TradeOfferKey generateKey() {
        return new TradeOfferKey(type, playerIdFrom, playerIdTo, offerValue);
    }
    
    public final TradeOfferType getType() {
        return this.type;
    }
}
