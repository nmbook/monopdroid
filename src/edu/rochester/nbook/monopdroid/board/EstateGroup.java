package edu.rochester.nbook.monopdroid.board;

public class EstateGroup {
    // estate data
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
