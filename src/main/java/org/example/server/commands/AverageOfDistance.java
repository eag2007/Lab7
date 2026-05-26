package org.example.server.commands;

import org.example.packet.collection.Route;
import org.example.packet.collection.RouteClient;
import org.example.packet.enums.Codes;
import org.example.server.Server;
import org.example.server.interfaces.Command;
import org.example.server.logger.ServerLogger;

import java.nio.channels.SocketChannel;

import static org.example.server.Server.managerCollections;

public class AverageOfDistance implements Command {
    public Codes executeCommand(String[] args, RouteClient value, SocketChannel clientChannel, String login, String password) {
        try {
            if (managerCollections.getSizeCollections() == 0) {

                Server.writeExecutor(
                        Codes.WARNING,
                        "Коллекция пуста",
                        0.0,
                        clientChannel
                );

                return Codes.WARNING;
            }

            double average = managerCollections.getCollectionsRoute().stream()
                    .mapToLong(Route::getDistance)
                    .average()
                    .orElse(0.0);

            Server.writeExecutor(
                    Codes.OK,
                    "Среднее значение distance",
                    average,
                    clientChannel
            );

            return Codes.OK;

        } catch (Exception e) {
            try {

                Server.writeExecutor(
                        Codes.ERROR,
                        "Ошибка: " + e.getMessage(),
                        null,
                        clientChannel
                );

            } catch (Exception ex) {
                ServerLogger.error("Ошибка создания ResponsePacket average_of_distance");
            }
            return Codes.ERROR;
        }
    }
}