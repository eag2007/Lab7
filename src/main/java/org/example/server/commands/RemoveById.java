package org.example.server.commands;

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

            long id = Long.parseLong(args[0]);

            long deletedId = managerDataBase.deleteRouteInDB(id, login);

            if (deletedId == 0) {

                Server.writeExecutor(
                        Codes.WARNING,
                        "Элемент с id " + id + " не найден у пользователя " + login,
                        null,
                        clientChannel
                );

                return Codes.WARNING;
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

            managerCollections.removeRouteById(id);

            Server.writeExecutor(
                    Codes.OK,
                    "Элемент с id " + id + " удалён",
                    null,
                    clientChannel
            );

            managerPush.sendPushToSubscribes("Удалён объект в коллекцию. Пользователем: " +  login);

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