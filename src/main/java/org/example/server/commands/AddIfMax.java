package org.example.server.commands;

import org.example.packet.collection.Route;
import org.example.packet.ResponsePacket;
import org.example.packet.collection.RouteClient;
import org.example.server.Server;
import org.example.server.interfaces.Command;
import org.example.server.logger.ServerLogger;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.Comparator;

import static org.example.server.Server.*;

public class AddIfMax implements Command {
    public int executeCommand(String[] args, RouteClient value, SocketChannel clientChannel, String login, String password) {
        try {
            if (value == null) {
                ResponsePacket response = new ResponsePacket(400, "Не переданы данные элемента", null);
                Server.getWrite().submit(() -> {
                    try {
                        writeModule.writeResponseForClient(clientChannel, response);
                    } catch (IOException e) {
                        ServerLogger.error("Ошибка отправки {}", e.getMessage());
                    }
                });
                return 400;
            }

            long id = managerDataBase.addDB(value, login);

            if (id == -1) {
                ResponsePacket response = new ResponsePacket(400, "Маршрут с таким именем уже существует", null);


                /// ОБРАБОТКА ЗАПИСИ
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

            Route newRoute = new Route(id,
                    value.getName(),
                    value.getCoordinates(),
                    value.getFrom(),
                    value.getTo(),
                    value.getDistance(),
                    value.getPrice(),
                    login);

            if (managerCollections.getCollectionsRoute().isEmpty()) {
                managerCollections.addCollections(newRoute);
                ResponsePacket response = new ResponsePacket(200, "Коллекция была пуста, элемент добавлен", newRoute.getId());


                /// ОБРАБОТКА ЗАПИСИ
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

            Route maxRoute = managerCollections.getCollectionsRoute().stream()
                    .max(Comparator.naturalOrder())
                    .orElse(null);

            if (maxRoute != null && newRoute.compareTo(maxRoute) > 0) {
                managerCollections.addCollections(newRoute);
                ResponsePacket response = new ResponsePacket(200, "Элемент добавлен (превышает максимальный)", newRoute.getId());


                /// ОБРАБОТКА ЗАПИСИ
                Server.getWrite().submit(() -> {
                    try {
                        writeModule.writeResponseForClient(clientChannel, response);
                    } catch (IOException e) {
                        ServerLogger.error("Ошибка отправки {}", e.getMessage());
                    }
                });
                /// ОБРАБОТКА ЗАПИСИ


                return 200;
            } else {
                managerDataBase.deleteDB(id);
                ResponsePacket response = new ResponsePacket(400, "Элемент не добавлен (не превышает максимальный)", null);


                /// ОБРАБОТКА ЗАПИСИ
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

        } catch (Exception e) {
            ServerLogger.error("Ошибка при добавлении: {}", e.getMessage());
            ResponsePacket error = new ResponsePacket(
                    500,
                    "Ошибка: " + e.getMessage(),
                    null);


            /// ОБРАБОТКА ЗАПИСИ
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