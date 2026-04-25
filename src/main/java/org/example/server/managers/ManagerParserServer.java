package org.example.server.managers;

import org.example.packet.CommandPacket;
import org.example.packet.ResponsePacket;
import org.example.server.commands.*;
import org.example.server.interfaces.Command;
import org.example.server.logger.ServerLogger;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.*;

import static org.example.server.Server.managerDataBase;
import static org.example.server.Server.writeModule;

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

            if (!managerDataBase.repeatConnect()) {
                sendError(clientChannel, 400, "База данных временно недоступна. Попробуйте позже");
                ServerLogger.debug("БД недоступна при выполнении команды {} от {}", command_name, login);
                return 400;
            }

            if (!managerDataBase.checkUserPassword(login, password)) {
                ServerLogger.info("ПОПЫТКА ВЗЛОМА под логином пользователя {}", login);

                sendError(clientChannel, 500, "За вашим поведением начнут следить");
                return 500;
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
            sendError(clientChannel, 400, "Неизвестная команда: " + command_name);
            return 404;
        }
    }

    private void sendError(SocketChannel clientChannel, int code, String message) {
        try {
            ResponsePacket packet = new ResponsePacket(
                    code,
                    message,
                    null
            );

            writeModule.writeResponseForClient(clientChannel, packet);
        } catch (IOException e) {
            ServerLogger.error("Ошибка отправки ответа: {}", e.getMessage());
        }
    }
}