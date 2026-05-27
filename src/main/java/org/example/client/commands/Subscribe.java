package org.example.client.commands;

import org.example.client.Client;
import org.example.client.enums.Colors;
import org.example.client.interfaces.Command;
import org.example.packet.CommandPacket;

import java.io.IOException;
import java.nio.channels.SocketChannel;

import static org.example.client.Client.*;

public class Subscribe implements Command {
    public void executeCommand(String[] args, SocketChannel serverChannel) {
        if (!checkArgs(args)) {
            managerInputOutput.writeLineIO("Неправильное количество аргументов или их тип\n", Colors.RED);
            return;
        }
        try {
            writeModule.writePacketForServer(serverChannel,
                    new CommandPacket("subscribe", args, null, Client.getLogin(), Client.getPassword_hash()));
        } catch (IOException e) {
            managerInputOutput.writeLineIO("Ошибка отправки: " + e.getMessage() + "\n", Colors.RED);
        }
    }

    public boolean checkArgs(String[] args) {
        return args.length == 1 && (args[0].equals("true") || args[0].equals("false"));
    }

    @Override
    public String toString() {
        return "subscribe";
    }
}