package org.example.server.commands;

import org.example.packet.collection.RouteClient;
import org.example.packet.enums.Codes;
import org.example.server.Server;
import org.example.server.interfaces.Command;
import org.example.server.logger.ServerLogger;
import org.example.server.managers.ManagerHasher;

import java.nio.channels.SocketChannel;
import java.sql.*;


public class Login implements Command {
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

                    Server.getLoginToChannel().put(login, clientChannel);

                    Server.writeExecutor(
                            Codes.OK,
                            "Успешно вошли в аккаунт",
                            null,
                            clientChannel
                    );

                    return Codes.OK;
                } else {

                    Server.writeExecutor(
                            Codes.WARNING,
                            "Неверный пароль",
                            null,
                            clientChannel
                    );

                    ServerLogger.info("Неверный пароль: {}", login);

                    return Codes.WARNING;
                }
            } else {

                Server.writeExecutor(
                        Codes.WARNING,
                        "Пользователь не найден",
                        null,
                        clientChannel
                );

                return Codes.WARNING;
            }

        } catch (SQLException e) {
            ServerLogger.error("Ошибка БД при входе: {}", e.getMessage());

            Server.writeExecutor(
                    Codes.ERROR,
                    "Ошибка входа",
                    null,
                    clientChannel
            );

            return Codes.ERROR;
        }
    }
}