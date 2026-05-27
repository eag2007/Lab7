package org.example.server.commands;

import org.example.packet.collection.Route;
import org.example.packet.collection.RouteClient;
import org.example.packet.enums.Codes;
import org.example.server.Server;
import org.example.server.interfaces.Command;
import org.example.server.logger.ServerLogger;

import java.nio.channels.SocketChannel;

import static org.example.server.Server.*;

public class RemoveFirst implements Command {
    public Codes executeCommand(String[] args, RouteClient values, SocketChannel clientChannel, String login, String password) {
        try {
            Route route = managerCollections.getCollectionsRoute().peek();

            if (route == null) {

                Server.writeExecutor(
                        Codes.WARNING,
                        "Коллекция пуста",
                        null,
                        clientChannel
                );

                return Codes.WARNING;
            }

            if (!route.getAuthor().equals(login)) {

                Server.writeExecutor(
                        Codes.WARNING,
                        "Первый элемент принадлежит другому пользователю",
                        null,
                        clientChannel
                );

                return Codes.WARNING;
            }

            long id = route.getId();

            long deletedId = managerDataBase.deleteRouteInDB(id, login);

            if (deletedId == 0) {

                Server.writeExecutor(
                        Codes.ERROR,
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

            managerCollections.removeRouteById(id);

            Server.writeExecutor(
                    Codes.OK,
                    "Объект удалён с id = " + id,
                    id,
                    clientChannel
            );

            managerPush.sendPushToSubscribes("Удалён первый объект в коллекции. Пользователем: " +  login);

            return Codes.OK;

        } catch (Exception e) {
            ServerLogger.error("Ошибка удаления первого элемента: {}", e.getMessage());

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