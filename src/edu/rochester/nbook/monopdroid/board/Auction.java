package edu.rochester.nbook.monopdroid.board;

public class Auction {
    // estate data
    private int auctionId;
    private int actorId;
    private int estateId;
    private int highBid;
    private int highBidder;
    private int status;

    public Auction(int auctionId) {
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

    @Override
    public String toString() {
        return this.highBid + " by playerId " + this.highBidder + " (id: " + this.auctionId + ")";
    }
}
