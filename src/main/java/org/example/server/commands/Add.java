package org.example.server.commands;

import org.example.packet.collection.Route;
import org.example.packet.collection.RouteClient;
import org.example.server.Server;
import org.example.server.interfaces.Command;
import org.example.server.logger.ServerLogger;

import java.nio.channels.SocketChannel;

import static org.example.server.Server.*;

public class Add implements Command {
    public int executeCommand(String[] args, RouteClient value, SocketChannel clientChannel, String login, String password) {
        try {
            long id = managerDataBase.addRouteInDB(value, login);

            if (id == -1) {

                Server.writeExecutor(
                        400,
                        "Маршрут с таким именем уже существует",
                        null,
                        clientChannel
                );

                return 400;
            }

            if (id == -3) {
                Server.writeExecutor(
                        500,
                        "База данных на сервере недоступна",
                        null,
                        clientChannel
                );
                return 500;
            }

            Route route = managerDataBase.getRouteInDB(id, login);
            if (route != null) {
                managerCollections.addCollections(route);
            }

            Server.writeExecutor(
                    200,
                    "Объект добавлен в коллекцию с ID: " + id,
                    id,
                    clientChannel
            );

            return 200;
        } catch (Exception e) {
            ServerLogger.error("Ошибка добавления: {}", e.getMessage());

            Server.writeExecutor(
                    500,
                    "Ошибка добавления: " + e.getMessage(),
                    null,
                    clientChannel
            );

            return 500;
        }
    }
}