package org.example.server.commands;

import org.example.packet.collection.RouteClient;
import org.example.server.Server;
import org.example.server.interfaces.Command;
import org.example.server.logger.ServerLogger;

import java.nio.channels.SocketChannel;

import static org.example.server.Server.*;

public class Clear implements Command {
    public int executeCommand(String[] args, RouteClient value, SocketChannel clientChannel, String login, String passsword) {
        try {
            managerCollections.clearCollections(login);
            int flag = managerDataBase.clearRoutesInDB(login);

            if (flag >= 0) {

                Server.writeExecutor(
                        200,
                        "Коллекция очищена",
                        null,
                        clientChannel
                );

                return 200;
            }

            if (flag == -3) {
                Server.writeExecutor(
                        500,
                        "База данных на сервере недоступна",
                        null,
                        clientChannel
                );
                return 500;
            }

            Server.writeExecutor(
                    400,
                    "Коллекция очистилась но сохранилась в БД",
                    null,
                    clientChannel
            );

            return 400;

        } catch (Exception e) {
            try {

                Server.writeExecutor(
                        500,
                        "Ошибка: " + e.getMessage(),
                        null,
                        clientChannel
                );

            } catch (Exception ex) {
                ServerLogger.error("Ошибка создания ResponsePacket clear");
            }
            return 500;
        }
    }
}