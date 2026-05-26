package org.example.server.commands;

import org.example.packet.collection.Route;
import org.example.packet.collection.RouteClient;
import org.example.server.Server;
import org.example.server.interfaces.Command;
import org.example.server.logger.ServerLogger;

import java.nio.channels.SocketChannel;

import static org.example.server.Server.managerCollections;

public class AverageOfDistance implements Command {
    public int executeCommand(String[] args, RouteClient value, SocketChannel clientChannel, String login, String password) {
        try {
            if (managerCollections.getSizeCollections() == 0) {

                Server.writeExecutor(
                        400,
                        "Коллекция пуста",
                        0.0,
                        clientChannel
                );

                return 400;
            }

            double average = managerCollections.getCollectionsRoute().stream()
                    .mapToLong(Route::getDistance)
                    .average()
                    .orElse(0.0);

            Server.writeExecutor(
                    200,
                    "Среднее значение distance",
                    average,
                    clientChannel
            );

            return 200;

        } catch (Exception e) {
            try {

                Server.writeExecutor(
                        500,
                        "Ошибка: " + e.getMessage(),
                        null,
                        clientChannel
                );

            } catch (Exception ex) {
                ServerLogger.error("Ошибка создания ResponsePacket average_of_distance");
            }
            return 500;
        }
    }
}