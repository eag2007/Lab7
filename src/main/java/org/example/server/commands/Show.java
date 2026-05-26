package org.example.server.commands;

import org.example.packet.collection.Route;
import org.example.packet.collection.RouteClient;
import org.example.packet.enums.Codes;
import org.example.server.Server;
import org.example.server.interfaces.Command;
import org.example.server.logger.ServerLogger;

import java.nio.channels.SocketChannel;
import java.util.List;

import static org.example.server.Server.managerCollections;

public class Show implements Command {
    public Codes executeCommand(String[] args, RouteClient value, SocketChannel clientChannel, String login, String password) {
        try {
            List<Route> routes = managerCollections.getSortedCollections();

            if (routes.isEmpty()) {

                Server.writeExecutor(
                        Codes.OK,
                        "Коллекция пуста",
                        routes,
                        clientChannel
                );

            } else {

                Server.writeExecutor(
                        Codes.OK,
                        "Найдено элементов: " + routes.size(),
                        routes,
                        clientChannel
                );

            }

            return Codes.OK;

        } catch (Exception e) {
            ServerLogger.error("Ошибка при получении коллекции: {}", e.getMessage());

            Server.writeExecutor(
                    Codes.ERROR,
                    "Ошибка при получении коллекции: " + e.getMessage(),
                    null,
                    clientChannel
            );

            return Codes.ERROR;
        }
    }
}