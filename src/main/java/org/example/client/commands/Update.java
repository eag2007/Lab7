package org.example.client.commands;

import org.example.client.Client;
import org.example.client.enums.Colors;
import org.example.client.interfaces.Command;
import org.example.client.managers.ResponseQueue;
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

        ResponseQueue queue = ResponseQueue.getInstance();

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
            managerInputOutput.writeLineIO("Текущие значения:\n", Colors.BLUE);
            managerInputOutput.writeLineIO("ID: "       + r.getId()                    + "\n");
            managerInputOutput.writeLineIO("Name: "     + r.getName()                  + "\n");
            managerInputOutput.writeLineIO("X: "        + r.getCoordinates().getX()    + "\n");
            managerInputOutput.writeLineIO("Y: "        + r.getCoordinates().getY()    + "\n");
            managerInputOutput.writeLineIO("From X: "   + r.getFrom().getX()           + "\n");
            managerInputOutput.writeLineIO("From Y: "   + r.getFrom().getY()           + "\n");
            managerInputOutput.writeLineIO("From Z: "   + r.getFrom().getZ()           + "\n");
            managerInputOutput.writeLineIO("To X: "     + r.getTo().getX()             + "\n");
            managerInputOutput.writeLineIO("To Y: "     + r.getTo().getY()             + "\n");
            managerInputOutput.writeLineIO("To Z: "     + r.getTo().getZ()             + "\n");
            managerInputOutput.writeLineIO("Distance: " + r.getDistance()              + "\n");
            managerInputOutput.writeLineIO("Price: "    + r.getPrice()                 + "\n\n");

            managerInputOutput.writeLineIO("Введите новые значения:\n", Colors.BLUE);
            RouteClient newRoute = managerValidation.validateFromInput();

            future = queue.expectResponse();
            writeModule.writePacketForServer(serverChannel,
                    new CommandPacket("update", args, newRoute, Client.getLogin(), Client.getPassword_hash()));

            response = future.get(TIMEOUT_SEC, TimeUnit.SECONDS);

            switch (response.getStatusCode()) {
                case OK      -> managerInputOutput.writeLineIO("Сервер: " + response.getMessage() + "\n", Colors.GREEN);
                case WARNING -> managerInputOutput.writeLineIO("Сервер: " + response.getMessage() + "\n", Colors.YELLOW);
                default      -> managerInputOutput.writeLineIO("Сервер: " + response.getMessage() + "\n", Colors.RED);
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
        try { Long.parseLong(args[0]); return true; }
        catch (NumberFormatException e) {
            managerInputOutput.writeLineIO("Аргумент должен быть числом\n", Colors.RED);
            return false;
        }
    }

    @Override
    public String toString() { return "update - обновляет значение элемента не меняя его id"; }
}