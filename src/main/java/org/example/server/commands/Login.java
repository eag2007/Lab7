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

public class Login implements Command {
    public int executeCommand(String[] args, RouteClient values, SocketChannel client, String login, String password) {
        try {


            Connection conn = Server.managerDataBase.getConnection();
            if (conn == null) {

                ResponsePacket responsePacket = new ResponsePacket(
                        500,
                        "База данных временно недоступна",
                        null
                );


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

            PreparedStatement pstmt = conn.prepareStatement(
                    "SELECT password, salt FROM users WHERE login = ?"
            );

            pstmt.setString(1, login);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String passwordHash = rs.getString("password");
                String salt = rs.getString("salt");

                String inputHash = ManagerHasher.hash(password, salt);

                if (passwordHash.equals(inputHash)) {

                    ResponsePacket response = new ResponsePacket(
                            200,
                            "Успешно вошли в аккаунт",
                            null
                    );

                    ServerLogger.info("Успешный вход: {}", login);


                    ///  ОБРАБОТКА  ЗАПИСИ
                    Server.getWrite().submit(() -> {
                        try {
                            writeModule.writeResponseForClient(client, response);
                        } catch (IOException e) {
                            ServerLogger.error("Ошибка отправки {}", e.getMessage());
                        }
                    });
                    /// ОБРАБОТКА ЗАПИСИ


                    return 200;
                } else {

                    ResponsePacket response = new ResponsePacket(
                            400,
                            "Неверный пароль",
                            null
                    );

                    ServerLogger.info("Неверный пароль: {}", login);


                    ///  ОБРАБОТКА  ЗАПИСИ
                    Server.getWrite().submit(() -> {
                        try {
                            writeModule.writeResponseForClient(client, response);
                        } catch (IOException e) {
                            ServerLogger.error("Ошибка отправки {}", e.getMessage());
                        }
                    });
                    /// ОБРАБОТКА ЗАПИСИ


                    return 400;
                }
            } else {

                ResponsePacket response = new ResponsePacket(
                        400,
                        "Пользователь не найден",
                        null
                );

                ServerLogger.info("Пользователь не найден: {}", login);


                ///  ОБРАБОТКА  ЗАПИСИ
                Server.getWrite().submit(() -> {
                    try {
                        writeModule.writeResponseForClient(client, response);
                    } catch (IOException e) {
                        ServerLogger.error("Ошибка отправки {}", e.getMessage());
                    }
                });
                /// ОБРАБОТКА ЗАПИСИ


                return 400;
            }

        } catch (SQLException e) {
            ServerLogger.error("Ошибка БД при входе: {}", e.getMessage());
            ResponsePacket response = new ResponsePacket(
                    500,
                    "Ошибка входа: " + e.getMessage(),
                    null
            );


            ///  ОБРАБОТКА  ЗАПИСИ
            Server.getWrite().submit(() -> {
                try {
                    writeModule.writeResponseForClient(client, response);
                } catch (IOException ex) {
                    ServerLogger.error("Ошибка отправки {}", ex.getMessage());
                }
            });
            /// ОБРАБОТКА ЗАПИСИ


            return 500;
        }
    }
}