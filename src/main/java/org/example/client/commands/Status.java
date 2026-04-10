package org.example.client.commands;

import org.example.client.Client;
import org.example.client.enums.Colors;
import org.example.client.interfaces.Command;

import java.nio.channels.SocketChannel;

import static org.example.client.Client.managerInputOutput;

public class Status implements Command {
    public void executeCommand(String[] args, SocketChannel serverChannel) {
        if (Client.getLogin() != null) {
            managerInputOutput.writeLineIO("Вы находитесь в аккаунте под логином: " + Client.getLogin() + "\n", Colors.BLUE);
        } else {
            managerInputOutput.writeLineIO("Вы не находитесь в аккаунте");
        }
    }

    @Override
    public String toString(){
        return "status - выводит информацию о текущем пользователе. ";
    }
}
