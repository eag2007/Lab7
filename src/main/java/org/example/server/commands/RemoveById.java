package org.example.server.commands;

import org.example.packet.collection.Route;
import org.example.packet.collection.RouteClient;
import org.example.packet.enums.Codes;
import org.example.server.Server;
import org.example.server.interfaces.Command;
import org.example.server.logger.ServerLogger;

import java.nio.channels.SocketChannel;

import static org.example.server.Server.*;

public class RemoveById implements Command {
    public Codes executeCommand(String[] args, RouteClient value, SocketChannel clientChannel, String login, String password) {
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

            Long id = Long.parseLong(args[0]);

            Route route = managerDataBase.getRouteInDB(id, login);
            if (route == null) {

                Server.writeExecutor(
                        Codes.WARNING,
                        "Элемент с id " + id + " не найден у пользователя " + login,
                        null,
                        clientChannel
                );

                return Codes.WARNING;
            }

            long deletedId = managerDataBase.deleteRouteInDB(id);

            if (deletedId == 0) {

                Server.writeExecutor(
                        Codes.WARNING,
                        "Ошибка удаления из БД",
                        null,
                        clientChannel
                );

                return Codes.ERROR;
            }

            if (deletedId == -1) {

                Server.writeExecutor(
                        Codes.ERROR,
                        "Ошибка при удалении из БД",
                        null,
                        clientChannel
                );

                return Codes.ERROR;
            }

            if (deletedId == -3) {
                Server.writeExecutor(
                        Codes.ERROR,
                        "База данных на сервере недоступна",
                        null,
                        clientChannel
                );

                return Codes.ERROR;
            }

            boolean removed = managerCollections.removeRouteById(id);

            if (removed) {

                Server.writeExecutor(
                        Codes.OK,
                        "Элемент с id " + id + "удалён",
                        null,
                        clientChannel
                );

                return Codes.OK;
            } else {

                Server.writeExecutor(
                        Codes.WARNING,
                        "Элемент с id " + id + " не найден в коллекции",
                        null,
                        clientChannel
                );

                return Codes.WARNING;
            }

        } catch (NumberFormatException e) {

            Server.writeExecutor(
                    Codes.WARNING,
                    "ID должен быть числом",
                    null,
                    clientChannel
            );

            return Codes.WARNING;
        } catch (Exception e) {
            ServerLogger.error("Ошибка удаления: {}", e.getMessage());

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