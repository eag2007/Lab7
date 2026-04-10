package org.example.client.commands;

import org.example.client.Client;
import org.example.client.enums.Colors;
import org.example.client.interfaces.Command;
import org.example.client.managers.ManagerSerialize;
import org.example.packet.CommandPacket;
import org.example.packet.ResponsePacket;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import static org.example.client.Client.*;

public class RemoveFirst implements Command {
    public void executeCommand(String[] args, SocketChannel serverChannel) {
        try {
            if (checkArgs(args)) {
                CommandPacket commandPacket = new CommandPacket("remove_first", null, null, Client.getLogin(), Client.getPassword_hash());

                writeModule.writePacketForServer(serverChannel, commandPacket);

                ResponsePacket response = readModule.readResponseForClient(serverChannel);

                if (response != null) {
                    if (response.getStatusCode() == 200) {
                        managerInputOutput.writeLineIO(response.getMessage() + "\n", Colors.GREEN);
                    } else if (response.getStatusCode() == 400) {
                        managerInputOutput.writeLineIO(response.getMessage() + "\n", Colors.YELLOW);
                    } else if (response.getStatusCode() == 500) {
                        managerInputOutput.writeLineIO(response.getMessage() + "\n", Colors.RED);
                    }
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
        return args.length == 0;
    }

    @Override
    public String toString() {
        return "remove_first - удаляет первый элемент коллекции";
    }
}