package org.example.client.commands;

import org.example.client.enums.Colors;
import org.example.client.interfaces.Command;
import org.example.packet.CommandPacket;

import java.io.IOException;
import java.nio.channels.SocketChannel;

import static org.example.client.Client.*;

public class See implements Command {
    public void executeCommand(String[] args, SocketChannel serverChannel) {
        if (args.length > 2) {
            managerInputOutput.writeLineIO("Использование: see [page] [pageSize]\n", Colors.RED);
            return;
        }
        try {
            writeModule.writePacketForServer(serverChannel,
                    new CommandPacket("see", args, null, getLogin(), getPassword_hash()));
        } catch (IOException e) {
            managerInputOutput.writeLineIO("Ошибка отправки: " + e.getMessage() + "\n", Colors.RED);
        }
    }

    @Override
    public String toString() { return "see [page] [pageSize] - показать коллекцию постранично"; }
}