package org.example.server.commands;

import org.example.packet.collection.Route;
import org.example.packet.ResponsePacket;
import org.example.packet.collection.RouteClient;
import org.example.server.Server;
import org.example.server.interfaces.Command;
import org.example.server.logger.ServerLogger;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.example.server.Server.managerCollections;
import static org.example.server.Server.writeModule;

public class FilterLessThanDistance implements Command {
    public int executeCommand(String[] args, RouteClient value, SocketChannel clientChannel, String login, String password) {
        try {
            int distance = Integer.parseInt(args[0]);

            List<Route> result = managerCollections.getCollectionsRoute().stream()
                    .filter(route -> route.getDistance() != null && route.getDistance() < distance)
                    .collect(Collectors.toCollection(ArrayList::new));

            if (result.isEmpty()) {
                ResponsePacket response = new ResponsePacket(400, "Нет элементов с distance меньше " + distance, result);
                Server.getWrite().submit(() -> {
                    try {
                        writeModule.writeResponseForClient(clientChannel, response);
                    } catch (IOException e) {
                        ServerLogger.error("Ошибка отправки {}", e.getMessage());
                    }
                });
                return 400;
            }

            ResponsePacket response = new ResponsePacket(200, "Найдено элементов: " + result.size(), result);
            Server.getWrite().submit(() -> {
                try {
                    writeModule.writeResponseForClient(clientChannel, response);
                } catch (IOException e) {
                    ServerLogger.error("Ошибка отправки {}", e.getMessage());
                }
            });
            return 200;

        } catch (Exception e) {
            ServerLogger.error("Ошибка: {}", e.getMessage());

            ResponsePacket error = new ResponsePacket(
                    500,
                    "Ошибка: " + e.getMessage(),
                    null);

            Server.getWrite().submit(() -> {
                try {
                    writeModule.writeResponseForClient(clientChannel, error);
                } catch (IOException ex) {
                    ServerLogger.error("Ошибка отправки {}", ex.getMessage());
                }
            });
            return 500;
        }
    }
}