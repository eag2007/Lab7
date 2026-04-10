package org.example.client.commands;

import org.example.client.Client;
import org.example.client.enums.Colors;
import org.example.client.interfaces.Command;
import org.example.client.managers.ManagerSerialize;
import org.example.packet.CommandPacket;
import org.example.packet.ResponsePacket;
import org.example.packet.collection.RouteClient;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import static org.example.client.Client.*;

public class AddIfMax implements Command {
    public void executeCommand(String[] args, SocketChannel serverChannel) {
        if (checkArgs(args)) {
            if (!managerInputOutput.isScriptMode()) {
                RouteClient route = managerValidation.validateFromInput();

                CommandPacket commandPacket = new CommandPacket("add_if_max", null, route, Client.getLogin(), Client.getPassword_hash());

                try {
                    writeModule.writePacketForServer(serverChannel, commandPacket);

                    ResponsePacket response = readModule.readResponseForClient(serverChannel);

                    if (response != null) {
                        if (response.getStatusCode() == 200) {
                            managerInputOutput.writeLineIO("Сервер: " + response.getMessage() + "\n", Colors.GREEN);
                        }

                        if (response.getStatusCode() == 500)
                            managerInputOutput.writeLineIO("Сервер: " + response.getMessage() + "\n", Colors.RED);
                        }
                } catch (IOException e) {
                    managerInputOutput.writeLineIO("Ошибка отправки\n", Colors.RED);
                } catch (ClassNotFoundException e) {
                    managerInputOutput.writeLineIO("Ошибка десериализации\n", Colors.RED);
                } catch (Exception e) {
                    managerInputOutput.writeLineIO("Ошибка: " + e.getMessage() + "\n", Colors.RED);
                }
            } else {
                RouteClient route = managerValidation.validateFromScript();

                if (route == null) {
                    managerInputOutput.writeLineIO("Объект не создан\n", Colors.RED);
                    return;
                }

                CommandPacket commandPacket = new CommandPacket("add", null, route, Client.getLogin(), Client.getPassword_hash());

                try {
                    writeModule.writePacketForServer(serverChannel, commandPacket);

                    ResponsePacket response = readModule.readResponseForClient(serverChannel);
                    if (response != null) {
                        if (response.getStatusCode() == 200) {
                            managerInputOutput.writeLineIO("Сервер: " + response.getMessage() + " ID:"
                                    + response.getData() + "\n", Colors.GREEN);
                        }

                        if (response.getStatusCode() == 500) {
                            managerInputOutput.writeLineIO("Сервер: " + response.getMessage() + "\n", Colors.RED);
                        }
                    } else {
                        managerInputOutput.writeLineIO("Сервер ничего не вернул\n", Colors.YELLOW);
                    }
                } catch (ClassNotFoundException e) {
                    managerInputOutput.writeLineIO("Ошибка десириализации\n", Colors.RED);
                } catch (Exception e) {
                    managerInputOutput.writeLineIO("Ошибка: " + e.getMessage() + "\n", Colors.RED);
                }
            }
        } else {
            managerInputOutput.writeLineIO("Неправильное количество элементов\n", Colors.RED);
        }
    }

    public boolean checkArgs(String[] args) {
        return args.length == 0 || (args.length == 1 && args[0].equals("Route"));
    }

    @Override
    public String toString() {
        return "add_if_max {element} - добавляет новый элемент, если он больше максимального";
    }
}