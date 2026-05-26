package org.example.server.commands;

import org.example.packet.collection.Route;
import org.example.packet.collection.RouteClient;
import org.example.server.Server;
import org.example.server.interfaces.Command;
import org.example.server.logger.ServerLogger;

import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.example.server.Server.managerCollections;

public class FilterLessThanDistance implements Command {
    public int executeCommand(String[] args, RouteClient value, SocketChannel clientChannel, String login, String password) {
        try {
            int distance = Integer.parseInt(args[0]);

            List<Route> result = managerCollections.getCollectionsRoute().stream()
                    .filter(route -> route.getDistance() != null && route.getDistance() < distance)
                    .collect(Collectors.toCollection(ArrayList::new));

            if (result.isEmpty()) {

                Server.writeExecutor(
                        400,
                        "Нет элементов с distance меньше " + distance,
                        result,
                        clientChannel
                );

                return 400;
            }

            Server.writeExecutor(
                    200,
                    "Найдено элементов: " + result.size(),
                    result,
                    clientChannel
            );

            return 200;

        } catch (Exception e) {
            ServerLogger.error("Ошибка: {}", e.getMessage());

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