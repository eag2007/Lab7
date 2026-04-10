package org.example.server.commands;

import org.example.packet.ResponsePacket;
import org.example.packet.collection.Route;
import org.example.packet.collection.RouteClient;
import org.example.server.Server;
import org.example.server.interfaces.Command;
import org.example.server.logger.ServerLogger;

import java.io.IOException;
import java.nio.channels.SocketChannel;

import static org.example.server.Server.*;

public class RemoveById implements Command {
    public int executeCommand(String[] args, RouteClient value, SocketChannel clientChannel, String login, String password) {
        try {
            if (args == null || args.length < 1) {
                ResponsePacket response = new ResponsePacket(
                        400,
                        "Не указан ID",
                        null
                );


                ///  ОБРАБОТКА ЗАПИСИ
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

            Long id = Long.parseLong(args[0]);

            Route route = managerDataBase.getDB(id, login);
            if (route == null) {
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

            boolean removed = managerCollections.removeRouteById(id);

            if (removed) {
                ResponsePacket response = new ResponsePacket(
                        200,
                        "Элемент с id " + id + " удален",
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
            } else {
                ResponsePacket response = new ResponsePacket(
                        400,
                        "Элемент с id " + id + " не найден в коллекции",
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
            ServerLogger.error("Ошибка удаления: {}", e.getMessage());
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