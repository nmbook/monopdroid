package edu.rochester.nbook.monopdroid.board;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;

import android.util.SparseArray;

public final class Trade {
    private enum TradeUpdateType {
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
    
    public static final HashMap<String, XmlAttribute> auctionAttributes = new HashMap<String, XmlAttribute>() {
        private static final long serialVersionUID = 2978911583408943533L;

        {
            this.put("type", new XmlAttribute(Trade.class, "setUpdateType", XmlAttributeType.STRING));
            this.put("actor", new XmlAttribute(Trade.class, "setActorId", XmlAttributeType.INT));
            this.put("revision", new XmlAttribute(Trade.class, "setRevision", XmlAttributeType.INT));
        }
    };
    
    // trade object data
    private int tradeId;
    private int actorId;
    private int revision;
    private TradeUpdateType lastUpdateType;
    
    private HashMap<TradeOfferKey, TradeOffer> offers = new HashMap<TradeOfferKey, TradeOffer>();
    private SparseArray<TradePlayer> players = new SparseArray<TradePlayer>();

    public Trade(int tradeId) {
        this.tradeId = tradeId;
    }

    public int getTradeId() {
        return this.tradeId;
    }
    
    public int getActorId() {
        return actorId;
    }

    public void setActorId(int actorId) {
        this.actorId = actorId;
    }

    public int getRevision() {
        return revision;
    }

    public void setRevision(int revision) {
        this.revision = revision;
    }
    
    public TradeUpdateType getLastUpdateType() {
        return this.lastUpdateType;
    }
    
    public void setType(String type) {
        this.lastUpdateType = TradeUpdateType.fromString(type);
    }
    
    public Collection<TradeOffer> getOffers() {
        return offers.values();
    }
    
    public Collection<TradePlayer> getPlayers() {
        ArrayList<TradePlayer> players = new ArrayList<TradePlayer>(this.players.size());
        for (int i = 0; i < this.players.size(); i++) {
            players.add(this.players.get(this.players.keyAt(i)));
        }
        return players;
    }
    
    public void setPlayer(int playerId, boolean accepted) {
        players.put(playerId, new TradePlayer(playerId, accepted));
    }
    
    public void mergeMoneyOffer(MoneyTradeOffer offer) {
        mergeOffer(offer);
    }
    
    public void mergeEstateOffer(EstateTradeOffer offer) {
        mergeOffer(offer);
    }
    
    public void mergeCardOffer(CardTradeOffer offer) {
        mergeOffer(offer);
    }

    private void mergeOffer(TradeOffer offer) {
        TradeOffer mergedOffer = offer.merge(offers);
        if (mergedOffer != null) {
            offers.put(mergedOffer.generateKey(), mergedOffer);
        }
    }

    @Override
    public String toString() {
        return "Trade by playerId " + this.actorId + " with " + this.offers.size() + " offers (id: " + this.tradeId + ").";
    }
}
