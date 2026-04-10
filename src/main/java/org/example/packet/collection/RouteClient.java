package org.example.packet.collection;

import java.io.Serializable;
import java.math.BigDecimal;

public class RouteClient implements Serializable {
    private String name;
    private Coordinates coordinates;
    private Location from;
    private Location to;
    private Integer distance;
    private BigDecimal price;

    public RouteClient(String name, Coordinates coordinates, Location from, Location to,
                 Integer distance, BigDecimal price) {
        this.name = name;
        this.coordinates = coordinates;
        this.from = from;
        this.to = to;
        this.distance = distance;
        this.price = price;
    }

    public String getName() {
        return this.name;
    }

    public Coordinates getCoordinates() {
        return this.coordinates;
    }

    public Location getFrom() {
        return this.from;
    }

    public Location getTo() {
        return this.to;
    }

    public Integer getDistance() {
        return this.distance;
    }

    public BigDecimal getPrice() {
        return this.price;
    }
}