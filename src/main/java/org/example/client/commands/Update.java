package org.example.client.commands;

import org.example.client.Client;
import org.example.client.enums.Colors;
import org.example.client.interfaces.Command;
import org.example.packet.CommandPacket;
import org.example.packet.ResponsePacket;
import org.example.packet.collection.Route;
import org.example.packet.collection.RouteClient;

import java.io.IOException;
import java.nio.channels.SocketChannel;

import static org.example.client.Client.*;

public class Update implements Command {
    public void executeCommand(String[] args, SocketChannel serverChannel) {
        try {
            if (checkArgs(args)) {
                CommandPacket commandPacket = new CommandPacket("update", args, null, Client.getLogin(), Client.getPassword_hash());

                writeModule.writePacketForServer(serverChannel, commandPacket);

                ResponsePacket response = readModule.readResponseForClient(serverChannel);

                if (response != null) {
                    if (response.getStatusCode() == 200) {
                        managerInputOutput.writeLineIO("Элемент найден, введите значения полей\n", Colors.GREEN);

                        Route existingRoute = (Route) response.getData();
                        managerInputOutput.writeLineIO("Текущие значения элемента\n", Colors.BLUE);
                        managerInputOutput.writeLineIO("ID: " + existingRoute.getId() + "\n");
                        managerInputOutput.writeLineIO("Name: " + existingRoute.getName() + "\n");
                        managerInputOutput.writeLineIO("Coordinates:\n");
                        managerInputOutput.writeLineIO("  X: " + existingRoute.getCoordinates().getX() + "\n");
                        managerInputOutput.writeLineIO("  Y: " + existingRoute.getCoordinates().getY() + "\n");
                        managerInputOutput.writeLineIO("Creation Date: " + existingRoute.getCreationDate() + "\n");
                        managerInputOutput.writeLineIO("From Location:\n");
                        managerInputOutput.writeLineIO("  X: " + existingRoute.getFrom().getX() + "\n");
                        managerInputOutput.writeLineIO("  Y: " + existingRoute.getFrom().getY() + "\n");
                        managerInputOutput.writeLineIO("  Z: " + existingRoute.getFrom().getZ() + "\n");
                        managerInputOutput.writeLineIO("To Location:\n");
                        managerInputOutput.writeLineIO("  X: " + existingRoute.getTo().getX() + "\n");
                        managerInputOutput.writeLineIO("  Y: " + existingRoute.getTo().getY() + "\n");
                        managerInputOutput.writeLineIO("  Z: " + existingRoute.getTo().getZ() + "\n");
                        managerInputOutput.writeLineIO("Distance: " + existingRoute.getDistance() + "\n");
                        managerInputOutput.writeLineIO("Price: " + existingRoute.getPrice() + "\n");
                        managerInputOutput.writeLineIO("\n");

                        managerInputOutput.writeLineIO("Введите новые значения: \n", Colors.BLUE);
                        RouteClient route = managerValidation.validateFromInput();

                        CommandPacket updatePacket = new CommandPacket("update", args, route, Client.getLogin(), Client.getPassword_hash());

                        writeModule.writePacketForServer(serverChannel, updatePacket);

                        response = readModule.readResponseForClient(serverChannel);

                        if (response != null) {
                            if (response.getStatusCode() == 200) {
                                managerInputOutput.writeLineIO("Сервер: " + response.getMessage() + "\n", Colors.GREEN);
                            } else if (response.getStatusCode() == 400) {
                                managerInputOutput.writeLineIO("Сервер: " + response.getMessage() + "\n", Colors.YELLOW);
                            } else {
                                managerInputOutput.writeLineIO("Сервер: " + response.getMessage() + "\n", Colors.RED);
                            }
                        } else {
                            managerInputOutput.writeLineIO("Сервер не ответил\n", Colors.YELLOW);
                        }
                    } else {
                        managerInputOutput.writeLineIO("Элемент не найден в коллекции пользователя " + getLogin() + "\n", Colors.YELLOW);
                    }
                } else {
                    managerInputOutput.writeLineIO("Сервер не ответил\n", Colors.YELLOW);
                }

            } else {
                managerInputOutput.writeLineIO("Неправильное количество аргументов или их тип\n", Colors.RED);
            }
        } catch (IOException e) {
            managerInputOutput.writeLineIO("Ошибка сериализации или десериализации\n", Colors.RED);
        } catch (ClassNotFoundException e) {
            managerInputOutput.writeLineIO("Ошибка в преобразовании в ResponsePacket\n", Colors.RED);
        } catch (Exception e) {
            managerInputOutput.writeLineIO("Ошибка " + e.getMessage() + "\n", Colors.RED);
        }
    }

    public boolean checkArgs(String[] args) {
        if (args.length == 1) {
            try {
                Long.parseLong(args[0]);
                return true;
            } catch (NumberFormatException e) {
                managerInputOutput.writeLineIO("Аргумент должен быть числом\n", Colors.RED);
                return false;
            }
        }
        return false;
    }


    @Override
    public String toString() {
        return "update - обновляет значение элемента не меняя его id";
    }
}