package org.example.server.commands;

import org.example.packet.collection.Route;
import org.example.packet.collection.RouteClient;
import org.example.server.Server;
import org.example.server.interfaces.Command;
import org.example.server.logger.ServerLogger;

import java.nio.channels.SocketChannel;

import static org.example.server.Server.*;

public class RemoveById implements Command {
    public int executeCommand(String[] args, RouteClient value, SocketChannel clientChannel, String login, String password) {
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

            Long id = Long.parseLong(args[0]);

            Route route = managerDataBase.getRouteInDB(id, login);
            if (route == null) {

                Server.writeExecutor(
                        400,
                        "Элемент с id " + id + " не найден у пользователя " + login,
                        null,
                        clientChannel
                );

                return 400;
            }

            long deletedId = managerDataBase.deleteRouteInDB(id);

            if (deletedId == 0) {

                Server.writeExecutor(
                        500,
                        "Ошибка удаления из БД",
                        null,
                        clientChannel
                );

                return 500;
            }

            if (deletedId == -1) {

                Server.writeExecutor(
                        500,
                        "Ошибка при удалении из БД",
                        null,
                        clientChannel
                );

                return 500;
            }

            if (deletedId == -3) {
                Server.writeExecutor(
                        500,
                        "База данных на сервере недоступна",
                        null,
                        clientChannel
                );

                return 500;
            }

            boolean removed = managerCollections.removeRouteById(id);

            if (removed) {

                Server.writeExecutor(
                        200,
                        "Элемент с id " + id + "удалён",
                        null,
                        clientChannel
                );

                return 200;
            } else {

                Server.writeExecutor(
                        400,
                        "Элемент с id " + id + " не найден в коллекции",
                        null,
                        clientChannel
                );

                return 400;
            }

        } catch (NumberFormatException e) {

            Server.writeExecutor(
                    400,
                    "ID должен быть числом",
                    null,
                    clientChannel
            );

            return 400;
        } catch (Exception e) {
            ServerLogger.error("Ошибка удаления: {}", e.getMessage());

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