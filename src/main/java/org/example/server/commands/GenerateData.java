package org.example.server.commands;

import org.example.packet.collection.Coordinates;
import org.example.packet.collection.Location;
import org.example.packet.collection.Route;
import org.example.packet.collection.RouteClient;
import org.example.packet.enums.Codes;
import org.example.server.Server;
import org.example.server.interfaces.Command;
import org.example.server.logger.ServerLogger;
import org.example.server.managers.ManagerTask;
import org.example.server.managers.ManagerTask.TaskInfo;

import java.math.BigDecimal;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static org.example.server.Server.managerCollections;
import static org.example.server.Server.managerDataBase;

public class GenerateData implements Command {

    private static final String[] NAMES = {
            "Волга", "Анжела", "Матильда", "Глория", "Гаврилова", "Киев", "Собака", "Екатерина Чистякова"
    };

    @Override
    public Codes executeCommand(String[] args, RouteClient value, SocketChannel clientChannel, String login, String password) {
        if (args.length < 1) {
            try {
                Server.writeExecutor(
                        Codes.WARNING,
                        "Использование: generate_data {count}",
                        null,
                        clientChannel
                );
            } catch (Exception e) {
                ServerLogger.error("Ошибка отправки ответа generate_data: {}", e.getMessage());
            }
            return Codes.WARNING;
        }

        int count;
        try {
            count = Integer.parseInt(args[0]);
            if (count <= 0) throw new NumberFormatException("Число должно быть положительным");
        } catch (NumberFormatException e) {
            try {
                Server.writeExecutor(
                        Codes.WARNING,
                        "Аргумент count должен быть положительным целым числом",
                        null,
                        clientChannel
                );
            } catch (Exception ex) {
                ServerLogger.error("Ошибка отправки ответа generate_data: {}", ex.getMessage());
            }
            return Codes.WARNING;
        }

        String taskId = ManagerTask.createTask("generate_data");
        TaskInfo task = ManagerTask.getTask(taskId);

        try {
            Map<String, String> responseData = new HashMap<>();
            responseData.put("taskId", taskId);
            responseData.put("count", String.valueOf(count));

            Server.writeExecutor(
                    Codes.OK,
                    "Задача принята. Используйте 'task_status " + taskId + "' для проверки статуса.",
                    responseData,
                    clientChannel
            );
        } catch (Exception e) {
            ServerLogger.error("Ошибка отправки подтверждения generate_data: {}", e.getMessage());
            return Codes.ERROR;
        }

        final int finalCount = count;
        Thread worker = new Thread(() -> {
            task.setStatus(ManagerTask.TaskStatus.IN_PROGRESS);
            task.setMessage("Генерация данных...");
            ServerLogger.info("Задача {}: начата генерация {} элементов", taskId, finalCount);

            try {
                Random rnd = new Random();
                int added = 0;
                for (int i = 0; i < finalCount; i++) {
                    String name = NAMES[rnd.nextInt(NAMES.length)] + "-" + (i + 1);

                    // coordinates_x <= 108, coordinates_y <= 20 (ограничения БД)
                    Coordinates coords = new Coordinates(
                            (long) (rnd.nextDouble() * 216) - 108,  // [-108, 108]
                            (long) (rnd.nextDouble() * 40)  - 20    // [-20,  20]
                    );
                    Location from = new Location(
                            rnd.nextFloat() * 200 - 100,
                            rnd.nextDouble() * 200 - 100,
                            rnd.nextInt(1000)
                    );
                    Location to = new Location(
                            rnd.nextFloat() * 200 - 100,
                            rnd.nextDouble() * 200 - 100,
                            rnd.nextInt(1000)
                    );
                    int distance = rnd.nextInt(9900) + 100;
                    BigDecimal price = BigDecimal.valueOf(rnd.nextInt(100000) + 1, 2);

                    RouteClient route = new RouteClient(name, coords, from, to, distance, price);

                    Route saved = managerDataBase.addRouteInDBFull(route, login);
                    if (saved != null) {
                        managerCollections.addCollections(saved);
                        added++;
                    }

                    if (finalCount > 100) {
                        Thread.sleep(2);
                    }
                }

                task.finish("Успешно сгенерировано " + added + " из " + finalCount + " элементов. Всего в коллекции: " + managerCollections.getSizeCollections());
                ServerLogger.info("Задача {}: завершена, добавлено {}/{} элементов", taskId, added, finalCount);

            } catch (InterruptedException e) {
                task.error("Задача прервана: " + e.getMessage());
                ServerLogger.error("Задача {}: прервана", taskId);
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                task.error("Ошибка при генерации: " + e.getMessage());
                ServerLogger.error("Задача {}: ошибка - {}", taskId, e.getMessage());
            }
        });

        worker.setDaemon(true);
        worker.setName("generate-data-" + taskId);
        worker.start();

        return Codes.OK;
    }

    @Override
    public String toString() {
        return "generate_data {count} - асинхронно генерирует count элементов в коллекции";
    }
}