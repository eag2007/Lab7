package org.example.server.interfaces;

import org.example.packet.collection.Route;
import org.example.packet.collection.RouteClient;

import java.io.IOException;
import java.nio.channels.SocketChannel;

public interface Command {
    String toString();
    int executeCommand(String[] args, RouteClient values, SocketChannel clientChannel, String login, String password_hash) throws IOException;
}