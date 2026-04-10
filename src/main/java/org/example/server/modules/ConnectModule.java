package org.example.server.modules;

import org.example.server.logger.ServerLogger;

import java.io.IOException;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class ConnectModule {
    private ServerSocketChannel server;
    private Selector selector;

    public void startServer(int port) throws IOException {
        try {
            this.server = ServerSocketChannel.open();
            this.server.configureBlocking(false);
            this.server.bind(new InetSocketAddress(port));

            this.selector = Selector.open();

            this.server.register(this.selector, SelectionKey.OP_ACCEPT);
        } catch (BindException e) {
            ServerLogger.info("Порт занят");
            ServerLogger.error("Порт занят " + e.getMessage());
            throw new IOException("Порт " + port + " занят ", e);
        } catch (Exception e) {
            ServerLogger.error("Что-то случилось при запуске сервера " + e.getMessage());
            throw e;
        }
    }

    public Selector getSelector() {
        return this.selector;
    }

    public SocketChannel acceptConnection() throws IOException {
        SocketChannel client = server.accept();
        if (client != null) {
            client.configureBlocking(false);
            client.register(selector, SelectionKey.OP_READ);
            ServerLogger.info("Клиент подключился {}", client.getRemoteAddress());
        }
        return client;
    }

    public void stopServer() throws IOException {
        if (server != null && server.isOpen())
            server.close();
        ServerLogger.info("Сервер закрыт");

        if (selector != null && selector.isOpen())
            selector.close();
        ServerLogger.info("Селектор закрыт");
    }
}