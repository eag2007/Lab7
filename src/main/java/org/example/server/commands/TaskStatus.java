package org.example.server.commands;

import org.example.packet.collection.RouteClient;
import org.example.packet.enums.Codes;
import org.example.server.Server;
import org.example.server.interfaces.Command;
import org.example.server.logger.ServerLogger;
import org.example.server.managers.ManagerTask;
import org.example.server.managers.ManagerTask.TaskInfo;

import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;


public class TaskStatus implements Command {

    @Override
    public Codes executeCommand(String[] args, RouteClient value, SocketChannel clientChannel, String login, String password) {
        if (args.length < 1) {
            try {

                Server.writeExecutor(
                        Codes.WARNING,
                        "Использование: task_status {taskId}",
                        null,
                        clientChannel
                );

            } catch (Exception e) {
                ServerLogger.error("Ошибка отправки ответа task_status: {}", e.getMessage());
            }
            return Codes.WARNING;
        }

        String taskId = args[0];

        if (!ManagerTask.taskExists(taskId)) {
            try {

                Server.writeExecutor(
                        Codes.WARNING,
                        "Задача с id '" + taskId + "' не найдена",
                        null,
                        clientChannel
                );

            } catch (Exception e) {
                ServerLogger.error("Ошибка отправки ответа task_status: {}", e.getMessage());
            }
            return Codes.WARNING;
        }

        TaskInfo task = ManagerTask.getTask(taskId);

        Map<String, String> data = new HashMap<>();
        data.put("taskId",      task.getTaskId());
        data.put("command",     task.getCommandName());
        data.put("status",      task.getStatus().name());
        data.put("message",     task.getMessage());
        data.put("created",   String.valueOf(task.getCreated()));
        data.put("finished",  String.valueOf(task.getFinished()));

        try {
            Server.writeExecutor(
                    Codes.OK,
                    "Статус задачи",
                    data,
                    clientChannel
            );

        } catch (Exception e) {
            ServerLogger.error("Ошибка отправки ответа task_status: {}", e.getMessage());
            return Codes.ERROR;
        }

        return Codes.OK;
    }

    @Override
    public String toString() {
        return "task_status {taskId} - показывает статус асинхронной задачи по её id";
    }
}