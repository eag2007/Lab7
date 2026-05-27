package org.example.server.commands;

import org.example.packet.collection.Route;
import org.example.packet.collection.RouteClient;
import org.example.packet.enums.Codes;
import org.example.server.Server;
import org.example.server.interfaces.Command;
import org.example.server.logger.ServerLogger;

import java.nio.channels.SocketChannel;

import static org.example.server.Server.*;

public class Add implements Command {
    public Codes executeCommand(String[] args, RouteClient value, SocketChannel clientChannel, String login, String password) {
        try {
            Route route = managerDataBase.addRouteInDBFull(value, login);

            if (route == null) {

                Server.writeExecutor(
                        Codes.WARNING,
                        "Маршрут с таким именем уже существует",
                        null,
                        clientChannel
                );

                return Codes.WARNING;
            }

            managerCollections.addCollections(route);

            Server.writeExecutor(
                    Codes.OK,
                    "Объект добавлен в коллекцию с ID: " + route.getId(),
                    route.getId(),
                    clientChannel
            );


            managerPush.sendPushToSubscribes("Добавлен новый объект в коллекцию. Пользователем: " +  login);

            return Codes.OK;

        } catch (RuntimeException e) {
            if ("DB_UNAVAILABLE".equals(e.getMessage())) {

                Server.writeExecutor(
                        Codes.ERROR,
                        "База данных на сервере недоступна",
                        null,
                        clientChannel
                );

                return Codes.ERROR;
            }

            ServerLogger.error("Ошибка добавления: {}", e.getMessage());

            Server.writeExecutor(
                    Codes.ERROR,
                    "Ошибка добавления: " + e.getMessage(),
                    null,
                    clientChannel
            );

            return Codes.ERROR;
        }
    }
}