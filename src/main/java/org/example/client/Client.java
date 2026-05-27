package org.example.client;

import org.example.client.commands.Exit;
import org.example.client.enums.Colors;
import org.example.client.managers.*;
import org.example.client.modules.ReadModule;
import org.example.client.modules.WriteModule;
import org.example.client.threads.PrinterThread;
import org.example.client.threads.ReaderThread;
import org.example.packet.CommandPacket;
import org.example.packet.ResponsePacket;
import org.example.packet.enums.Codes;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.NoSuchElementException;

public class Client {
    public static ManagerValidation managerValidation = new ManagerValidation();
    public static ManagerInputOutput managerInputOutput = ManagerInputOutput.getInstance();
    public static ManagerParserClient managerParserClient = new ManagerParserClient();
    public static ManagerResponseQueue managerResponseQueue = ManagerResponseQueue.getInstance();
    public static SocketChannel server = null;
    public static ReadModule readModule = new ReadModule();
    public static WriteModule writeModule = new WriteModule();

    private static String login = null;
    private static String password_hash = null;
    private static boolean account = false;
    private static int currentPort = 8080;

    private static ReaderThread readerThread = null;
    private static PrinterThread printerThread = null;

    public static void main(String[] args) {
        try {
            managerInputOutput.setCommands(managerParserClient.getCommandNames());

            try {
                currentPort = Integer.parseInt(args[0]);
            } catch (Exception e) {
                currentPort = 8080;
                managerInputOutput.writeLineIO("Порт по умолчанию\n", Colors.YELLOW);
            }

            connect(currentPort);

            while (!account) {
                account = authenticate();
            }
            startBackgroundThreads();

            while (true) {
                try {
                    server.socket().sendUrgentData(0);
                } catch (IOException e) {
                    managerInputOutput.writeLineIO("Сервер умер\n", Colors.YELLOW);
                    stopBackgroundThreads();
                    connect(currentPort);

                    if (login != null && password_hash != null) {
                        if (relogin()) {
                            managerInputOutput.writeLineIO("Сессия восстановлена автоматически\n", Colors.GREEN);
                        } else {
                            managerInputOutput.writeLineIO("Не удалось восстановить сессию. Войдите заново.\n", Colors.YELLOW);
                            account = false;
                            login = null;
                            password_hash = null;
                            while (!account) {
                                account = authenticate();
                            }
                        }
                    } else {
                        account = false;
                        while (!account) {
                            account = authenticate();
                        }
                    }

                    startBackgroundThreads();
                }

                String input = managerInputOutput.readLineIO("Введите команду : ");
                if (input == null || input.isBlank()) {
                    continue;
                }
                managerParserClient.parserCommand(input);

                if (!account) {
                    stopBackgroundThreads();
                    closeServer();
                    connect(currentPort);
                    while (!account) {
                        account = authenticate();
                    }
                    startBackgroundThreads();
                }
            }

        } catch (NoSuchElementException e) {
            shutdown("Завершение работы");
        } catch (RuntimeException e) {
            if (e.getMessage() != null && e.getMessage().contains("EOF")) {
                shutdown("Экстренное завершение работы");
            } else {
                managerInputOutput.writeLineIO("Ошибка во время работы программы\n", Colors.RED);
            }
        } finally {
            stopBackgroundThreads();
            closeServer();
        }
    }

    private static void connect(int port) {
        boolean connected = false;
        while (!connected) {
            try {
                managerInputOutput.writeLineIO("Подключение к серверу...\n", Colors.BLUE);
                server = SocketChannel.open();
                server.configureBlocking(true);
                server.connect(new InetSocketAddress("localhost", port));
                connected = true;
                managerInputOutput.writeLineIO("Вы подключились к серверу\n", Colors.GREEN);
            } catch (IOException e) {
                managerInputOutput.writeLineIO("Сервер не доступен. Нажмите Enter для повторной попытки...\n", Colors.YELLOW);
                String input = managerInputOutput.readLineIO();
                if (input != null && input.trim().equalsIgnoreCase("exit")) {
                    new Exit().executeCommand(new String[]{}, server);
                    System.exit(0);
                }
            }
        }
    }

