package edu.rochester.nbook.monopdroid.board;

import java.util.HashMap;

public class Player {
    public static final HashMap<String, XmlAttribute> playerAttributes = new HashMap<String, XmlAttribute>() {
        private static final long serialVersionUID = 1431923100451372984L;
    
        {
            this.put("name", new XmlAttribute(Player.class, "setName", XmlAttributeType.STRING));
            this.put("host", new XmlAttribute(Player.class, "setHost", XmlAttributeType.STRING));
            this.put("master", new XmlAttribute(Player.class, "setMaster", XmlAttributeType.BOOLEAN));
            this.put("money", new XmlAttribute(Player.class, "setMoney", XmlAttributeType.INT));
            this.put("doublecount", new XmlAttribute(Player.class, "setDoubleCount", XmlAttributeType.INT));
            this.put("jailcount", new XmlAttribute(Player.class, "setJailCount", XmlAttributeType.INT));
            this.put("bankrupt", new XmlAttribute(Player.class, "setBankrupt", XmlAttributeType.BOOLEAN));
            this.put("jailed", new XmlAttribute(Player.class, "setJailed", XmlAttributeType.BOOLEAN));
            this.put("hasturn", new XmlAttribute(Player.class, "setHasTurn", XmlAttributeType.BOOLEAN));
            this.put("spectator", new XmlAttribute(Player.class, "setSpectator", XmlAttributeType.BOOLEAN));
            this.put("can_roll", new XmlAttribute(Player.class, "setCanRoll", XmlAttributeType.BOOLEAN));
            this.put("canrollagain", new XmlAttribute(Player.class, "setCanRollAgain", XmlAttributeType.BOOLEAN));
            this.put("can_buyestate", new XmlAttribute(Player.class, "setCanBuyEstate", XmlAttributeType.BOOLEAN));
            this.put("canauction", new XmlAttribute(Player.class, "setCanAuction", XmlAttributeType.BOOLEAN));
            this.put("canusecard", new XmlAttribute(Player.class, "setCanUseCard", XmlAttributeType.BOOLEAN));
            this.put("hasdebt", new XmlAttribute(Player.class, "setHasDebt", XmlAttributeType.BOOLEAN));
            this.put("location", new XmlAttribute(Player.class, "setLocation", XmlAttributeType.INT));
            this.put("directmove", new XmlAttribute(Player.class, "setDirectMove", XmlAttributeType.BOOLEAN));
            this.put("game", new XmlAttribute(Player.class, "setGameId", XmlAttributeType.INT));
            this.put("cookie", new XmlAttribute(Player.class, null, XmlAttributeType.STRING));
            this.put("image", new XmlAttribute(Player.class, null, XmlAttributeType.STRING));
        }
    };

    // player object data
    private String name;
    private String host;
    private String cookie;
    private int playerId;
    private int gameId;
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
    
    private boolean grayed;
    
    public Player(int playerId) {
        this.playerId = playerId;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
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

    public int getGameId() {
        return gameId;
    }

    public void setGameId(int gameId) {
        this.gameId = gameId;
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
    
    @Override
    public String toString() {
        return name + " (" + playerId + ")";
    }

    public int getLastLocation() {
        return lastLocation;
    }

    public boolean isGrayed() {
        return grayed;
    }
    
    public void setGrayed(boolean grayed) {
        this.grayed = grayed;
    }
}
