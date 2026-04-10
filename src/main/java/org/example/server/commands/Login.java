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

            PreparedStatement pstmt = conn.prepareStatement("SELECT password FROM users WHERE login = ?");
            pstmt.setString(1, login);

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                if (rs.getString("password").equals(ManagerHasher.hash(password))) {

                    writeModule.writeResponseForClient(client, new ResponsePacket(
                            200,
                            "Успешно залогинился",
                            null
                    ));

                    ServerLogger.info("Успешно вошли под логином: {}", login);
                    return 200;
                } else {

                    writeModule.writeResponseForClient(client, new ResponsePacket(
                            500,
                            "Неверный пароль",
                            null
                    ));

                    ServerLogger.info("Неверный пароль: {}", login);
                    return 500;
                }
            } else {
                writeModule.writeResponseForClient(client, new ResponsePacket(
                        400,
                        "Пользователь не найден",
                        null
                ));

                return 400;
            }

        } catch (Exception e) {
            ServerLogger.error("Неверный логин: {}", e.getMessage());
            try {
                writeModule.writeResponseForClient(client, new ResponsePacket(
                        500,
                        "Ошибка Входа",
                        null
                ));

            } catch (IOException ex) {
                ServerLogger.error("Ошибка отправки при неверном логине");
            }
            return 500;
        }
    }
}