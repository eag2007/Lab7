package org.example.client.commands;

import org.example.client.enums.Colors;
import org.example.client.interfaces.Command;

import java.nio.channels.SocketChannel;

import static org.example.client.Client.managerInputOutput;
import static org.example.client.Client.managerParserClient;

public class Help implements Command {

    public void executeCommand(String[] args, SocketChannel serverChannel) {
        if (checkArgs(args)) {
            StringBuilder sb = new StringBuilder();
            sb.append("Справка по командам:\n");
            sb.append("------------------------------------------------------\n");
            for (Command cmd : managerParserClient.getCommands()) {
                sb.append(cmd).append("\n");
            }
            sb.append("------------------------------------------------------");
            managerInputOutput.writeLineIO(sb.toString());
        } else {
            managerInputOutput.writeLineIO("Неверное количество аргументов", Colors.RED);
        }
    }

    public boolean checkArgs(String[] args) {
        return args.length == 0;
    }

    @Override
    public String toString() {
        return "help - выводит справку по каждой из команд";
    }
}