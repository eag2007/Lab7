package org.example.client.commands;

import org.example.client.enums.Colors;
import org.example.client.interfaces.Command;
import org.example.packet.CommandPacket;
import org.example.packet.ResponsePacket;
import org.example.packet.enums.Codes;

import java.nio.channels.SocketChannel;
import java.util.Map;

import static org.example.client.Client.*;

public class GenerateData implements Command {

    @Override
    public void executeCommand(String[] args, SocketChannel serverChannel) {
        if (!checkArgs(args)) {
            managerInputOutput.writeLineIO("Использование: generate_data {count}\n", Colors.RED);
            return;
        }

        try {
            CommandPacket commandPacket = new CommandPacket("generate_data", args, null, getLogin(), getPassword_hash());
            writeModule.writePacketForServer(serverChannel, commandPacket);

            ResponsePacket response = readModule.readResponseForClient(serverChannel);

            if (response == null) {
                managerInputOutput.writeLineIO("Нет ответа от сервера\n", Colors.RED);
                return;
            }

            if (response.getStatusCode() == Codes.OK) {
                Map<String, String> data = (Map<String, String>) response.getData();

                String taskId = data != null ? data.get("taskId") : "?";
                String count  = data != null ? data.get("count")  : args[0];

                managerInputOutput.writeLineIO("  Задача запущена!\n", Colors.GREEN);
                managerInputOutput.writeLineIO("  Команда  : generate_data\n");
                managerInputOutput.writeLineIO("  Элементов: " + count + "\n");
                managerInputOutput.writeLineIO("  Task ID  : " + taskId + "\n", Colors.YELLOW);
                managerInputOutput.writeLineIO("  Проверить статус: task_status " + taskId + "\n", Colors.BLUE);
            } else {
                managerInputOutput.writeLineIO("Ошибка (" + response.getStatusCode() + "): " + response.getMessage() + "\n", Colors.RED);
            }

        } catch (Exception e) {
            managerInputOutput.writeLineIO("Ошибка: " + e.getMessage() + "\n", Colors.RED);
        }
    }

    public boolean checkArgs(String[] args) {
        if (args.length != 1) return false;
        try {
            int n = Integer.parseInt(args[0]);
            return n > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public String toString() {
        return "generate_data {count} - асинхронно генерирует count элементов на сервере";
    }
}