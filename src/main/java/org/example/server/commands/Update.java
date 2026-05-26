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

            if (newRoute == null) {

                Server.writeExecutor(
                        Codes.OK,
                        "Элемент с id " + id + " найден",
                        existingRoute,
                        clientChannel
                );

                return Codes.OK;
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
                        Codes.ERROR,
                        "Ошибка обновления в БД",
                        null,
                        clientChannel
                );

                return Codes.ERROR;
            }

            managerCollections.updateRoute(updatedRoute);

            Server.writeExecutor(
                    Codes.OK,
                    "Элемент с id " + id + " обновлен",
                    null,
                    clientChannel
            );

            return Codes.OK;

        } catch (NumberFormatException e) {

            Server.writeExecutor(
                    Codes.WARNING,
                    "ID должен быть числом",
                    null,
                    clientChannel
            );

            return Codes.WARNING;
        } catch (Exception e) {
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