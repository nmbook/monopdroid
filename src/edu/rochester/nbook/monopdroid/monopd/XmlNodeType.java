package edu.rochester.nbook.monopdroid.monopd;

/**
 * Node type used for parsing of XML.
 * 
 * @author Nate
 */
public enum XmlNodeType {
    NONE,
    META_ATLANTIC, METASERVER, SERVERGAMELIST, GAME,
    MONOPD, SERVER, CLIENT, PLAYERUPDATE, MSG,
    GAMEUPDATE, UPDATEPLAYERLIST, PLAYER, CONFIGUPDATE,
    OPTION, ESTATEUPDATE, ESTATEGROUPUPDATE, DISPLAY,
    UPDATEGAMELIST, DELETEPLAYER, BUTTON, AUCTIONUPDATE
}
