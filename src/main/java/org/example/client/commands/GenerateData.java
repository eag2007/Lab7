package org.example.client.commands;

import org.example.client.enums.Colors;
import org.example.client.interfaces.Command;
import org.example.packet.CommandPacket;

import java.io.IOException;
import java.nio.channels.SocketChannel;

import static org.example.client.Client.*;

public class GenerateData implements Command {
    public void executeCommand(String[] args, SocketChannel serverChannel) {
        if (!checkArgs(args)) {
            managerInputOutput.writeLineIO("Использование: generate_data {count}\n", Colors.RED);
            return;
        }
        try {
            writeModule.writePacketForServer(serverChannel,
                    new CommandPacket("generate_data", args, null, getLogin(), getPassword_hash()));
        } catch (IOException e) {
            managerInputOutput.writeLineIO("Ошибка отправки: " + e.getMessage() + "\n", Colors.RED);
        }
    }

    public boolean checkArgs(String[] args) {
        if (args.length != 1) {
            return false;
        }
        try {
            return Integer.parseInt(args[0]) > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public String toString() {
        return "generate_data {count} - асинхронно генерирует count элементов";
    }
}