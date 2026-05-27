package org.example.client.commands;

import org.example.client.Client;
import org.example.client.enums.Colors;
import org.example.client.interfaces.Command;
import org.example.client.managers.ManagerResponseQueue;
import org.example.packet.CommandPacket;
import org.example.packet.ResponsePacket;
import org.example.packet.collection.Route;
import org.example.packet.collection.RouteClient;
import org.example.packet.enums.Codes;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.example.client.Client.*;

public class Update implements Command {

    private static final long TIMEOUT_SEC = 10;

    public void executeCommand(String[] args, SocketChannel serverChannel) {
        if (!checkArgs(args)) {
            managerInputOutput.writeLineIO("Неправильное количество аргументов или их тип\n", Colors.RED);
            return;
        }

        ManagerResponseQueue queue = ManagerResponseQueue.getInstance();

        try {
            CompletableFuture<ResponsePacket> future = queue.expectResponse();
            writeModule.writePacketForServer(serverChannel,
                    new CommandPacket("update", args, null, Client.getLogin(), Client.getPassword_hash()));

            ResponsePacket response = future.get(TIMEOUT_SEC, TimeUnit.SECONDS);

            if (response.getStatusCode() != Codes.OK) {
                managerInputOutput.writeLineIO(
                        "Элемент не найден у пользователя " + getLogin() + "\n", Colors.YELLOW);
                return;
            }

            Route r = (Route) response.getData();
            String info = "ID: " + r.getId() + "\n" +
                    "Name: " + r.getName() + "\n" +
                    "X: " + r.getCoordinates().getX() + "\n" +
                    "Y: " + r.getCoordinates().getY() + "\n" +
                    "From X: " + r.getFrom().getX() + "\n" +
                    "From Y: " + r.getFrom().getY() + "\n" +
                    "From Z: " + r.getFrom().getZ() + "\n" +
                    "To X: " + r.getTo().getX() + "\n" +
                    "To Y: " + r.getTo().getY() + "\n" +
                    "To Z: " + r.getTo().getZ() + "\n" +
                    "Distance: " + r.getDistance() + "\n" +
                    "Price: " + r.getPrice();
            managerInputOutput.writeLineIO("Текущие значения:", Colors.BLUE);
            managerInputOutput.writeLineIO(info);

            managerInputOutput.writeLineIO("Введите новые значения:", Colors.BLUE);
            RouteClient newRoute = managerValidation.validateFromInput();

            future = queue.expectResponse();
            writeModule.writePacketForServer(serverChannel,
                    new CommandPacket("update", args, newRoute, Client.getLogin(), Client.getPassword_hash()));

            response = future.get(TIMEOUT_SEC, TimeUnit.SECONDS);

            switch (response.getStatusCode()) {
                case OK -> managerInputOutput.writeLineIO("Сервер: " + response.getMessage() + "\n", Colors.GREEN);
                case WARNING ->
                        managerInputOutput.writeLineIO("Сервер: " + response.getMessage() + "\n", Colors.YELLOW);
                default -> managerInputOutput.writeLineIO("Сервер: " + response.getMessage() + "\n", Colors.RED);
            }

        } catch (TimeoutException e) {
            queue.cancelExpected();
            managerInputOutput.writeLineIO("Сервер не ответил за " + TIMEOUT_SEC + " секунд\n", Colors.RED);
        } catch (IOException e) {
            queue.cancelExpected();
            managerInputOutput.writeLineIO("Ошибка отправки: " + e.getMessage() + "\n", Colors.RED);
        } catch (Exception e) {
            queue.cancelExpected();
            managerInputOutput.writeLineIO("Ошибка: " + e.getMessage() + "\n", Colors.RED);
        }
    }

    public boolean checkArgs(String[] args) {
        if (args.length != 1) return false;
        try {
            Long.parseLong(args[0]);
            return true;
        } catch (NumberFormatException e) {
            managerInputOutput.writeLineIO("Аргумент должен быть числом\n", Colors.RED);
            return false;
        }
    }

    @Override
    public String toString() {
        return "update - обновляет значение элемента не меняя его id";
    }
}