package org.example.server.commands;

import org.example.packet.collection.RouteClient;
import org.example.server.Server;
import org.example.server.interfaces.Command;
import org.example.server.logger.ServerLogger;
import org.example.server.managers.ManagerHasher;

import java.nio.channels.SocketChannel;
import java.sql.*;


public class Register implements Command {
    public int executeCommand(String[] args, RouteClient values, SocketChannel clientChannel, String login, String password) {
        try {

            Connection conn = Server.managerDataBase.getConnection();
            if (conn == null) {

                Server.writeExecutor(
                        500,
                        "База данных временно недоступна",
                        null,
                        clientChannel
                );

                return 500;
            }

            if (password.length() < 4) {

                Server.writeExecutor(
                        400,
                        "Пароль слишком короткий",
                        null,
                        clientChannel
                );

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

            Server.writeExecutor(
                    200,
                    "Пользователь зарегистрирован",
                    null,
                    clientChannel
            );

            ServerLogger.info("Регистрация прошла успешно с логином: {}", login);
            return 200;

        } catch (SQLException e) {
            ServerLogger.error("Ошибка Регистрации: {}", e.getMessage());
            if (e.getMessage().contains("unique")) {

                Server.writeExecutor(
                        400,
                        "Пользователь существует",
                        null,
                        clientChannel
                );

                ServerLogger.debug("Пользователь {} уже существует", login);
            } else {

                Server.writeExecutor(
                        500,
                        "Ошибка базы данных",
                        null,
                        clientChannel
                );

                ServerLogger.debug("Ошибка в БД");
            }
            return 500;
        } catch (Exception e) {
            ServerLogger.error("Ошибка Регистрации: {}", e.getMessage());

            Server.writeExecutor(
                    500,
                    "Ошибка регистрациии",
                    null,
                    clientChannel
            );

            return 500;
        }
    }
}