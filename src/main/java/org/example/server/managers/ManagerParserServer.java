package org.example.server.managers;

import org.example.packet.CommandPacket;
import org.example.server.commands.*;
import org.example.server.interfaces.Command;
import org.example.server.logger.ServerLogger;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.*;

import static org.example.server.Server.managerDataBase;

public class ManagerParserServer {
    private final HashMap<String, Command> commands;

    public ManagerParserServer() {
        this.commands = new HashMap<String, Command>();

        this.commands.put("add", new Add());
        this.commands.put("add_if_max", new AddIfMax());
        this.commands.put("average_of_distance", new AverageOfDistance());
        this.commands.put("clear", new Clear());
        this.commands.put("filter_less_than_distance", new FilterLessThanDistance());
        this.commands.put("info", new Info());
        this.commands.put("remove_all_by_distance", new RemoveAllByDistance());
        this.commands.put("remove_by_id", new RemoveById());
        this.commands.put("remove_first", new RemoveFirst());
        this.commands.put("show", new Show());
        this.commands.put("update", new Update());
        this.commands.put("register", new Register());
        this.commands.put("login", new Login());
    }

    public int parserCommand(CommandPacket commandPacket, SocketChannel clientChannel) {
        String command_name = commandPacket.getType();
        String login = commandPacket.getLogin();
        String password = commandPacket.getPassword();

        if (!command_name.equals("login") && !command_name.equals("register")) {
            if (!managerDataBase.checkUserPassword(login, password)) {
                ServerLogger.info("ПОПЫТКА ВЗЛОМА под логином пользователя {}", login);

                try {
                    String response = "За вашим поведением начнут следить" + command_name;
                    clientChannel.write(ByteBuffer.wrap(response.getBytes(StandardCharsets.UTF_8)));
                } catch (IOException e) {
                    ServerLogger.error("Ошибка отправки ответа: {}", e.getMessage());
                }

                return 401;
            }
        }

        if (this.commands.containsKey(command_name)) {
            Command command = this.commands.get(command_name);

            try {
                int code = command.executeCommand(
                        commandPacket.getArgs(),
                        commandPacket.getValues(),
                        clientChannel,
                        commandPacket.getLogin(),
                        commandPacket.getPassword()
                );

                return code;
            } catch (IOException e) {
                ServerLogger.error("Ошибка выполнения команды {}: {}", command_name, e.getMessage());
                return 500;
            }
        } else {
            try {
                String response = "Неизвестная команда: " + command_name;
                clientChannel.write(ByteBuffer.wrap(response.getBytes(StandardCharsets.UTF_8)));
            } catch (IOException e) {
                ServerLogger.error("Ошибка отправки ответа: {}", e.getMessage());
            }
            return 404;
        }
    }
}