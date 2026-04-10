package org.example.client.interfaces;

import java.nio.channels.SocketChannel;

public interface Command {
    String toString();
    void executeCommand(String[] args, SocketChannel serverChannel);
}