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
		return estateId;
	}
	public String getName() {
		return name;
	}
	public String getIcon_name() {
		return icon_name;
	}
	public int getHouses() {
		return houses;
	}
	public int getMoney() {
		return money;
	}
	public int getPassMoney() {
		return passMoney;
	}
	public int getMortgagePrice() {
		return mortgagePrice;
	}
	public int getUnmortgagePrice() {
		return unmortgagePrice;
	}
	public boolean isMortgaged() {
		return mortgaged;
	}
	public int getPrice() {
		return price;
	}
	public int getSellHousePrice() {
		return sellHousePrice;
	}
}
