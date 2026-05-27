package org.example.server.managers;

import java.util.concurrent.ConcurrentHashMap;

public class ManagerPush {
    private final ConcurrentHashMap<String, Boolean> userSubscribes = new ConcurrentHashMap<>();

    public void addSubscribe(String login, boolean flag) {
        userSubscribes.put(login, flag);
    }

    public boolean deleteSubscribe(String login, boolean flag) {
        if (userSubscribes.containsKey(login)) {
            userSubscribes.remove(login);
            return true;
        }
        return false;
    }

    public ConcurrentHashMap<String, Boolean> getUserSubscribes() {
        return new ConcurrentHashMap<>(userSubscribes);
    }
}
