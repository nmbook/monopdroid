package com.natembook.monopdroid.board;

import java.util.HashMap;

public class EstateGroup {
    public static final HashMap<String, XmlAttribute> estateGroupAttributes = new HashMap<String, XmlAttribute>() {
        private static final long serialVersionUID = -4145245059753235694L;

        {
            this.put("name", new XmlAttribute(EstateGroup.class, "setName", XmlAttributeType.STRING));
        }
    };
    
    // estate group object data
    private int estateGroupId;
    private String name;

    public EstateGroup(int estateGroupId) {
        this.estateGroupId = estateGroupId;
    }

    public int getEstateGroupId() {
        return this.estateGroupId;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    @Override
    public String toString() {
        return this.name + " (id: " + this.estateGroupId + ")";
    }
}
