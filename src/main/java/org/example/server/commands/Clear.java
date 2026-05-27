package org.example.server.commands;

import org.example.packet.collection.RouteClient;
import org.example.packet.enums.Codes;
import org.example.server.Server;
import org.example.server.interfaces.Command;
import org.example.server.logger.ServerLogger;

import java.nio.channels.SocketChannel;

import static org.example.server.Server.*;

public class Clear implements Command {
    public Codes executeCommand(String[] args, RouteClient value, SocketChannel clientChannel, String login, String passsword) {
        try {
            managerCollections.clearCollections(login);
            int flag = managerDataBase.clearRoutesInDB(login);

            if (flag >= 0) {

                Server.writeExecutor(
                        Codes.OK,
                        "Коллекция очищена",
                        null,
                        clientChannel
                );

                managerPush.sendPushToSubscribes("Коллекция очищена. Пользователем: " +  login);

                return Codes.OK;
            }

            if (flag == -3) {
                Server.writeExecutor(
                        Codes.ERROR,
                        "База данных на сервере недоступна",
                        null,
                        clientChannel
                );
                return Codes.ERROR;
            }

            Server.writeExecutor(
                    Codes.WARNING,
                    "Коллекция очистилась но сохранилась в БД",
                    null,
                    clientChannel
            );

            return Codes.WARNING;

        } catch (Exception e) {
            try {

                Server.writeExecutor(
                        Codes.ERROR,
                        "Ошибка: " + e.getMessage(),
                        null,
                        clientChannel
                );

            } catch (Exception ex) {
                ServerLogger.error("Ошибка создания ResponsePacket clear");
            }
            return Codes.ERROR;
        }
    }
}