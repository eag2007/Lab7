package org.example.client.commands;

import org.example.client.enums.Colors;
import org.example.client.interfaces.Command;
import org.example.packet.CommandPacket;
import org.example.packet.ResponsePacket;
import org.example.packet.enums.Codes;

import java.nio.channels.SocketChannel;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import static org.example.client.Client.*;

public class TaskStatus implements Command {

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault());

    @Override
    public void executeCommand(String[] args, SocketChannel serverChannel) {
        if (!checkArgs(args)) {
            managerInputOutput.writeLineIO("Использование: task_status {taskId}\n", Colors.RED);
            return;
        }

        try {
            CommandPacket commandPacket = new CommandPacket("task_status", args, null, getLogin(), getPassword_hash());
            writeModule.writePacketForServer(serverChannel, commandPacket);

            ResponsePacket response = readModule.readResponseForClient(serverChannel);

            if (response == null) {
                managerInputOutput.writeLineIO("Нет ответа от сервера\n", Colors.RED);
                return;
            }

            if (response.getStatusCode() == Codes.OK) {
                @SuppressWarnings("unchecked")
                Map<String, String> data = (Map<String, String>) response.getData();

                if (data == null) {
                    managerInputOutput.writeLineIO(response.getMessage() + "\n", Colors.YELLOW);
                    return;
                }

                String status    = data.getOrDefault("status", "UNKNOWN");
                String message   = data.getOrDefault("message", "");
                String command   = data.getOrDefault("command", "");
                String taskId    = data.getOrDefault("taskId", args[0]);
                String createdAt = data.getOrDefault("created", "-1");
                String finishedAt = data.getOrDefault("finished", "-1");

                Colors statusColor = switch (status) {
                    case "DONE"        -> Colors.GREEN;
                    case "ERROR"       -> Colors.RED;
                    case "IN_PROGRESS" -> Colors.BLUE;
                    default            -> Colors.YELLOW;
                };

                managerInputOutput.writeLineIO("┌─ Статус задачи ──────────────────────────\n");
                managerInputOutput.writeLineIO("│ Task ID : " + taskId + "\n");
                managerInputOutput.writeLineIO("│ Команда : " + command + "\n");
                managerInputOutput.writeLineIO("│ Статус  : ", Colors.WHITE);
                managerInputOutput.writeLineIO(status + "\n", statusColor);
                managerInputOutput.writeLineIO("│ Сообщение: " + message + "\n");

                try {
                    long created = Long.parseLong(createdAt);
                    if (created > 0) {
                        managerInputOutput.writeLineIO("│ Создана : " + FMT.format(Instant.ofEpochMilli(created)) + "\n");
                    }
                    long finished = Long.parseLong(finishedAt);
                    if (finished > 0) {
                        managerInputOutput.writeLineIO("│ Завершена: " + FMT.format(Instant.ofEpochMilli(finished)) + "\n");
                        long elapsed = finished - created;
                        managerInputOutput.writeLineIO("│ Время   : " + elapsed + " мс\n");
                    }
                } catch (NumberFormatException ignored) {}

                managerInputOutput.writeLineIO("└──────────────────────────────────────────\n");

            } else if (response.getStatusCode() == Codes.WARNING) {
                managerInputOutput.writeLineIO("Задача не найдена: " + args[0] + "\n", Colors.RED);
            } else {
                managerInputOutput.writeLineIO("Ошибка (" + response.getStatusCode() + "): " + response.getMessage() + "\n", Colors.RED);
            }

        } catch (Exception e) {
            managerInputOutput.writeLineIO("Ошибка: " + e.getMessage() + "\n", Colors.RED);
        }
    }

    public boolean checkArgs(String[] args) {
        return args.length == 1 && !args[0].isBlank();
    }

    @Override
    public String toString() {
        return "task_status {taskId} - показывает статус асинхронной задачи по её id";
    }
}