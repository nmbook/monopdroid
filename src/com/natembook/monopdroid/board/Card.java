package com.natembook.monopdroid.board;

import java.util.HashMap;

/**
 * Represents a MONOPD Card object.
 * @author Nate
 *
 */
public class Card {
    public static final HashMap<String, XmlAttribute> cardAttributes = new HashMap<String, XmlAttribute>() {
        private static final long serialVersionUID = -1317407283311685448L;

        {
            this.put("title", new XmlAttribute(Card.class, "setTitle", XmlAttributeType.STRING));
            this.put("owner", new XmlAttribute(Card.class, "setOwner", XmlAttributeType.INT));
        }
    };
    
    // card object data
    private int cardId;
    private String title;
    private int owner;

    public Card(int cardId) {
        this.cardId = cardId;
    }
    
    public int getCardId() {
        return cardId;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getOwner() {
        return this.owner;
    }

    public void setOwner(int owner) {
        this.owner = owner;
    }
}
