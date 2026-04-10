package org.example.server.commands;

import org.example.packet.ResponsePacket;
import org.example.packet.collection.RouteClient;
import org.example.server.Server;
import org.example.server.interfaces.Command;
import org.example.server.logger.ServerLogger;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;

import static org.example.server.Server.managerCollections;
import static org.example.server.Server.writeModule;

public class Info implements Command {
    public int executeCommand(String[] args, RouteClient value, SocketChannel clientChannel, String login, String password) {
        try {
            Map<String, Object> info = new HashMap<>();
            info.put("size", managerCollections.getSizeCollections());
            info.put("initTime", managerCollections.getTimeInit().toString());
            info.put("createdBy", login);
            info.put("type", "PriorityQueue<Route>");

            ResponsePacket response = new ResponsePacket(200, "Информация о коллекции", info);


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
            ServerLogger.error("Ошибка info: {}", e.getMessage());
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


            return 500;
        }
    }
}