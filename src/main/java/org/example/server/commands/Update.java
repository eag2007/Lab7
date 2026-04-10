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

public class Update implements Command {
    public int executeCommand(String[] args, RouteClient newRoute, SocketChannel clientChannel, String login, String password) {
        try {
            if (args == null || args.length < 1) {
                ResponsePacket response = new ResponsePacket(
                        400,
                        "Не указан ID",
                        null
                );


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

            long id = Long.parseLong(args[0]);

            Route existingRoute = managerDataBase.getDB(id, login);

            if (existingRoute == null) {
                ResponsePacket response = new ResponsePacket(
                        400,
                        "Элемент с id " + id + " не найден у пользователя " + login,
                        null
                );


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

            if (newRoute == null) {
                ResponsePacket response = new ResponsePacket(
                        200,
                        "Элемент с id " + id + " найден",
                        existingRoute
                );


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

            Route updatedRoute = new Route(
                    id,
                    newRoute.getName(),
                    newRoute.getCoordinates(),
                    existingRoute.getCreationDate(),
                    newRoute.getFrom(),
                    newRoute.getTo(),
                    newRoute.getDistance(),
                    newRoute.getPrice(),
                    login
            );

            boolean updated = managerDataBase.updateDB(updatedRoute, login);

            if (!updated) {
                ResponsePacket response = new ResponsePacket(
                        500,
                        "Ошибка обновления в БД",
                        null
                );


                /// ОБРАБОТКА ЗАПИСИ
                Server.getWrite().submit(() -> {
                    try {
                        writeModule.writeResponseForClient(clientChannel, response);
                    } catch (IOException e) {
                        ServerLogger.error("Ошибка отправки {}", e.getMessage());
                    }
                });
                /// ОБРАБОТКА ЗАПИСИ


                return 500;
            }

            managerCollections.updateRoute(updatedRoute);

            ResponsePacket response = new ResponsePacket(
                    200,
                    "Элемент с id " + id + " обновлен",
                    null
            );


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

        } catch (NumberFormatException e) {
            ResponsePacket response = new ResponsePacket(
                    400,
                    "ID должен быть числом",
                    null
            );


            /// ОБРАБОТКА ЗАПИСИ
            Server.getWrite().submit(() -> {
                try {
                    writeModule.writeResponseForClient(clientChannel, response);
                } catch (IOException ex) {
                    ServerLogger.error("Ошибка отправки {}", ex.getMessage());
                }
            });
            /// ОБРАБОТКА ЗАПИСИ


            return 400;
        } catch (Exception e) {
            ServerLogger.error("Ошибка в update: {}", e.getMessage());
            ResponsePacket error = new ResponsePacket(
                    500,
                    "Ошибка: " + e.getMessage(),
                    null
            );


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