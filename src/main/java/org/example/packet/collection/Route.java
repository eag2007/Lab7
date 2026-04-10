package org.example.packet.collection;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.ZonedDateTime;

public class Route implements Comparable<Route>, Serializable {
    private long id;
    private String name;
    private Coordinates coordinates;
    private ZonedDateTime creationDate;
    private Location from;
    private Location to;
    private Integer distance;
    private BigDecimal price;
    private String author;

    public Route(long id, String name, Coordinates coordinates, Location from, Location to,
                 Integer distance, BigDecimal price, String author) {
        this.id = id;
        this.creationDate = ZonedDateTime.now();
        this.name = name;
        this.coordinates = coordinates;
        this.from = from;
        this.to = to;
        this.distance = distance;
        this.price = price;
        this.author = author;
    }

    public Route(long id, String name, Coordinates coordinates, ZonedDateTime creationDate, Location from, Location to,
                 Integer distance, BigDecimal price, String author) {
        this.id = id;
        this.creationDate = creationDate;
        this.name = name;
        this.coordinates = coordinates;
        this.from = from;
        this.to = to;
        this.distance = distance;
        this.price = price;
        this.author = author;
    }

    public long getId() {
        return this.id;
    }

    public ZonedDateTime getCreationDate() {
        return this.creationDate;
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

    public String getAuthor() { return this.author; }

    @Override
    public int compareTo(Route o) {
        int idCompare = Long.compare(this.id, o.id);
        if (idCompare != 0) {
            return idCompare;
        }

        int nameCompare = this.name.compareTo(o.name);
        if (nameCompare != 0) {
            return nameCompare;
        }

        int coordCompare = this.coordinates.compareTo(o.coordinates);
        if (coordCompare != 0) {
            return coordCompare;
        }

        int dateCompare = this.creationDate.compareTo(o.creationDate);
        if (dateCompare != 0) {
            return dateCompare;
        }

        if (this.from == null) {
            return -1;
        }

        if (o.from == null) {
            return 1;
        }

        int locFromCompare = this.from.compareTo(o.from);
        if (locFromCompare != 0) {
            return locFromCompare;
        }

        int locToCompare = this.to.compareTo(o.to);
        if (locToCompare != 0) {
            return locToCompare;
        }

        if (this.distance == null) {
            return -1;
        }

        if (o.distance == null) {
            return 1;
        }

        return Integer.compare(this.distance, o.distance);
    }
}