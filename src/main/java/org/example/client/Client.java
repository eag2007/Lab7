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
    public static SocketChannel server = null;
    public static ReadModule readModule = new ReadModule();
    public static WriteModule writeModule = new WriteModule();

    private static String login = null;
    private static String password_hash = null;
    private static boolean account = false;

    private static ReaderThread readerThread = null;
    private static PrinterThread printerThread = null;

    public static void main(String[] args) {
        try {
            managerInputOutput.setCommands(managerParserClient.getCommandNames());

            int port;
            try {
                port = Integer.parseInt(args[0]);
            } catch (Exception e) {
                port = 8080;
                managerInputOutput.writeLineIO("Порт по умолчанию\n", Colors.YELLOW);
            }

            connect(port);

            while (!account) {
                account = authenticate();
            }

            readerThread = new ReaderThread(server, readModule);
            readerThread.start();

            printerThread = new PrinterThread();
            printerThread.start();

            while (true) {
                try {
                    server.socket().sendUrgentData(0);
                } catch (IOException e) {
                    managerInputOutput.writeLineIO("Сервер умер\n", Colors.YELLOW);
                    stopBackgroundThreads();
                    connect(port);
                    account = false;
                    while (!account) {
                        account = authenticate();
                    }
                    readerThread = new ReaderThread(server, readModule);
                    readerThread.start();
                    printerThread = new PrinterThread();
                    printerThread.start();
                }

                String input = managerInputOutput.readLineIO("Введите команду : ");
                if (input == null || input.isBlank()) continue;
                managerParserClient.parserCommand(input);
            }

        } catch (NoSuchElementException e) {
            shutdown("Завершение работы");
        } catch (RuntimeException e) {
            if (e.getMessage() != null && e.getMessage().contains("EOF")) {
                shutdown("Завершение работы");
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

    private static void stopBackgroundThreads() {
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
                managerInputOutput.writeLineIO("Соединение с сервером закрыто\n", Colors.GREEN);
            }
        } catch (IOException e) {
            managerInputOutput.writeLineIO("Проблема при разрыве соединения\n", Colors.YELLOW);
        }
    }

    private static void shutdown(String message) {
        managerInputOutput.writeLineIO(message + "\n", Colors.GREEN);
        managerInputOutput.closeIO();
        closeServer();
    }

    public static String getLogin() { return login; }

    public static String getPassword_hash() { return password_hash; }

    public static String enterLogin() {
        return managerInputOutput.readLineIO("Логин: ");
    }

    public static String enterPassword() {
        return managerInputOutput.readLineIO("Пароль: ");
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

                } else if (data.equals("2")) {
                    String inputLogin = enterLogin().replaceAll("\\s++", " ").trim();
                    String inputPassword = enterPassword().replaceAll("\\s++", " ").trim();

                    if (inputPassword.length() < 4) {
                        managerInputOutput.writeLineIO("Пароль мин. 4 символа\n", Colors.RED);
                        continue;
                    }

                    CommandPacket packet = new CommandPacket("register", null, null, inputLogin, inputPassword);
                    writeModule.writePacketForServer(server, packet);
                    ResponsePacket response = readModule.readResponseForClient(server);

                    if (response != null && response.getStatusCode() == Codes.OK) {
                        Client.login = inputLogin;
                        Client.password_hash = inputPassword;
                        managerInputOutput.writeLineIO("Вы вошли в аккаунт\n", Colors.GREEN);
                        return true;
                    }
                    managerInputOutput.writeLineIO("Ошибка регистрации: " + (response != null ? response.getMessage() : "нет ответа") + "\n", Colors.RED);

                } else if (data.equals("3")) {
                    new Exit().executeCommand(new String[]{}, server);
                    return false;
                }
            }
        } catch (NoSuchElementException e) {
            throw new RuntimeException("EOF");
        } catch (Exception e) {
            managerInputOutput.writeLineIO("Ошибка: " + e.getMessage() + "\n", Colors.RED);
            return false;
        }
    }
}