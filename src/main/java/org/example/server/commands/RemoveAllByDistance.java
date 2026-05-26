package org.example.server.commands;

import org.example.packet.collection.Route;
import org.example.packet.ResponsePacket;
import org.example.packet.collection.RouteClient;
import org.example.server.Server;
import org.example.server.interfaces.Command;
import org.example.server.logger.ServerLogger;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.PriorityQueue;
import java.util.stream.Collectors;

import static org.example.server.Server.*;

public class RemoveAllByDistance implements Command {
    public int executeCommand(String[] args, RouteClient value, SocketChannel clientChannel, String login, String password) {
        try {
            int distance = Integer.parseInt(args[0]);

            managerDataBase.deleteRouteDistanceInDB(distance, login);

            long removedCount = managerCollections.getCollectionsRoute().stream()
                    .filter(route -> route.getDistance() == distance && route.getAuthor().equals(login))
                    .count();

            PriorityQueue<Route> routesNew = managerCollections.getCollectionsRoute().stream()
                    .filter(route -> !(route.getDistance() == distance && route.getAuthor().equals(login)))
                    .collect(Collectors.toCollection(PriorityQueue::new));

            managerCollections.removeAllByDistanceCollections(routesNew);

            Server.writeExecutor(
                    200,
                    "Удалено элементов: " + removedCount,
                    null,
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
                ServerLogger.error("Ошибка создания ResponsePacket remove_all_by_distance");
            }
            return 500;
        }
    }
}