    private static boolean relogin() {
        try {
            CommandPacket packet = new CommandPacket("login", null, null, login, password_hash);
            writeModule.writePacketForServer(server, packet);
            ResponsePacket response = readModule.readResponseForClient(server);
            if (response != null && response.getStatusCode() == Codes.OK) {
                account = true;
                return true;
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    private static void startBackgroundThreads() {
        stopBackgroundThreads();
        readerThread = new ReaderThread(server, readModule);
        readerThread.start();
        printerThread = new PrinterThread();
        printerThread.start();
    }

    public static void stopBackgroundThreads() {
        if (readerThread != null) {
            readerThread.stopReader();
            readerThread = null;
        }
        if (printerThread != null) {
            printerThread.stopPrinter();
            printerThread = null;
        }
    }

    private static void closeServer() {
        try {
            if (server != null && server.isOpen()) {
                server.close();
            }
        } catch (IOException e) {
            managerInputOutput.writeLineIO("Проблема при разрыве соединения\n", Colors.YELLOW);
        }
    }

    private static void shutdown(String message) {
        managerInputOutput.writeLineIO(message + "\n", Colors.GREEN);
        managerInputOutput.closeIO();
        stopBackgroundThreads();
        closeServer();
    }

    public static String getLogin() {
        return login;
    }

    public static String getPassword_hash() {
        return password_hash;
    }

    public static String enterLogin() {
        return managerInputOutput.readLineIO("Логин: ");
    }

    public static String enterPassword() {
        return managerInputOutput.readPasswordIO("Пароль: ");
    }

    public static void setAccount() {
        account = false;
        managerInputOutput.writeLineIO("Вы вышли из аккаунта " + getLogin() + "\n", Colors.GREEN);
        login = null;
        password_hash = null;
    }

    private static boolean authenticate() {
        try {
            while (true) {
                String data = managerInputOutput.readLineIO("1. Вход\n2. Регистрация\n3. Выход из приложения\nВыбор: ").trim();

                if (data.equals("1")) {
                    String inputLogin = enterLogin().replaceAll("\\s++", " ").trim();
                    String inputPassword = enterPassword().replaceAll("\\s++", " ").trim();

                    try {
                        CommandPacket packet = new CommandPacket("login", null, null, inputLogin, inputPassword);
                        writeModule.writePacketForServer(server, packet);
                        ResponsePacket response = readModule.readResponseForClient(server);

                        if (response != null && response.getStatusCode() == Codes.OK) {
                            Client.login = inputLogin;
                            Client.password_hash = inputPassword;
                            managerInputOutput.writeLineIO("Вы вошли в аккаунт\n", Colors.GREEN);
                            return true;
                        }
                        managerInputOutput.writeLineIO("Ошибка входа: " + (response != null ? response.getMessage() : "нет ответа") + "\n", Colors.RED);
                    } catch (Exception e) {
                        managerInputOutput.writeLineIO("Соединение потеряно, переподключение...\n", Colors.YELLOW);
                        closeServer();
                        connect(currentPort);
                    }

                } else if (data.equals("2")) {
                    String inputLogin = enterLogin().replaceAll("\\s++", " ").trim();
                    String inputPassword = enterPassword().replaceAll("\\s++", " ").trim();

                    if (inputPassword.length() < 4) {
                        managerInputOutput.writeLineIO("Пароль мин. 4 символа\n", Colors.RED);
                        continue;
                    }

                    try {
                        CommandPacket packet = new CommandPacket("register", null, null, inputLogin, inputPassword);
                        writeModule.writePacketForServer(server, packet);
                        ResponsePacket response = readModule.readResponseForClient(server);

                        if (response != null && response.getStatusCode() == Codes.OK) {
                            Client.login = inputLogin;
                            Client.password_hash = inputPassword;
                            managerInputOutput.writeLineIO("Вы зарегистрированы\n", Colors.GREEN);
                            return true;
                        }
                        managerInputOutput.writeLineIO("Ошибка регистрации: " + (response != null ? response.getMessage() : "нет ответа") + "\n", Colors.RED);
                    } catch (Exception e) {
                        managerInputOutput.writeLineIO("Соединение потеряно, переподключение...\n", Colors.YELLOW);
                        closeServer();
                        connect(currentPort);
                    }

                } else if (data.equals("3")) {
                    new Exit().executeCommand(new String[]{}, server);
                    return false;
                }
            }
        } catch (NoSuchElementException e) {
            throw new RuntimeException("EOF");
        }
    }
}