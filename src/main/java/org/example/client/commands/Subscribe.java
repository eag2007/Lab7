package org.example.client.commands;

import org.example.client.enums.Colors;
import org.example.client.interfaces.Command;
import org.example.packet.CommandPacket;
import org.example.packet.ResponsePacket;
import org.example.packet.enums.Codes;

import java.io.IOException;
import java.nio.channels.SocketChannel;

import static org.example.client.Client.*;

public class Subscribe implements Command {
    @Override
    public void executeCommand(String[] args, SocketChannel serverChannel) {
        if (checkArgs(args)) {
            CommandPacket packet = new CommandPacket(
                    "subscribe",
                    args,
                    null,
                    getLogin(),
                    getPassword_hash()
            );

            try {
                writeModule.writePacketForServer(serverChannel, packet);

                ResponsePacket response = readModule.readResponseForClient(serverChannel);

                if (response.getStatusCode() == Codes.OK) {
                    managerInputOutput.writeLineIO(response.getMessage() + "\n", Colors.RED);
                }

            } catch (IOException e) {
                managerInputOutput.writeLineIO("Не удалось отправить пакет\n");
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        } else {
            managerInputOutput.writeLineIO("Неверное количество аргументов или их тип");
        }
    }

    public boolean checkArgs(String[] args) {
        if (args.length == 1 && (args[0].equals("true")) || args[0].equals("false")) {
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "subscribe [true/false] - подписывает/отписывает пользователя от push-уведомлений";
    }

}
