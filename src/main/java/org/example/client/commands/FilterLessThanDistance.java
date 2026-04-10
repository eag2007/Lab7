package org.example.client.commands;

import org.example.client.Client;
import org.example.client.enums.Colors;
import org.example.client.interfaces.Command;
import org.example.client.managers.ManagerSerialize;
import org.example.packet.collection.Route;
import org.example.packet.CommandPacket;
import org.example.packet.ResponsePacket;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.List;

import static org.example.client.Client.*;

public class FilterLessThanDistance implements Command {
    public void executeCommand(String[] args, SocketChannel serverChannel) {
        if (checkArgs(args)) {
            CommandPacket commandPacket = new CommandPacket("filter_less_than_distance", args, null, Client.getLogin(), Client.getPassword_hash());

            try {
                writeModule.writePacketForServer(serverChannel, commandPacket);

                ResponsePacket response = readModule.readResponseForClient(serverChannel);

                if (response != null) {
                    if (response.getStatusCode() == 200) {
                        List<Route> routes = (List<Route>) response.getData();

                        String header = String.format("%-3s | %-15s | %-3s | %-3s | %-6s | %-6s | %-4s | %-6s | %-6s | %-4s | %-5s | %-10s | %-10s",
                                "ID", "Name", "X", "Y", "Date", "FromX", "FromY", "FromZ", "ToX", "ToY", "ToZ", "Distance", "Price");

                        managerInputOutput.writeLineIO(header + "\n");
                        managerInputOutput.writeLineIO("-".repeat(header.length()) + "\n");

                        for (Route route : routes) {
                            String line = String.format("%-3s | %-15s | %-3s | %-3s | %-6s | %-6s | %-4s | %-6s | %-6s | %-4s | %-5s | %-10s | %-10s",
                                    route.getId(), route.getName(), route.getCoordinates().getX(), route.getCoordinates().getY(),
                                    route.getCreationDate(), route.getFrom().getX(), route.getFrom().getY(), route.getFrom().getZ(),
                                    route.getTo().getX(), route.getTo().getY(), route.getTo().getZ(), route.getDistance(), route.getPrice());
                            managerInputOutput.writeLineIO(line + "\n");
                        }
                    } else if (response.getStatusCode() == 400) {
                        managerInputOutput.writeLineIO("Сервер: " + response.getMessage() + "\n", Colors.YELLOW);
                    } else {
                        managerInputOutput.writeLineIO("Сервер: " + response.getMessage() + "\n", Colors.RED);
                    }
                }
            } catch (IOException e) {
                managerInputOutput.writeLineIO("Ошибка отправки\n", Colors.RED);
            } catch (ClassNotFoundException e) {
                managerInputOutput.writeLineIO("Ошибка десериализации\n", Colors.RED);
            } catch (Exception e) {
                managerInputOutput.writeLineIO("Ошибка: " + e.getMessage() + "\n", Colors.RED);
            }
        } else {
            managerInputOutput.writeLineIO("Неправильное количество аргументов или их тип\n", Colors.RED);
        }
    }

    public boolean checkArgs(String[] args) {
        if (args.length == 1) {
            try {
                Integer.parseInt(args[0]);
                return true;
            } catch (NumberFormatException e) {
                managerInputOutput.writeLineIO("Ошибка: аргумент должен быть целым числом\n", Colors.RED);
                return false;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "filter_less_than_distance distance - выводит элементы с distance меньше заданного";
    }
}