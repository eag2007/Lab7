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
            long id = managerDataBase.addRouteInDB(value, login);

            if (id == -1) {

                Server.writeExecutor(
                        Codes.WARNING,
                        "Маршрут с таким именем уже существует",
                        null,
                        clientChannel
                );

                return Codes.WARNING;
            }

            if (id == -3) {
                Server.writeExecutor(
                        Codes.ERROR,
                        "База данных на сервере недоступна",
                        null,
                        clientChannel
                );
                return Codes.ERROR;
            }

            Route route = managerDataBase.getRouteInDB(id, login);
            if (route != null) {
                managerCollections.addCollections(route);
            }

            Server.writeExecutor(
                    Codes.OK,
                    "Объект добавлен в коллекцию с ID: " + id,
                    id,
                    clientChannel
            );

            return Codes.OK;
        } catch (Exception e) {
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