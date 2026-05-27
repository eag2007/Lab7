package org.example.client.threads;

import org.example.client.modules.ReadModule;
import org.example.packet.ResponsePacket;
import org.example.packet.enums.Codes;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SocketChannel;

import static org.example.client.Client.managerResponseQueue;


public class ReaderThread extends Thread {

    private final SocketChannel serverChannel;
    private final ReadModule readModule;

    private volatile boolean running = true;

    public ReaderThread(SocketChannel serverChannel, ReadModule readModule) {
        super("reader-thread");
        this.serverChannel = serverChannel;
        this.readModule = readModule;
        setDaemon(true);
    }

    @Override
    public void run() {
        while (running && !Thread.currentThread().isInterrupted()) {
            try {
                ResponsePacket packet = readModule.readResponseForClient(serverChannel);

                if (packet == null) {
                    break;
                }

                managerResponseQueue.put(packet);

            } catch (ClosedChannelException e) {
                break;
            } catch (IOException e) {
                if (running) {
                    try {
                        managerResponseQueue.put(new ResponsePacket(
                                Codes.ERROR,
                                "Потеряно соединение с сервером: " + e.getMessage(),
                                null
                        ));
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
                break;
            } catch (ClassNotFoundException e) {
                try {
                    managerResponseQueue.put(new ResponsePacket(
                            Codes.ERROR,
                            "Ошибка десериализации ответа: " + e.getMessage(),
                            null
                    ));
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
                break;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    public void stopReader() {
        running = false;
        interrupt();
    }
}