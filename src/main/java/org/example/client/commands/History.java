package org.example.client.commands;

import org.example.client.enums.Colors;
import org.example.client.interfaces.Command;

import java.nio.channels.SocketChannel;
import java.util.List;

import static org.example.client.Client.managerInputOutput;
import static org.example.client.Client.managerParserClient;

public class History implements Command {

    public void executeCommand(String[] args, SocketChannel serverChannel) {
        if (checkArgs(args)) {
            List<String> historyCommands = managerParserClient.getHistoryCommands();
            managerInputOutput.writeLineIO("Список последних 14 команд:\n");
            managerInputOutput.writeLineIO("-----------------------\n");
            for (String cmd : historyCommands) {
                managerInputOutput.writeLineIO(cmd + "\n");
            }
            managerInputOutput.writeLineIO("-----------------------\n");
        } else {
            managerInputOutput.writeLineIO("Неверное количество аргументов\n", Colors.RED);
        }
    }

    public boolean checkArgs(String[] args) {
        return args.length == 0;
    }

    @Override
    public String toString() {
        return "history - выводит 14 последних команд";
    }
}