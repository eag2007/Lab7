package org.example.server.commands;

import org.example.packet.ResponsePacket;
import org.example.packet.collection.RouteClient;
import org.example.server.Server;
import org.example.server.interfaces.Command;
import org.example.server.logger.ServerLogger;

import java.io.IOException;
import java.nio.channels.SocketChannel;

import static org.example.server.Server.*;

public class Clear implements Command {
    public int executeCommand(String[] args, RouteClient value, SocketChannel clientChannel, String login, String passsword) {
        Server.getWrite().submit(() -> {


            try {
                managerCollections.clearCollections(login);
                int flag = managerDataBase.clearDB(login);

                if (flag >= 0) {

                    ResponsePacket response = new ResponsePacket(
                            200,
                            "Коллекция очищена",
                            null
                    );


                    ///  ОБРАБОТКА  ЗАПИСИ
                    Server.getWrite().submit(() -> {
                        try {
                            writeModule.writeResponseForClient(clientChannel, response);
                        } catch (IOException e) {
                            ServerLogger.error("Ошибка отправки {}", e.getMessage());
                        }
                    });
                    /// ОБРАБОТКА ЗАПИСИ


                    return 200;
                }

                ResponsePacket response = new ResponsePacket(
                        400,
                        "Коллекция очистилась но сохранилась в БД",
                        null
                );


                ///  ОБРАБОТКА  ЗАПИСИ
                Server.getWrite().submit(() -> {
                    try {
                        writeModule.writeResponseForClient(clientChannel, response);
                    } catch (IOException e) {
                        ServerLogger.error("Ошибка отправки {}", e.getMessage());
                    }
                });
                /// ОБРАБОТКА ЗАПИСИ


                return 400;

            } catch (Exception e) {
                try {
                    ResponsePacket error = new ResponsePacket(
                            500,
                            "Ошибка: " + e.getMessage(),
                            null
                    );


                    ///  ОБРАБОТКА  ЗАПИСИ
                    Server.getWrite().submit(() -> {
                        try {
                            writeModule.writeResponseForClient(clientChannel, error);
                        } catch (IOException ex) {
                            ServerLogger.error("Ошибка отправки {}", ex.getMessage());
                        }
                    });
                    /// ОБРАБОТКА ЗАПИСИ


                } catch (Exception ex) {
                    ServerLogger.error("Ошибка создания ResponsePacket clear");
                }
                return 500;
            }

        });
        return 0;
    }
}