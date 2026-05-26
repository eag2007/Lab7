package org.example.server.commands;

import org.example.packet.collection.Route;
import org.example.packet.collection.RouteClient;
import org.example.packet.enums.Codes;
import org.example.server.Server;
import org.example.server.interfaces.Command;
import org.example.server.logger.ServerLogger;

import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.example.server.Server.managerCollections;

public class FilterLessThanDistance implements Command {
    public Codes executeCommand(String[] args, RouteClient value, SocketChannel clientChannel, String login, String password) {
        try {
            int distance = Integer.parseInt(args[0]);

            List<Route> result = managerCollections.getCollectionsRoute().stream()
                    .filter(route -> route.getDistance() != null && route.getDistance() < distance)
                    .collect(Collectors.toCollection(ArrayList::new));

            if (result.isEmpty()) {

                Server.writeExecutor(
                        Codes.WARNING,
                        "Нет элементов с distance меньше " + distance,
                        result,
                        clientChannel
                );

                return Codes.WARNING;
            }

            Server.writeExecutor(
                    Codes.OK,
                    "Найдено элементов: " + result.size(),
                    result,
                    clientChannel
            );

            return Codes.OK;

        } catch (Exception e) {
            ServerLogger.error("Ошибка: {}", e.getMessage());

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