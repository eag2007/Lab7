package org.example.server.managers;

import org.example.packet.collection.Route;
import org.example.server.logger.ServerLogger;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ManagerCollections {
    private PriorityQueue<Route> collectionsRoute;
    private ZonedDateTime timeInit;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    public ManagerCollections() {
        this.collectionsRoute = new PriorityQueue<>();
        this.timeInit = ZonedDateTime.now();
    }

    public void addCollections(Route element) {
        lock.writeLock().lock();
        try {
            this.collectionsRoute.add(element);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void clearCollections(String login) {
        lock.writeLock().lock();
        try {
            this.collectionsRoute.removeIf(route -> route.getAuthor().equals(login));
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void removeAllByDistanceCollections(PriorityQueue<Route> routes) {
        lock.writeLock().lock();
        try {
            this.collectionsRoute = routes;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public List<Route> getSortedCollections() {
        lock.readLock().lock();
        try {
            List<Route> sorted = new ArrayList<>(collectionsRoute);
            sorted.sort(Comparator.naturalOrder());
            return sorted;
        } finally {
            lock.readLock().unlock();
        }
    }

    public PriorityQueue<Route> getCollectionsRoute() {
        lock.readLock().lock();
        try {
            return this.collectionsRoute;
        } finally {
            lock.readLock().unlock();
        }
    }

    public int getSizeCollections() {
        lock.readLock().lock();
        try {
            return this.collectionsRoute.size();
        } finally {
            lock.readLock().unlock();
        }
    }

    public ZonedDateTime getTimeInit() {
        lock.readLock().lock();
        try {
            return this.timeInit;
        } finally {
            lock.readLock().unlock();
        }
    }

    public boolean removeRouteById(long id) {
        lock.writeLock().lock();
        try {
            return this.collectionsRoute.removeIf(route -> route.getId() == id);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public boolean updateRoute(Route newRoute) {
        lock.writeLock().lock();
        try {
            this.collectionsRoute.removeIf(route -> route.getId() == newRoute.getId());
            return this.collectionsRoute.add(newRoute);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void loadAllRoutes(PriorityQueue<Route> routes) {
        lock.writeLock().lock();
        try {
            this.collectionsRoute.clear();
            this.collectionsRoute.addAll(routes);
            ServerLogger.info("Загружено {} маршрутов в коллекцию из БД", routes.size());
        } finally {
            lock.writeLock().unlock();
        }
    }
}