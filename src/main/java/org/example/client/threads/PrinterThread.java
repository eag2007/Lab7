package org.example.client.threads;

import org.example.client.enums.Colors;
import org.example.packet.ResponsePacket;
import org.example.packet.collection.Route;
import org.example.packet.enums.Codes;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import static org.example.client.Client.*;

public class PrinterThread extends Thread {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault());
    private volatile boolean running = true;

    public PrinterThread() {
        super("printer-thread");
        setDaemon(true);
    }

    @Override
    public void run() {
        while (running && !Thread.currentThread().isInterrupted()) {
            try {
                ResponsePacket packet = managerResponseQueue.take();
                print(packet);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void print(ResponsePacket p) {
        if (p == null) return;

        if (p.getStatusCode() == Codes.PUSH) {
            if (p.getMessage().contains(getLogin()))
                return;

            managerInputOutput.writeLineIO("PUSH [ " + p.getMessage() + " ] ", Colors.PUSH_BOLD);
            return;
        }

        Object data = p.getData();

        if (data instanceof List) {
            @SuppressWarnings("unchecked")
            List<Route> routes = (List<Route>) data;
            printRouteList(routes, p);
            return;
        }

        if (data instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) data;
            if (map.containsKey("routes")) {
                printSee(map, p);
                return;
            }
            if (map.containsKey("status")) {
                printTaskStatus(map, p);
                return;
            }
            if (map.containsKey("taskId")) {
                printGenerateData(map, p);
                return;
            }
            if (map.containsKey("size")) {
                printInfo(map, p);
                return;
            }
        }

        printSimple(p);
    }


    private void printSimple(ResponsePacket p) {
        Colors c = switch (p.getStatusCode()) {
            case OK -> Colors.GREEN;
            case WARNING -> Colors.YELLOW;
            case ERROR -> Colors.RED;
            default -> null;
        };
        String text = "Сервер: " + p.getMessage();
        if (c != null) managerInputOutput.writeLineIO(text, c);
        else managerInputOutput.writeLineIO(text);
    }

    private void printRouteList(List<Route> routes, ResponsePacket p) {
        if (p.getStatusCode() != Codes.OK) {
            printSimple(p);
            return;
        }
        if (routes == null || routes.isEmpty()) {
            managerInputOutput.writeLineIO("Коллекция пуста", Colors.YELLOW);
            return;
        }
        String header = String.format(
                "%-4s | %-15s | %-3s | %-3s | %-20s | %-6s | %-6s | %-4s | %-6s | %-6s | %-4s | %-8s | %-10s | %-9s",
                "ID", "Name", "X", "Y", "Date", "FromX", "FromY", "FromZ", "ToX", "ToY", "ToZ", "Distance", "Price", "Author");

        StringBuilder sb = new StringBuilder();
        sb.append(header).append("\n");
        sb.append("-".repeat(header.length())).append("\n");
        for (Route r : routes) {
            sb.append(String.format(
                    "%-4s | %-15s | %-3s | %-3s | %-20s | %-6s | %-6s | %-4s | %-6s | %-6s | %-4s | %-8s | %-10s | %-9s\n",
                    r.getId(), trunc(r.getName(), 15),
                    r.getCoordinates().getX(), r.getCoordinates().getY(),
                    r.getCreationDate().toString().substring(0, 19),
                    r.getFrom().getX(), r.getFrom().getY(), r.getFrom().getZ(),
                    r.getTo().getX(), r.getTo().getY(), r.getTo().getZ(),
                    r.getDistance(), r.getPrice(), trunc(r.getAuthor(), 9)));
        }
        if (!sb.isEmpty() && sb.charAt(sb.length() - 1) == '\n')
            sb.deleteCharAt(sb.length() - 1);
        managerInputOutput.writeLineIO(sb.toString());
    }

    private void printSee(Map<String, Object> data, ResponsePacket p) {
        if (p.getStatusCode() != Codes.OK) {
            printSimple(p);
            return;
        }
        if (data == null) {
            managerInputOutput.writeLineIO(p.getMessage(), Colors.YELLOW);
            return;
        }

        @SuppressWarnings("unchecked")
        List<Route> routes = (List<Route>) data.get("routes");
        int page = (int) data.get("page");
        int totalPages = (int) data.get("totalPages");
        int total = (int) data.get("total");
        int pageSize = (int) data.get("pageSize");

        String header = String.format("%-5s | %-20s | %-10s", "ID", "Name", "Distance");

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Страница %d из %d | Всего: %d | На странице: %d",
                page, totalPages, total, pageSize)).append("\n");
        sb.append("-".repeat(80)).append("\n");
        sb.append(header).append("\n");
        sb.append("-".repeat(header.length())).append("\n");
        for (Route r : routes) {
            sb.append(String.format("%-5d | %-20s | %-10d\n",
                    r.getId(), r.getName(), r.getDistance()));
        }
        sb.append("-".repeat(header.length()));
        if (totalPages > 1) {
            sb.append("\nНавигация:");
            if (page > 1) sb.append("\n  see ").append(page - 1).append(" - предыдущая");
            if (page < totalPages) sb.append("\n  see ").append(page + 1).append(" - следующая");
            sb.append("\n  see ").append(page).append(" ").append(pageSize).append(" - обновить");
        }
        managerInputOutput.writeLineIO(sb.toString());
    }

    private void printTaskStatus(Map<String, Object> data, ResponsePacket p) {
        if (p.getStatusCode() == Codes.WARNING) {
            managerInputOutput.writeLineIO("Задача не найдена", Colors.RED);
            return;
        }
        if (p.getStatusCode() != Codes.OK) {
            printSimple(p);
            return;
        }
        if (data == null) {
            managerInputOutput.writeLineIO(p.getMessage(), Colors.YELLOW);
            return;
        }

        @SuppressWarnings("unchecked")
        Map<String, String> d = (Map<String, String>) (Object) data;
        String status = d.getOrDefault("status", "UNKNOWN");
        String message = d.getOrDefault("message", "");
        String command = d.getOrDefault("command", "");
        String taskId = d.getOrDefault("taskId", "?");
        String createdAt = d.getOrDefault("created", "-1");
        String finishedAt = d.getOrDefault("finished", "-1");

        Colors sc = switch (status) {
            case "DONE" -> Colors.GREEN;
            case "ERROR" -> Colors.RED;
            case "IN_PROGRESS" -> Colors.BLUE;
            default -> Colors.YELLOW;
        };

        StringBuilder sb = new StringBuilder();
        sb.append("┌─ Статус задачи ──────────────────────────\n");
        sb.append("│ Task ID : ").append(taskId).append("\n");
        sb.append("│ Команда : ").append(command).append("\n");
        sb.append("│ Статус  : ").append(status).append("\n");
        sb.append("│ Сообщение: ").append(message).append("\n");
        try {
            long created = Long.parseLong(createdAt);
            if (created > 0) sb.append("│ Создана : ").append(FMT.format(Instant.ofEpochMilli(created))).append("\n");
            long finished = Long.parseLong(finishedAt);
            if (finished > 0) {
                sb.append("│ Завершена: ").append(FMT.format(Instant.ofEpochMilli(finished))).append("\n");
                sb.append("│ Время   : ").append(finished - created).append(" мс\n");
            }
        } catch (NumberFormatException ignored) {
        }
        sb.append("└──────────────────────────────────────────");
        managerInputOutput.writeLineIO(sb.toString(), sc);
    }

    private void printGenerateData(Map<String, Object> data, ResponsePacket p) {
        if (p.getStatusCode() != Codes.OK) {
            printSimple(p);
            return;
        }
        @SuppressWarnings("unchecked")
        Map<String, String> d = (Map<String, String>) (Object) data;
        String taskId = d != null ? d.get("taskId") : "?";
        String count = d != null ? d.get("count") : "?";

        String sb = "  Задача запущена!\n" +
                "  Команда  : generate_data\n" +
                "  Элементов: " + count + "\n" +
                "  Task ID  : " + taskId + "\n" +
                "  Проверить статус: task_status " + taskId;
        managerInputOutput.writeLineIO(sb, Colors.GREEN);
    }

    private void printInfo(Map<String, Object> data, ResponsePacket p) {
        if (p.getStatusCode() != Codes.OK) {
            printSimple(p);
            return;
        }
        String sb = "Количество элементов: " + data.get("size") + "\n" +
                "Время инициализации: " + data.get("initTime") + "\n" +
                "Тип данных: Route";
        managerInputOutput.writeLineIO(sb);
    }

    private String trunc(String s, int n) {
        if (s == null) return "";
        return s.length() > n ? s.substring(0, n - 3) + "..." : s;
    }

    public void stopPrinter() {
        running = false;
        interrupt();
    }
}