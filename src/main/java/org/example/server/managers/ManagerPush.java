package org.example.server.managers;

import org.example.packet.enums.Codes;
import org.example.server.Server;

import java.nio.channels.SocketChannel;
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

    public void sendPushToSubscribes(String message) {
        if (userSubscribes.isEmpty()) {
            return;
        }

        for (String login : userSubscribes.keySet()) {
            SocketChannel channel = Server.getLoginToChannel().get(login);
            if (channel != null && channel.isOpen()) {
                Server.writeExecutor(
                        Codes.PUSH,
                        message,
                        null,
                        channel
                );
            }
        }
    }
}
