package org.example.server.commands;

import org.example.packet.collection.RouteClient;
import org.example.packet.enums.Codes;
import org.example.server.Server;
import org.example.server.interfaces.Command;
import org.example.server.logger.ServerLogger;

import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;

import static org.example.server.Server.managerCollections;

public class Info implements Command {
    public Codes executeCommand(String[] args, RouteClient value, SocketChannel clientChannel, String login, String password) {
        try {
            Map<String, Object> info = new HashMap<>();
            info.put("size", managerCollections.getSizeCollections());
            info.put("initTime", managerCollections.getTimeInit().toString());
            info.put("createdBy", login);
            info.put("type", "PriorityQueue<Route>");

            Server.writeExecutor(
                    Codes.OK,
                    "Информация о коллекции",
                    info,
                    clientChannel
            );

            return Codes.OK;

        } catch (Exception e) {
            ServerLogger.error("Ошибка info: {}", e.getMessage());

            Server.writeExecutor(
                    Codes.ERROR,
                    "Ошибка: " + e.getMessage(),
                    null,
                    clientChannel
            );

            return Codes.ERROR;
        }
    }
}