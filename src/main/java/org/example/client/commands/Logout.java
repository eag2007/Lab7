package org.example.client.commands;

import org.example.client.Client;
import org.example.client.interfaces.Command;

import java.nio.channels.SocketChannel;

public class Logout implements Command {
    public void executeCommand(String[] args, SocketChannel serverChannel) {
        Client.setAccount();
    }

    @Override
    public String toString(){
        return "logout - выходит из текущего юзера";
    }
}

