package com.elvarg.game.model;

public class HouseFurniture {
    public int objectId;
    public int x, y, z;
    public int rotation;

    public HouseFurniture(int objectId, int x, int y, int z, int rotation) {
        this.objectId = objectId;
        this.x = x;
        this.y = y;
        this.z = z;
        this.rotation = rotation;
    }
}// A list to hold all the objects this player has built
private List<HouseFurniture> myHouse = new ArrayList<>();

public List<HouseFurniture> getHouse() {
    return myHouse;
}