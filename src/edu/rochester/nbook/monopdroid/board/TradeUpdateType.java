package edu.rochester.nbook.monopdroid.board;

import java.util.Locale;

public enum TradeUpdateType {
    UPDATED, NEW, REJECTED, ACCEPTED, COMPLETED;
    
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