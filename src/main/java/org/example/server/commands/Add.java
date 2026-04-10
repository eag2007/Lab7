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

public class Add implements Command {
    public int executeCommand(String[] args, RouteClient value, SocketChannel clientChannel, String login, String password) {
        try {
            long id = managerDataBase.addDB(value, login);

            if (id == -1) {
                ResponsePacket response = new ResponsePacket(
                        400,
                        "Маршрут с таким именем уже существует",
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
            }

            Route route = managerDataBase.getDB(id, login);
            if (route != null) {
                managerCollections.addCollections(route);
            }

            ResponsePacket response = new ResponsePacket(
                    200,
                    "Объект добавлен в коллекцию с ID: " + id,
                    id
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
        } catch (Exception e) {
            ServerLogger.error("Ошибка добавления: {}", e.getMessage());
            ResponsePacket error = new ResponsePacket(
                    500,
                    "Ошибка добавления: " + e.getMessage(),
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


            return 500;
        }
    }
}