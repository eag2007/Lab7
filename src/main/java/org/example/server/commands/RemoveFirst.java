package org.example.server.commands;

import org.example.packet.ResponsePacket;
import org.example.packet.collection.RouteClient;
import org.example.server.Server;
import org.example.server.interfaces.Command;
import org.example.packet.collection.Route;
import org.example.server.logger.ServerLogger;

import java.io.IOException;
import java.nio.channels.SocketChannel;

import static org.example.server.Server.*;

public class RemoveFirst implements Command {
    public int executeCommand(String[] args, RouteClient values, SocketChannel clientChannel, String login, String password) {
        try {
            Route route = managerCollections.getCollectionsRoute().peek();

            if (route == null) {
                ResponsePacket response = new ResponsePacket(
                        400,
                        "Коллекция пуста",
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

            if (!route.getAuthor().equals(login)) {
                ResponsePacket response = new ResponsePacket(
                        400,
                        "Первый элемент принадлежит другому пользователю",
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

            long id = route.getId();

            long deletedId = managerDataBase.deleteDB(id);

            if (deletedId == 0) {
                ResponsePacket response = new ResponsePacket(
                        500,
                        "Ошибка удаления из БД",
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

            if (deletedId == -1) {
                ResponsePacket response = new ResponsePacket(
                        500,
                        "Ошибка при удалении из БД",
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

            managerCollections.removeRouteById(id);

            ResponsePacket response = new ResponsePacket(
                    200,
                    "Объект удалён с id = " + id,
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
            ServerLogger.error("Ошибка удаления первого элемента: {}", e.getMessage());
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