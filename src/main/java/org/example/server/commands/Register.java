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
            if (password.length() < 4) {
                writeModule.writeResponseForClient(client, new ResponsePacket(
                        400,
                        "Пароль слишком короткий",
                        null
                ));

                return 400;
            }

            Connection conn = Server.managerDataBase.getConnection();

            PreparedStatement pstmt = conn.prepareStatement("INSERT INTO users (login, password) VALUES (?, ?)");

            pstmt.setString(1, login);
            pstmt.setString(2, ManagerHasher.hash(password));

            pstmt.executeUpdate();

            writeModule.writeResponseForClient(client, new ResponsePacket(
                    200,
                    "Пользователь зарегистрирован",
                    null
            ));

            ServerLogger.info("Регистрация прошла успешно с логином: {}", login);
            return 200;

        } catch (SQLException e) {
            ServerLogger.error("Ошибка Регистрации: {}", e.getMessage());
            if (e.getMessage().contains("unique")) {
                try {
                    writeModule.writeResponseForClient(client, new ResponsePacket(
                            400,
                            "Пользователь существует",
                            null
                    ));

                } catch (IOException ex) {
                    ServerLogger.info("Произошла ошибка при отправке");
                }
            } else {
                try {
                    writeModule.writeResponseForClient(client, new ResponsePacket(
                            500,
                            "Ошибка базы данных",
                            null
                    ));

                } catch (IOException ex) {
                    ServerLogger.info("Произошла ошибка при работе с БД");
                }
            }
            return 500;
        } catch (Exception e) {
            ServerLogger.error("Ошибка Регистрации: {}", e.getMessage());
            try {
                writeModule.writeResponseForClient(client, new ResponsePacket(
                        500,
                        "Ошибка Регистрации",
                        null
                ));

            } catch (Exception ex) {
                ServerLogger.info("Ошибка отправки при регистрации: {}", e.getMessage());
            }
            return 500;
        }
    }
}