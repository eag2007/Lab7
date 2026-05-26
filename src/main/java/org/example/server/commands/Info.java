package org.example.server.commands;

import org.example.packet.collection.RouteClient;
import org.example.server.Server;
import org.example.server.interfaces.Command;
import org.example.server.logger.ServerLogger;

import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;

import static org.example.server.Server.managerCollections;

public class Info implements Command {
    public int executeCommand(String[] args, RouteClient value, SocketChannel clientChannel, String login, String password) {
        try {
            Map<String, Object> info = new HashMap<>();
            info.put("size", managerCollections.getSizeCollections());
            info.put("initTime", managerCollections.getTimeInit().toString());
            info.put("createdBy", login);
            info.put("type", "PriorityQueue<Route>");

            Server.writeExecutor(
                    200,
                    "Информация о коллекции",
                    info,
                    clientChannel
            );

            return 200;

        } catch (Exception e) {
            ServerLogger.error("Ошибка info: {}", e.getMessage());

            Server.writeExecutor(
                    500,
                    "Ошибка: " + e.getMessage(),
                    null,
                    clientChannel
            );

            return 500;
        }
    }
}