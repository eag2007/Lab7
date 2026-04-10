package org.example.packet.collection;

import java.io.Serializable;

public class Location implements Comparable<Location>, Serializable {
    private float x;
    private Double y;
    private int z;

    @Override
    public int compareTo(Location o) {
        int xCompare = Float.compare(this.x, o.x);
        if (xCompare != 0) {
            return xCompare;
        }

        int yCompare = Double.compare(this.y, o.y);
        if (yCompare != 0) {
            return yCompare;
        }

        return Integer.compare(this.z, o.z);
    }

    public Location(float x, Double y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public float getX() {
        return this.x;
    }

    public Double getY() {
        return this.y;
    }

    public int getZ() {
        return this.z;
    }
}