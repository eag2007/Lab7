package org.example.server.interfaces;

import org.example.packet.collection.RouteClient;
import org.example.packet.enums.Codes;

import java.io.IOException;
import java.nio.channels.SocketChannel;

public interface Command {
    String toString();
    Codes executeCommand(String[] args, RouteClient values, SocketChannel clientChannel, String login, String password_hash) throws IOException;
}