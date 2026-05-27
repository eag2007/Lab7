package org.example.server.commands;

import org.example.packet.collection.Route;
import org.example.packet.collection.RouteClient;
import org.example.packet.enums.Codes;
import org.example.server.Server;
import org.example.server.interfaces.Command;
import org.example.server.logger.ServerLogger;

import java.nio.channels.SocketChannel;
import java.util.Comparator;

import static org.example.server.Server.*;

public class AddIfMax implements Command {
    public Codes executeCommand(String[] args, RouteClient value, SocketChannel clientChannel, String login, String password) {
        try {
            if (value == null) {

                Server.writeExecutor(
                        Codes.WARNING,
                        "Не переданы данные элемента",
                        null,
                        clientChannel
                );

                return Codes.WARNING;
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
                            Codes.WARNING,
                            "Элемент не добавлен (не превышает максимальный)",
                            null,
                            clientChannel
                    );

                    return Codes.WARNING;
                }
            }

            Route newRoute = managerDataBase.addRouteInDBFull(value, login);

            if (newRoute == null) {

                Server.writeExecutor(
                        Codes.WARNING,
                        "Маршрут с таким именем уже существует",
                        null,
                        clientChannel
                );

                return Codes.WARNING;
            }

            managerCollections.addCollections(newRoute);

            Server.writeExecutor(
                    Codes.OK,
                    "Элемент добавлен с ID: " + newRoute.getId(),
                    newRoute.getId(),
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

            ServerLogger.error("Ошибка при добавлении: {}", e.getMessage());

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