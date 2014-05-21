package edu.rochester.nbook.monopdroid.board;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import android.util.SparseArray;

public final class Trade {
    
    public static final HashMap<String, XmlAttribute> tradeAttributes = new HashMap<String, XmlAttribute>() {
        private static final long serialVersionUID = 7243437286161425757L;

        {
            this.put("type", new XmlAttribute(Trade.class, "setType", XmlAttributeType.STRING));
            this.put("actor", new XmlAttribute(Trade.class, "setActorId", XmlAttributeType.INT));
            this.put("revision", new XmlAttribute(Trade.class, "setRevision", XmlAttributeType.INT));
        }
    };
    
    // trade object data
    private int tradeId;
    private int actorId;
    private int revision;
    private TradeUpdateType lastUpdateType;
    
    private int creatorId;
    
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
    
    public int getCreatorId() {
        return creatorId;
    }
    
    public void setCreatorId(int creatorId) {
        this.creatorId = creatorId;
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
    
    public void setPlayer(TradePlayer player) {
        players.put(player.getPlayerId(), player);
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

    public int getOfferCount() {
        return offers.size();
    }
}
