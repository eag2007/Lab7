package org.example.client.commands;

import org.example.client.Client;
import org.example.client.enums.Colors;
import org.example.client.interfaces.Command;
import org.example.packet.CommandPacket;
import org.example.packet.collection.RouteClient;

import java.io.IOException;
import java.nio.channels.SocketChannel;

import static org.example.client.Client.*;

public class Add implements Command {
    public void executeCommand(String[] args, SocketChannel serverChannel) {
        if (!checkArgs(args)) {
            managerInputOutput.writeLineIO("Неправильное количество аргументов\n", Colors.RED);
            return;
        }

        RouteClient route = managerInputOutput.isScriptMode()
                ? managerValidation.validateFromScript()
                : managerValidation.validateFromInput();

        if (route == null) {
            managerInputOutput.writeLineIO("Объект не создан\n", Colors.RED);
            return;
        }

        try {
            writeModule.writePacketForServer(serverChannel,
                    new CommandPacket("add", null, route, Client.getLogin(), Client.getPassword_hash()));
        } catch (IOException e) {
            managerInputOutput.writeLineIO("Ошибка отправки: " + e.getMessage() + "\n", Colors.RED);
        }
    }

    public boolean checkArgs(String[] args) {
        return args.length == 0 || (args.length == 1 && args[0].equals("Route"));
    }

    @Override
    public String toString() {
        return "add - добавляет новый элемент в коллекцию";
    }
}