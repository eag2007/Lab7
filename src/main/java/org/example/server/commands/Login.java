package org.example.server.commands;

import org.example.packet.collection.RouteClient;
import org.example.server.Server;
import org.example.server.interfaces.Command;
import org.example.server.logger.ServerLogger;
import org.example.server.managers.ManagerHasher;

import java.nio.channels.SocketChannel;
import java.sql.*;


public class Login implements Command {
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

                    Server.writeExecutor(
                            200,
                            "Успешно вошли в аккаунт",
                            null,
                            clientChannel
                    );

                    return 200;
                } else {

                    Server.writeExecutor(
                            400,
                            "Неверный пароль",
                            null,
                            clientChannel
                    );

                    ServerLogger.info("Неверный пароль: {}", login);

                    return 400;
                }
            } else {

                Server.writeExecutor(
                        400,
                        "Пользователь не найден",
                        null,
                        clientChannel
                );

                return 400;
            }

        } catch (SQLException e) {
            ServerLogger.error("Ошибка БД при входе: {}", e.getMessage());

            Server.writeExecutor(
                    500,
                    "Ошибка входа",
                    null,
                    clientChannel
            );

            return 500;
        }
    }
}