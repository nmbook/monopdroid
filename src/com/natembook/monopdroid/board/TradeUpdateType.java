package com.natembook.monopdroid.board;

import java.util.Locale;

/**
 * Represents possible states of a trade.
 * @author Nate
 *
 */
public enum TradeUpdateType {
    /**
     * No "type" attribute was specified. This &lt;tradeupdate&gt; tag will probably have
     * children implementing {@link TradeUpdateSubject}.
     */
    UPDATED,
    /**
     * type="new". A trade was initiated. The actor created the trade. 
     */
    NEW,
    /**
     * type="rejected". A trade was rejected. The actor was the rejecter.
     * No further actions can be taken in this trade. 
     */
    REJECTED,
    /**
     * type="accepted". The trade was accepted by the current player. Other players
     * may still have to accept.
     */
    ACCEPTED,
    /**
     * type="completed". The trade was completed. The assets will change hands and
     * no further actions can be taken in this trade.
     */
    COMPLETED;
    
    public static TradeUpdateType fromString(String updateType) {
        for (TradeUpdateType type : values()) {
            if (type.toString().equals(updateType)) {
                return type;
            }
        }
        return UPDATED;
    }
    
    @Override
    public String toString() {
        return name().toLowerCase(Locale.US);
    }
}