package org.example.packet.collection;

import java.io.Serializable;

public class Coordinates implements Comparable<Coordinates>, Serializable {
    private long x;
    private long y;

    public Coordinates(long x, long y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public int compareTo(Coordinates o) {
        int xCompare = Long.compare(this.x, o.x);
        if (xCompare != 0) {
            return xCompare;
        }
        return Long.compare(this.y, o.y);
    }

    public long getX() {
        return this.x;
    }

    public long getY() {
        return this.y;
    }
}