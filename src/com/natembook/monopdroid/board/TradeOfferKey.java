package com.natembook.monopdroid.board;

/**
 * Allows merging a {@link TradeOffer} with other {@link TradeOffer}s by
 * storing the parts of a trade proposal which uniquely identify a type of trade.
 *<p>
 * For example: A money trade offer's key contains the tuple (type, playerIdFrom, playerIdTo)
 * but doesn't stor the amount so that two trade offers with different amounts but the same other
 * three values cannot exist.
 * <p>
 * Tuples that define uniqueness by offer type:<br>
 * Money: (type, playerIdFrom, playerIdTo)<br>
 * Estate (type, offerKey/estateId)<br>
 * Card (type, offerKey/cardId)<br>
 * These tuples are defined by the trade proposal sub-classes when they make a key.
 * @author Nate
 *
 */
public class TradeOfferKey {
    private TradeOfferType type;
    private int playerIdFrom;
    private int playerIdTo;
    private int offerKey;
    
    public TradeOfferKey(TradeOfferType type, int playerIdFrom,
            int playerIdTo, int offerKey) {
        this.type = type;
        this.playerIdFrom = playerIdFrom;
        this.playerIdTo = playerIdTo;
        this.offerKey = offerKey;
    }

    @Override
    public boolean equals(Object o) {
        // Return true if the objects are identical.
        // (This is just an optimization, not required for correctness.)
        if (this == o) {
          return true;
        }

        // Return false if the other object has the wrong type.
        // This type may be an interface depending on the interface's specification.
        if (!(o instanceof TradeOfferKey)) {
          return false;
        }

        // Cast to the appropriate type.
        // This will succeed because of the instanceof, and lets us access private fields.
        TradeOfferKey lhs = (TradeOfferKey) o;

        // Check each field. Primitive fields, reference fields, and nullable reference
        // fields are all treated differently.
        return playerIdFrom == lhs.playerIdFrom &&
                playerIdTo == lhs.playerIdFrom &&
                offerKey == lhs.offerKey &&
                type.equals(lhs.type);
    }
    
    @Override
    public int hashCode() {
        // Start with a non-zero constant.
        int result = 17;

        // Include a hash for each field.
        result = 31 * result + playerIdFrom;
        result = 31 * result + playerIdTo;
        result = 31 * result + offerKey;
        result = 31 * result + type.hashCode();
        return result;
    }
    
    @Override
    public String toString() {
        return "offer " + type.toString() + " from " + playerIdFrom + " to " + playerIdTo + " of " + offerKey;
    }
}
