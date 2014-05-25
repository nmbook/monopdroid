package com.natembook.monopdroid.board;

import java.util.HashMap;

/**
 * Represents the MONOPD Auction object.
 * @author Nate
 *
 */
public class Auction {
    /**
     * Automatically settable attributes.
     */
    public static final HashMap<String, XmlAttribute> auctionAttributes = new HashMap<String, XmlAttribute>() {
        private static final long serialVersionUID = 2978911583408943533L;

        {
            this.put("actor", new XmlAttribute(Auction.class, "setActorId", XmlAttributeType.INT));
            this.put("estateid", new XmlAttribute(Auction.class, "setEstateId", XmlAttributeType.INT));
            this.put("status", new XmlAttribute(Auction.class, "setStatus", XmlAttributeType.INT));
            this.put("highbid", new XmlAttribute(Auction.class, "setHighBid", XmlAttributeType.INT));
            this.put("highbidder", new XmlAttribute(Auction.class, "setHighBidder", XmlAttributeType.INT));
        }
    };
    
    // auction object data
    private int auctionId;
    private int actorId;
    private int estateId;
    private int highBid;
    private int highBidder;
    private int status;
    
    private int numberBids;

    public Auction(int auctionId) {
        this.status = -1;
        this.auctionId = auctionId;
    }

    public int getAuctionId() {
        return this.auctionId;
    }
    
    public int getActorId() {
        return actorId;
    }

    public void setActorId(int actorId) {
        this.actorId = actorId;
    }

    public int getEstateId() {
        return estateId;
    }

    public void setEstateId(int estateId) {
        this.estateId = estateId;
    }

    public int getHighBid() {
        return highBid;
    }

    public void setHighBid(int highBid) {
        this.highBid = highBid;
    }

    public int getHighBidder() {
        return highBidder;
    }

    public void setHighBidder(int highBidder) {
        this.highBidder = highBidder;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
    
    public int getNumberOfBids() {
        return numberBids;
    }
    
    public void setNumberOfBids(int numberBids) {
        this.numberBids = numberBids;
    }

    @Override
    public String toString() {
        return this.highBid + " by playerId " + this.highBidder + " (id: " + this.auctionId + ")";
    }
}
