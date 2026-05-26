package org.example.server.commands;

import org.example.packet.collection.Route;
import org.example.packet.collection.RouteClient;
import org.example.server.Server;
import org.example.server.interfaces.Command;
import org.example.server.logger.ServerLogger;

import java.nio.channels.SocketChannel;
import java.util.Comparator;

import static org.example.server.Server.*;

public class AddIfMax implements Command {
    public int executeCommand(String[] args, RouteClient value, SocketChannel clientChannel, String login, String password) {
        try {
            if (value == null) {

                Server.writeExecutor(
                        400,
                        "Не переданы данные элемента",
                        null,
                        clientChannel
                )
                ;
                return 400;
            }

            if (!managerCollections.getCollectionsRoute().isEmpty()) {
                Route maxRoute = managerCollections.getCollectionsRoute().stream()
                        .max(Comparator.naturalOrder())
                        .orElse(null);

                Route tempRoute = new Route(
                        0,
                        value.getName(),
                        value.getCoordinates(),
                        value.getFrom(),
                        value.getTo(),
                        value.getDistance(),
                        value.getPrice(),
                        login
                );

                if (maxRoute != null && tempRoute.compareTo(maxRoute) <= 0) {

                    Server.writeExecutor(
                            400,
                            "Элемент не добавлен (не превышает максимальный)",
                            null,
                            clientChannel
                    );

                    return 400;
                }
            }

            long id = managerDataBase.addRouteInDB(value, login);

            if (id == -3) {

                Server.writeExecutor(
                        500,
                        "База данных на сервере недоступна",
                        null,
                        clientChannel
                );

                return 500;
            }
            if (id == -1) {

                Server.writeExecutor(
                        400,
                        "Маршрут с таким именем уже существует",
                        null,
                        clientChannel
                );

                return 400;
            }

            Route newRoute = new Route(
                    id,
                    value.getName(),
                    value.getCoordinates(),
                    value.getFrom(),
                    value.getTo(),
                    value.getDistance(),
                    value.getPrice(),
                    login
            );
            managerCollections.addCollections(newRoute);

            Server.writeExecutor(
                    200,
                    "Элемент добавлен с ID: " + id,
                    id,
                    clientChannel
            );

            return 200;

        } catch (Exception e) {
            ServerLogger.error("Ошибка при добавлении: {}", e.getMessage());

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