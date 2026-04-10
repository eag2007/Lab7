package org.example.server.commands;

import org.example.packet.collection.Route;
import org.example.packet.ResponsePacket;
import org.example.packet.collection.RouteClient;
import org.example.server.Server;
import org.example.server.interfaces.Command;
import org.example.server.logger.ServerLogger;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.List;

import static org.example.server.Server.managerCollections;
import static org.example.server.Server.writeModule;

public class Show implements Command {
    public int executeCommand(String[] args, RouteClient value, SocketChannel clientChannel, String login, String password) {
        try {
            List<Route> routes = managerCollections.getSortedCollections();

            ResponsePacket response;
            if (routes.isEmpty()) {
                response = new ResponsePacket(
                        200,
                        "Коллекция пуста",
                        routes
                );
            } else {
                response = new ResponsePacket(200,
                        "Найдено элементов: " + routes.size(),
                        routes
                );
            }


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
            ServerLogger.error("Ошибка при получении коллекции: {}", e.getMessage());
            ResponsePacket error = new ResponsePacket(
                    500,
                    "Ошибка при получении коллекции: " + e.getMessage(),
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