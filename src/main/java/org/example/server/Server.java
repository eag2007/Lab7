package org.example.server;

import org.example.packet.CommandPacket;
import org.example.packet.collection.Route;
import org.example.server.logger.ServerLogger;
import org.example.server.managers.ManagerCollections;
import org.example.server.managers.ManagerDataBase;
import org.example.server.managers.ManagerParserServer;
import org.example.server.modules.ConnectModule;
import org.example.server.modules.ReadModule;
import org.example.server.modules.WriteModule;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    public static ManagerCollections managerCollections = new ManagerCollections();
    public static ManagerParserServer managerParserServer = new ManagerParserServer();
    public static ManagerDataBase managerDataBase = ManagerDataBase.getInstance();

    public static ReadModule readModule = new ReadModule();
    public static WriteModule writeModule = new WriteModule();
    public static ConnectModule connectModule = new ConnectModule();

    private static final int DEFAULT_PORT = 8080;

    private static final ExecutorService READ = Executors.newCachedThreadPool();
    private static final ExecutorService PROCESS = Executors.newFixedThreadPool(10);
    private static final ExecutorService WRITE = Executors.newFixedThreadPool(8);

    public static void main(String[] args) {
        try {
            ServerLogger.info("Запуск сервера");

            PriorityQueue<Route> routesDB = managerDataBase.getRoutesDB();
            managerCollections.loadAllRoutes(routesDB);
            ServerLogger.info("Загружено элементов {} из БД", managerCollections.getSizeCollections());

            int port = parsePortFromArgs(args);
            connectModule.startServer(port);
            ServerLogger.info("Сервер запущен на порту {}", port);

            ifCloseServer();
            inputOutputServer();

            while (true) {
                int countChannels = connectModule.getSelector().select();
                if (countChannels == 0) continue;

                Set<SelectionKey> selectedKeys = connectModule.getSelector().selectedKeys();
                Iterator<SelectionKey> keyIterator = selectedKeys.iterator();

                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();

                    if (key.isAcceptable()) {
                        connectModule.acceptConnection();
                    }

                    if (key.isReadable()) {
                        SocketChannel client = (SocketChannel) key.channel();
                        SelectionKey currentKey = key;

                        /// ОТМЕНЯЕМ КЛЮЧ
                        key.cancel();


                        /// ЧТЕНИЕ ПАКЕТА
                        READ.submit(() -> {
                            try {
                                CommandPacket packet = readModule.readPacketForServer(client);

                                if (packet != null && packet.getValues() == null &&
                                        packet.getArgs() == null && packet.getType() == null) {
                                    try {
                                        client.close();
                                    } catch (IOException ignored) {
                                    }
                                    ServerLogger.info("Клиент отключился");
                                    return;
                                }

                                /// ПЕРЕРЕГИСТРАЦИЯ КАНАЛА
                                client.register(connectModule.getSelector(), SelectionKey.OP_READ);
                                connectModule.getSelector().wakeup();


                                /// ОБРАБОТКА КОМАНД
                                PROCESS.submit(() -> {
                                    int code = managerParserServer.parserCommand(packet, client);
                                    try {
                                        ServerLogger.info("Код выполнения команды {} от {}", code, client.getRemoteAddress());
                                    } catch (IOException e) {
                                        ServerLogger.info("Код выполнения команды {} от unknown", code);
                                    }
                                });
                                /// ОБРАБОТКА КОМАНД


                            } catch (IOException e) {
                                try {
                                    currentKey.cancel();
                                    client.close();
                                } catch (IOException ex) {
                                    ServerLogger.info("Клиент отключился (адрес недоступен)");
                                }
                                ServerLogger.info("Клиент отключился");
                            }
                        });
                        /// ЧТЕНИЕ ПАКЕТА



                    }
                    keyIterator.remove();
                }
            }
        } catch (IOException e) {
            ServerLogger.error("Ошибка: {}", e.getMessage());
        } finally {
            try {
                connectModule.stopServer();

                if (managerDataBase != null) {
                    managerDataBase.close();
                }

            } catch (IOException e) {
                ServerLogger.error("Ошибка: {}", e.getMessage());
            }
        }
    }

    public static void inputOutputServer() {
        new Thread(() -> {
            Scanner scanner = new Scanner(System.in);
            try {
                while (true) {
                    System.out.print("\u001B[34mКоманда для сервера$: \u001B[0m\n");
                    String input = scanner.nextLine().trim();
                     if (input.equalsIgnoreCase("exit")) {
                        ServerLogger.info("Отключение сервера");

                        if (managerDataBase != null) {
                            managerDataBase.close();
                        }

                        System.exit(0);
                    }
                }
            } catch (NoSuchElementException e) {
                if (managerDataBase != null) {
                    managerDataBase.close();
                }

                System.exit(0);
            }
        }).start();
    }

    public static void ifCloseServer() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            ServerLogger.info("Завершение работы сервера, Экстренное сохранение");
            try {

                if (managerDataBase != null) {
                    managerDataBase.close();
                }

            } catch (Exception e) {
                ServerLogger.error("Ошибка: {}", e.getMessage());
            }
        }));
    }

    public static int parsePortFromArgs(String[] args) {
        if (args.length > 0) {
            try {
                int port = Integer.parseInt(args[0]);
                if (port < 1 || port > 65535) return DEFAULT_PORT;
                return port;
            } catch (NumberFormatException e) {
                return DEFAULT_PORT;
            }
        }
        return DEFAULT_PORT;
    }

    public static ExecutorService getRead() {
        return READ;
    }

    public static ExecutorService getProcess() {
        return PROCESS;
    }

    public static ExecutorService getWrite() {
        return WRITE;
    }
}