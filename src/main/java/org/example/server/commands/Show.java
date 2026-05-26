package org.example.server.commands;

import org.example.packet.collection.Route;
import org.example.packet.collection.RouteClient;
import org.example.server.Server;
import org.example.server.interfaces.Command;
import org.example.server.logger.ServerLogger;

import java.nio.channels.SocketChannel;
import java.util.List;

import static org.example.server.Server.managerCollections;

public class Show implements Command {
    public int executeCommand(String[] args, RouteClient value, SocketChannel clientChannel, String login, String password) {
        try {
            List<Route> routes = managerCollections.getSortedCollections();

            if (routes.isEmpty()) {

                Server.writeExecutor(
                        200,
                        "Коллекция пуста",
                        routes,
                        clientChannel
                );

            } else {

                Server.writeExecutor(
                        200,
                        "Найдено элементов: " + routes.size(),
                        routes,
                        clientChannel
                );

            }

            return 200;

        } catch (Exception e) {
            ServerLogger.error("Ошибка при получении коллекции: {}", e.getMessage());

            Server.writeExecutor(
                    500,
                    "Ошибка при получении коллекции: " + e.getMessage(),
                    null,
                    clientChannel
            );

            return 500;
        }
    }
}