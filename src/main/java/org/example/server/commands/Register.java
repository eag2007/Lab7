package org.example.server.commands;

import org.example.packet.collection.RouteClient;
import org.example.packet.enums.Codes;
import org.example.server.Server;
import org.example.server.interfaces.Command;
import org.example.server.logger.ServerLogger;
import org.example.server.managers.ManagerHasher;

import java.nio.channels.SocketChannel;
import java.sql.*;


public class Register implements Command {
    public Codes executeCommand(String[] args, RouteClient values, SocketChannel clientChannel, String login, String password) {
        try {

            Connection conn = Server.managerDataBase.getConnection();
            if (conn == null) {

                Server.writeExecutor(
                        Codes.ERROR,
                        "База данных временно недоступна",
                        null,
                        clientChannel
                );

                return Codes.ERROR;
            }

            if (password.length() < 4) {

                Server.writeExecutor(
                        Codes.WARNING,
                        "Пароль слишком короткий",
                        null,
                        clientChannel
                );

                ServerLogger.debug("Короткий пароль");
                return Codes.WARNING;
            }

            conn = Server.managerDataBase.getConnection();

            PreparedStatement pstmt = conn.prepareStatement("INSERT INTO users (login, password, salt) VALUES (?, ?, ?)");

            String salt = ManagerHasher.salt();
            String hash = ManagerHasher.hash(password, salt);

            pstmt.setString(1, login);
            pstmt.setString(2, hash);
            pstmt.setString(3, salt);

            pstmt.executeUpdate();

            Server.getLoginToChannel().put(login, clientChannel);

            Server.writeExecutor(
                    Codes.OK,
                    "Пользователь зарегистрирован",
                    null,
                    clientChannel
            );

            ServerLogger.info("Регистрация прошла успешно с логином: {}", login);
            return Codes.OK;

        } catch (SQLException e) {
            ServerLogger.error("Ошибка Регистрации: {}", e.getMessage());
            if (e.getMessage().contains("unique")) {

                Server.writeExecutor(
                        Codes.WARNING,
                        "Пользователь существует",
                        null,
                        clientChannel
                );

                ServerLogger.debug("Пользователь {} уже существует", login);
            } else {

                Server.writeExecutor(
                        Codes.ERROR,
                        "Ошибка базы данных",
                        null,
                        clientChannel
                );

                ServerLogger.debug("Ошибка в БД");
            }
            return Codes.ERROR;
        } catch (Exception e) {
            ServerLogger.error("Ошибка Регистрации: {}", e.getMessage());

            Server.writeExecutor(
                    Codes.ERROR,
                    "Ошибка регистрациии",
                    null,
                    clientChannel
            );

            return Codes.ERROR;
        }
    }
}