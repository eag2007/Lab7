package org.example.server.commands;

import org.example.packet.collection.Route;
import org.example.packet.ResponsePacket;
import org.example.packet.collection.RouteClient;
import org.example.server.Server;
import org.example.server.interfaces.Command;
import org.example.server.logger.ServerLogger;

import java.io.IOException;
import java.nio.channels.SocketChannel;

import static org.example.server.Server.managerCollections;
import static org.example.server.Server.writeModule;

public class AverageOfDistance implements Command {
    public int executeCommand(String[] args, RouteClient value, SocketChannel clientChannel, String login, String password) {
        try {
            if (managerCollections.getSizeCollections() == 0) {
                ResponsePacket response = new ResponsePacket(
                        400,
                        "Коллекция пуста",
                        0.0
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

            double average = managerCollections.getCollectionsRoute().stream()
                    .mapToLong(Route::getDistance)
                    .average()
                    .orElse(0.0);

            ResponsePacket response = new ResponsePacket(
                    200,
                    "Среднее значение distance",
                    average
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
                ServerLogger.error("Ошибка создания ResponsePacket average_of_distance");
            }
            return 500;
        }
    }
}