package edu.rochester.nbook.monopdroid;

public class Player {
    // player data
    private String nick;
    private String host;
    private String cookie;
    private int playerId;
    private int money;
    private int doubleCount;
    private int jailCount;
    private int location;
    private int lastLocation;
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
    
    // non-attribute data
    private int drawColor;
    private int drawLocation;

    public Player(int playerId) {
        this.playerId = playerId;
    }

    public String getNick() {
        return this.nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public int getMoney() {
        return this.money;
    }

    public void setMoney(int money) {
        this.money = money;
    }

    public int getDoubleCount() {
        return this.doubleCount;
    }

    public void setDoubleCount(int doubleCount) {
        this.doubleCount = doubleCount;
    }

    public int getJailCount() {
        return this.jailCount;
    }

    public void setJailCount(int jailCount) {
        this.jailCount = jailCount;
    }

    public boolean isJailed() {
        return this.jailed;
    }

    public void setJailed(boolean jailed) {
        this.jailed = jailed;
    }

    public boolean isTurn() {
        return this.hasTurn;
    }

    public void setHasTurn(boolean hasTurn) {
        this.hasTurn = hasTurn;
    }

    public boolean isSpectator() {
        return this.spectator;
    }

    public void setSpectator(boolean spectator) {
        this.spectator = spectator;
    }

    public boolean isInDebt() {
        return this.hasDebt;
    }

    public void setHasDebt(boolean hasDebt) {
        this.hasDebt = hasDebt;
    }

    public boolean canUseCard() {
        return this.canUseCard;
    }

    public void setCanUseCard(boolean canUseCard) {
        this.canUseCard = canUseCard;
    }

    public String getHost() {
        return this.host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public boolean isMaster() {
        return this.master;
    }

    public void setMaster(boolean master) {
        this.master = master;
    }

    public int getPlayerId() {
        return this.playerId;
    }

    public String getCookie() {
        return this.cookie;
    }

    public void setCookie(String cookie) {
        this.cookie = cookie;
    }

    public boolean isBankrupt() {
        return this.bankrupt;
    }

    public void setBankrupt(boolean bankrupt) {
        this.bankrupt = bankrupt;
    }

    public boolean canRoll() {
        return this.canRoll;
    }

    public void setCanRoll(boolean canRoll) {
        this.canRoll = canRoll;
    }

    public boolean canRollAgain() {
        return this.canRollAgain;
    }

    public void setCanRollAgain(boolean canRollAgain) {
        this.canRollAgain = canRollAgain;
    }

    public boolean canBuyEstate() {
        return this.canBuyEstate;
    }

    public void setCanBuyEstate(boolean canBuyEstate) {
        this.canBuyEstate = canBuyEstate;
    }

    public boolean canAuction() {
        return this.canAuction;
    }

    public void setCanAuction(boolean canAuction) {
        this.canAuction = canAuction;
    }

    public int getLocation() {
        return this.location;
    }

    public void setLocation(int location) {
        this.lastLocation = this.location;
        this.location = location;
    }

    public boolean getDirectMove() {
        return this.directMove;
    }

    public void setDirectMove(boolean directMove) {
        this.directMove = directMove;
    }

    public int getDrawColor() {
        return drawColor;
    }

    public void setDrawColor(int color) {
        this.drawColor = color;
    }

    public int getDrawLocation() {
        return drawLocation;
    }

    public void setDrawLocation(int location) {
        this.drawLocation = location;
    }
    
    @Override
    public String toString() {
        return this.nick + " (id: " + this.playerId + ")";
    }

    public int getLastLocation() {
        return lastLocation;
    }
}
