package edu.rochester.nbook.monopdroid;

import android.graphics.PointF;
import android.graphics.RectF;

public class Estate {
    // estate data
    private int estateId;
    private String name;
    private String icon;
    private int houses;
    private int money;
    private int price;
    private int mortgagePrice;
    private int unmortgagePrice;
    private int sellHousePrice;
    private int color;
    private int bgColor;
    private int owner;
    private int housePrice;
    private int estateGroup;
    private int passMoney;
    private int taxPercentage;
    private int tax;
    private int payAmount;
    private int[] rent = new int[6];
    private boolean mortgaged;
    private boolean canBeOwned;
    private boolean canToggleMortgage;
    private boolean canBuyHouses;
    private boolean canSellHouses;
    private boolean isJail;
    private boolean isToJail;
    
    // boardView stuff
    private RectF bounds;
    private RectF ownerBounds;
    private PointF ownerLocation;
    private PointF pieceLocation;
    private PointF pieceLocationOffset;
    private float drawRadius;

    public Estate(int estateId) {
        this.estateId = estateId;
    }

    public int getEstateId() {
        return this.estateId;
    }

    public void setEstateId(int estateId) {
        this.estateId = estateId;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getHouses() {
        return this.houses;
    }

    public void setHouses(int houses) {
        this.houses = houses;
    }

    public int getMoney() {
        return this.money;
    }

    public void setMoney(int money) {
        this.money = money;
    }

    public int getPrice() {
        return this.price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getMortgagePrice() {
        return this.mortgagePrice;
    }

    public void setMortgagePrice(int mortgagePrice) {
        this.mortgagePrice = mortgagePrice;
    }

    public int getUnmortgagePrice() {
        return this.unmortgagePrice;
    }

    public void setUnmortgagePrice(int unmortgagePrice) {
        this.unmortgagePrice = unmortgagePrice;
    }

    public int getSellHousePrice() {
        return this.sellHousePrice;
    }

    public void setSellHousePrice(int sellHousePrice) {
        this.sellHousePrice = sellHousePrice;
    }

    public boolean isMortgaged() {
        return this.mortgaged;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public int getPassMoney() {
        return passMoney;
    }

    public void setPassMoney(int passMoney) {
        this.passMoney = passMoney;
    }

    public int getTaxPercentage() {
        return taxPercentage;
    }

    public void setTaxPercentage(int taxPercentage) {
        this.taxPercentage = taxPercentage;
    }

    public int getTax() {
        return tax;
    }

    public void setTax(int tax) {
        this.tax = tax;
    }

    public int getPayAmount() {
        return payAmount;
    }

    public void setPayAmount(int payAmount) {
        this.payAmount = payAmount;
    }

    public boolean isJail() {
        return isJail;
    }

    public void setIsJail(boolean isJail) {
        this.isJail = isJail;
    }

    public boolean isToJail() {
        return isToJail;
    }

    public void setIsToJail(boolean isToJail) {
        this.isToJail = isToJail;
    }

    public void setMortgaged(boolean mortgaged) {
        this.mortgaged = mortgaged;
    }

    public int getColor() {
        return this.color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public int getBgColor() {
        return this.bgColor;
    }

    public void setBgColor(int bgColor) {
        this.bgColor = bgColor;
    }

    public int getOwner() {
        return this.owner;
    }

    public void setOwner(int owner) {
        this.owner = owner;
    }

    public int getHousePrice() {
        return this.housePrice;
    }

    public void setHousePrice(int housePrice) {
        this.housePrice = housePrice;
    }

    public int getEstateGroup() {
        return this.estateGroup;
    }

    public void setEstateGroup(int estateGroup) {
        this.estateGroup = estateGroup;
    }

    public int getRent(int houses) {
        return this.rent[houses];
    }

    public void setRent(int houses, int rent) {
        this.rent[houses] = rent;
    }

    public boolean canBeOwned() {
        return this.canBeOwned;
    }

    public void setCanBeOwned(boolean canBeOwned) {
        this.canBeOwned = canBeOwned;
    }

    public boolean canToggleMortgage() {
        return this.canToggleMortgage;
    }

    public void setCanToggleMortgage(boolean canToggleMortgage) {
        this.canToggleMortgage = canToggleMortgage;
    }

    public boolean canBuyHouses() {
        return this.canBuyHouses;
    }

    public void setCanBuyHouses(boolean canBuyHouses) {
        this.canBuyHouses = canBuyHouses;
    }

    public boolean canSellHouses() {
        return this.canSellHouses;
    }

    public void setCanSellHouses(boolean canSellHouses) {
        this.canSellHouses = canSellHouses;
    }
    
    public RectF getDrawRegion() {
        return this.bounds;
    }

    public void setDrawRegion(RectF bounds) {
        this.bounds = bounds;
    }
    
    public RectF getDrawOwnerRegion() {
        return this.ownerBounds;
    }

    public void setDrawOwnerRegion(RectF ownerBounds) {
        this.ownerBounds = ownerBounds;
    }

    public PointF getDrawOwnerLocation() {
        return ownerLocation;
    }

    public void setDrawOwnerLocation(PointF ownerLocation) {
        this.ownerLocation = ownerLocation;
    }

    public PointF getDrawPieceLocation() {
        return pieceLocation;
    }

    public void setPieceLocation(PointF location) {
        pieceLocation = location;
    }
    
    public PointF getDrawPieceLocationOffset() {
        return pieceLocationOffset;
    }
    
    public void setPieceLocationOffset(PointF offset) {
        pieceLocationOffset = offset;
    }

    public float getDrawRadius() {
        return drawRadius;
    }

    public void setDrawRadius(float radius) {
        this.drawRadius = radius;
    }
    
    @Override
    public String toString() {
        return this.name + " (id: " + this.estateId + ")";
    }
}
