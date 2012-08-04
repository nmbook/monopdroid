package edu.rochester.nbook.monopdroid;

public class Estate {
    // given
    private int estateId;
    private String name;
    private String icon_name;
    private int houses;
    private int money;
    private int passMoney;
    private int mortgagePrice;
    private int unmortgagePrice;
    private boolean mortgaged;
    private int price;
    private int sellHousePrice;

    public int getEstateId() {
        return this.estateId;
    }

    public String getName() {
        return this.name;
    }

    public String getIcon_name() {
        return this.icon_name;
    }

    public int getHouses() {
        return this.houses;
    }

    public int getMoney() {
        return this.money;
    }

    public int getPassMoney() {
        return this.passMoney;
    }

    public int getMortgagePrice() {
        return this.mortgagePrice;
    }

    public int getUnmortgagePrice() {
        return this.unmortgagePrice;
    }

    public boolean isMortgaged() {
        return this.mortgaged;
    }

    public int getPrice() {
        return this.price;
    }

    public int getSellHousePrice() {
        return this.sellHousePrice;
    }
}
