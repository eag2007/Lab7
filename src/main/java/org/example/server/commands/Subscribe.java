package org.example.server.commands;

import org.example.packet.collection.RouteClient;
import org.example.server.Server;
import org.example.server.interfaces.Command;
import org.example.packet.enums.Codes;

import java.io.IOException;
import java.nio.channels.SocketChannel;

import static org.example.server.Server.managerPush;

public class Subscribe implements Command {

    @Override
    public Codes executeCommand(String[] args, RouteClient values, SocketChannel clientChannel, String login, String password_hash) throws IOException {
        if (args.length == 1) {
            if (args[0].equals("false")) {

                Server.writeExecutor(
                        Codes.PUSH,
                        managerPush.deleteSubscribe(login, false) ?
                                "Вы отписались от рассылки уведомлений" : "Вы не были подписаны на рассылку уведомлений",
                        null,
                        clientChannel
                );

                return Codes.OK;
            } else if (args[0].equals("true")) {
                managerPush.addSubscribe(login, false);

                Server.writeExecutor(
                        Codes.PUSH,
                        "Вы подписались на рассылку уведомлений",
                        null,
                        clientChannel
                );

                return Codes.OK;
            } else {

                Server.writeExecutor(
                        Codes.PUSH_ERROR,
                        "Неверный тип аргумента команды",
                        null,
                        clientChannel
                );

                return Codes.PUSH_ERROR;
            }
        }

        Server.writeExecutor(
                Codes.PUSH_ERROR,
                "Неверное количество аргументов",
                null,
                clientChannel
        );

        return Codes.PUSH_ERROR;
    }
}
