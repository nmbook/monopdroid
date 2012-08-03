package edu.rochester.nbook.monopdroid;

public class Player {
	private String nick;
	private String host;
	private String cookie;
	private int playerId;
	private int money;
	private int doubleCount;
	private int jailCount;
	private int location;
	private boolean master;
	private boolean bankrupt;
	private boolean jailed;
	private boolean hasTurn;
	private boolean spectator;
	private boolean canRoll;
	private boolean canRollAgain;
	private boolean canBuyEstate;
	private boolean canAuction;
	private boolean hasDebt;
	private boolean canUseCard;
	private boolean directMove;
	
	public Player(int playerId) {
		this.playerId = playerId;
	}
	
	public String getNick() {
		return nick;
	}
	
	public void setNick(String nick) {
		this.nick = nick;
	}
	
	public int getMoney() {
		return money;
	}
	
	public void setMoney(int money) {
		this.money = money;
	}
	
	public int getDoubleCount() {
		return doubleCount;
	}
	
	public void setDoubleCount(int doubleCount) {
		this.doubleCount = doubleCount;
	}
	
	public int getJailCount() {
		return jailCount;
	}
	
	public void setJailCount(int jailCount) {
		this.jailCount = jailCount;
	}
	
	public boolean isJailed() {
		return jailed;
	}
	
	public void setJailed(boolean jailed) {
		this.jailed = jailed;
	}
	
	public boolean isTurn() {
		return hasTurn;
	}
	
	public void setHasTurn(boolean hasTurn) {
		this.hasTurn = hasTurn;
	}
	
	public boolean isSpectator() {
		return spectator;
	}
	
	public void setSpectator(boolean spectator) {
		this.spectator = spectator;
	}
	
	public boolean isInDebt() {
		return hasDebt;
	}
	
	public void setHasDebt(boolean hasDebt) {
		this.hasDebt = hasDebt;
	}
	
	public boolean canUseCard() {
		return canUseCard;
	}
	
	public void setCanUseCard(boolean canUseCard) {
		this.canUseCard = canUseCard;
	}
	
	public String getHost() {
		return host;
	}
	
	public void setHost(String host) {
		this.host = host;
	}
	
	public boolean isMaster() {
		return master;
	}
	
	public void setMaster(boolean master) {
		this.master = master;
	}
	
	public int getPlayerId() {
		return playerId;
	}
	
	public String getCookie() {
		return cookie;
	}
	
	public void setCookie(String cookie) {
		this.cookie = cookie;
	}
	
	public boolean isBankrupt() {
		return bankrupt;
	}
	
	public void setBankrupt(boolean bankrupt) {
		this.bankrupt = bankrupt;
	}
	
	public boolean canRoll() {
		return canRoll;
	}
	
	public void setRoll(boolean canRoll) {
		this.canRoll = canRoll;
	}
	
	public boolean canRollAgain() {
		return canRollAgain;
	}
	
	public void setRollAgain(boolean canRollAgain) {
		this.canRollAgain = canRollAgain;
	}
	
	public boolean canBuyEstate() {
		return canBuyEstate;
	}
	
	public void setBuyEstate(boolean canBuyEstate) {
		this.canBuyEstate = canBuyEstate;
	}
	
	public boolean canAuction() {
		return canAuction;
	}
	
	public void setAuction(boolean canAuction) {
		this.canAuction = canAuction;
	}
	
	public int getLocation() {
		return location;
	}
	
	public void setLocation(int location) {
		this.location = location;
	}
	
	public boolean getDirectMove() {
		return directMove;
	}

	public void setDirectMove(boolean directMove) {
		this.directMove = directMove;
	}
}
