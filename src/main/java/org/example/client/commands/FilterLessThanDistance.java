package org.example.client.commands;

import org.example.client.Client;
import org.example.client.enums.Colors;
import org.example.client.interfaces.Command;
import org.example.packet.CommandPacket;

import java.io.IOException;
import java.nio.channels.SocketChannel;

import static org.example.client.Client.*;

public class FilterLessThanDistance implements Command {
    public void executeCommand(String[] args, SocketChannel serverChannel) {
        if (!checkArgs(args)) {
            managerInputOutput.writeLineIO("Неправильное количество аргументов или их тип\n", Colors.RED);
            return;
        }
        try {
            writeModule.writePacketForServer(serverChannel,
                    new CommandPacket("filter_less_than_distance", args, null, Client.getLogin(), Client.getPassword_hash()));
        } catch (IOException e) {
            managerInputOutput.writeLineIO("Ошибка отправки: " + e.getMessage() + "\n", Colors.RED);
        }
    }

    public boolean checkArgs(String[] args) {
        if (args.length != 1) {
            return false;
        }
        try {
            Integer.parseInt(args[0]);
            return true;
        } catch (NumberFormatException e) {
            managerInputOutput.writeLineIO("Аргумент должен быть целым числом\n", Colors.RED);
            return false;
        }
    }

    @Override
    public String toString() {
        return "filter_less_than_distance distance - элементы с distance меньше заданного";
    }
}