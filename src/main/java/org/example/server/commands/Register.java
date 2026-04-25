package org.example.server.commands;

import org.example.packet.ResponsePacket;
import org.example.packet.collection.RouteClient;
import org.example.server.Server;
import org.example.server.interfaces.Command;
import org.example.server.logger.ServerLogger;
import org.example.server.managers.ManagerHasher;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.sql.*;

import static org.example.server.Server.writeModule;

public class Register implements Command {
    public int executeCommand(String[] args, RouteClient values, SocketChannel client, String login, String password) {
        try {

            Connection conn = Server.managerDataBase.getConnection();
            if (conn == null) {


                /// ОБРАБОТКА ЗАПИСИ
                Server.getWrite().submit(() -> {
                    try {
                        writeModule.writeResponseForClient(client,
                                new ResponsePacket(500, "База данных временно недоступна", null));
                    } catch (Exception ignored) {}
                });
                /// ОБРАБОТКА ЗАПИСИ


                return 500;
            }

            if (password.length() < 4) {
                ResponsePacket response = new ResponsePacket(
                        400,
                        "Пароль слишком короткий",
                        null
                );


                ///  ОБРАБОТКА  ЗАПИСИ
                Server.getWrite().submit(() -> {
                    try {
                        writeModule.writeResponseForClient(client, response);
                    } catch (IOException e) {
                        ServerLogger.error("Ошибка отправки {}", e.getMessage());
                    }
                });
                /// ОБРАБОТКА ЗАПИСИ


                ServerLogger.debug("Короткий пароль");
                return 400;
            }

            conn = Server.managerDataBase.getConnection();

            PreparedStatement pstmt = conn.prepareStatement("INSERT INTO users (login, password, salt) VALUES (?, ?, ?)");

            String salt = ManagerHasher.salt();
            String hash = ManagerHasher.hash(password, salt);

            pstmt.setString(1, login);
            pstmt.setString(2, hash);
            pstmt.setString(3, salt);

            pstmt.executeUpdate();

            ResponsePacket response = new ResponsePacket(
                    200,
                    "Пользователь зарегистрирован",
                    null
            );


            ///  ОБРАБОТКА  ЗАПИСИ
            Server.getWrite().submit(() -> {
                try {
                    writeModule.writeResponseForClient(client, response);
                } catch (IOException e) {
                    ServerLogger.error("Ошибка отправки {}", e.getMessage());
                }
            });
            /// ОБРАБОТКА ЗАПИСИ


            ServerLogger.info("Регистрация прошла успешно с логином: {}", login);
            return 200;

        } catch (SQLException e) {
            ServerLogger.error("Ошибка Регистрации: {}", e.getMessage());
            if (e.getMessage().contains("unique")) {
                ResponsePacket response = new ResponsePacket(
                        400,
                        "Пользователь существует",
                        null
                );


                ///  ОБРАБОТКА  ЗАПИСИ
                Server.getWrite().submit(() -> {
                    try {
                        writeModule.writeResponseForClient(client, response);
                    } catch (IOException ei) {
                        ServerLogger.error("Ошибка отправки {}", ei.getMessage());
                    }
                });
                /// ОБРАБОТКА ЗАПИСИ


                ServerLogger.debug("Пользователь {} уже существует", login);
            } else {
                ResponsePacket response = new ResponsePacket(
                        500,
                        "Ошибка базы данных",
                        null
                );


                ///  ОБРАБОТКА  ЗАПИСИ
                Server.getWrite().submit(() -> {
                    try {
                        writeModule.writeResponseForClient(client, response);
                    } catch (IOException ei) {
                        ServerLogger.error("Ошибка отправки {}", ei.getMessage());
                    }
                });
                /// ОБРАБОТКА ЗАПИСИ


                ServerLogger.debug("Ошибка в БД");
            }
            return 500;
        } catch (Exception e) {
            ServerLogger.error("Ошибка Регистрации: {}", e.getMessage());


            ResponsePacket response = new ResponsePacket(
                    500,
                    "Ошибка Регистрации",
                    null
            );


            ///  ОБРАБОТКА  ЗАПИСИ
            Server.getWrite().submit(() -> {
                try {
                    writeModule.writeResponseForClient(client, response);
                } catch (IOException ei) {
                    ServerLogger.error("Ошибка отправки {}", ei.getMessage());
                }
            });
            /// ОБРАБОТКА ЗАПИСИ


            return 500;
        }
    }
}