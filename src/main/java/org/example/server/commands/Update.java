package org.example.server.commands;

import org.example.packet.collection.Route;
import org.example.packet.collection.RouteClient;
import org.example.packet.enums.Codes;
import org.example.server.Server;
import org.example.server.interfaces.Command;
import org.example.server.logger.ServerLogger;

import java.nio.channels.SocketChannel;

import static org.example.server.Server.*;

public class Update implements Command {
    public Codes executeCommand(String[] args, RouteClient newRoute, SocketChannel clientChannel, String login, String password) {
        try {
            if (args == null || args.length < 1) {

                Server.writeExecutor(
                        Codes.WARNING,
                        "Не указан ID",
                        null,
                        clientChannel
                );

                return Codes.WARNING;
            }

            long id = Long.parseLong(args[0]);

            if (newRoute == null) {
                Route existingRoute = managerDataBase.getRouteInDB(id, login);

                if (existingRoute == null) {

                    Server.writeExecutor(
                            Codes.WARNING,
                            "Элемент с id " + id + " не найден у пользователя " + login,
                            null,
                            clientChannel
                    );

                    return Codes.WARNING;
                }

                Server.writeExecutor(
                        Codes.OK,
                        "Элемент с id " + id + " найден",
                        existingRoute,
                        clientChannel
                );

                return Codes.OK;
            }

            Route updatedRoute = managerDataBase.updateRouteInDBFull(id, newRoute, login);

            if (updatedRoute == null) {

                Server.writeExecutor(
                        Codes.WARNING,
                        "Элемент с id " + id + " не найден у пользователя " + login,
                        null,
                        clientChannel
                );

                return Codes.WARNING;
            }

            managerCollections.updateRoute(updatedRoute);

            Server.writeExecutor(
                    Codes.OK,
                    "Элемент с id " + id + " обновлен",
                    null,
                    clientChannel
            );

            managerPush.sendPushToSubscribes("Обновлён объект в коллекции. Пользователем: " +  login);

            return Codes.OK;

        } catch (NumberFormatException e) {

            Server.writeExecutor(
                    Codes.WARNING,
                    "ID должен быть числом",
                    null,
                    clientChannel
            );

            return Codes.WARNING;

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

            ServerLogger.error("Ошибка в update: {}", e.getMessage());

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