package org.example.server.commands;

import org.example.packet.collection.Route;
import org.example.packet.ResponsePacket;
import org.example.packet.collection.RouteClient;
import org.example.server.Server;
import org.example.server.interfaces.Command;
import org.example.server.logger.ServerLogger;

import java.io.IOException;
import java.nio.channels.SocketChannel;

import static org.example.server.Server.*;

public class Update implements Command {
    public int executeCommand(String[] args, RouteClient newRoute, SocketChannel clientChannel, String login, String password) {
        try {
            if (args == null || args.length < 1) {

                Server.writeExecutor(
                        400,
                        "Не указан ID",
                        null,
                        clientChannel
                );

                return 400;
            }

            long id = Long.parseLong(args[0]);

            Route existingRoute = managerDataBase.getRouteInDB(id, login);

            if (existingRoute == null) {

                Server.writeExecutor(
                        400,
                        "Элемент с id " + id + " не найден у пользователя " + login,
                        null,
                        clientChannel
                );

                return 400;
            }

            if (newRoute == null) {

                Server.writeExecutor(
                        200,
                        "Элемент с id " + id + " найден",
                        existingRoute,
                        clientChannel
                );

                return 200;
            }

            Route updatedRoute = new Route(
                    id,
                    newRoute.getName(),
                    newRoute.getCoordinates(),
                    existingRoute.getCreationDate(),
                    newRoute.getFrom(),
                    newRoute.getTo(),
                    newRoute.getDistance(),
                    newRoute.getPrice(),
                    login
            );

            boolean updated = managerDataBase.updateRouteInDB(updatedRoute, login);

            if (!updated) {

                Server.writeExecutor(
                        500,
                        "Ошибка обновления в БД",
                        null,
                        clientChannel
                );

                return 500;
            }

            managerCollections.updateRoute(updatedRoute);

            Server.writeExecutor(
                    200,
                    "Элемент с id " + id + " обновлен",
                    null,
                    clientChannel
            );

            return 200;

        } catch (NumberFormatException e) {

            Server.writeExecutor(
                    400,
                    "ID должен быть числом",
                    null,
                    clientChannel
            );

            return 400;
        } catch (Exception e) {
            ServerLogger.error("Ошибка в update: {}", e.getMessage());

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