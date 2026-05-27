package org.example.client.threads;

import org.example.client.enums.Colors;
import org.example.client.managers.ManagerInputOutput;
import org.example.client.managers.ResponseQueue;
import org.example.packet.ResponsePacket;
import org.example.packet.collection.Route;
import org.example.packet.enums.Codes;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class PrinterThread extends Thread {

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault());

    private final ResponseQueue queue = ResponseQueue.getInstance();
    private final ManagerInputOutput io = ManagerInputOutput.getInstance();
    private volatile boolean running = true;

    public PrinterThread() {
        super("printer-thread");
        setDaemon(true);
    }

    @Override
    public void run() {
        while (running && !Thread.currentThread().isInterrupted()) {
            try {
                ResponsePacket packet = queue.take();
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
            io.writeLineIO("PUSH{" + p.getMessage() + "}\n", Colors.PUSH_BOLD);
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
        switch (p.getStatusCode()) {
            case OK      -> io.writeLineIO("Сервер: " + p.getMessage() + "\n", Colors.GREEN);
            case WARNING -> io.writeLineIO("Сервер: " + p.getMessage() + "\n", Colors.YELLOW);
            case ERROR   -> io.writeLineIO("Сервер: " + p.getMessage() + "\n", Colors.RED);
            default      -> io.writeLineIO("Сервер: " + p.getMessage() + "\n");
        }
    }

    private void printRouteList(List<Route> routes, ResponsePacket p) {
        if (p.getStatusCode() != Codes.OK) { printSimple(p); return; }
        if (routes == null || routes.isEmpty()) {
            io.writeLineIO("Коллекция пуста\n", Colors.YELLOW);
            return;
        }
        String header = String.format(
                "%-3s | %-15s | %-3s | %-3s | %-20s | %-6s | %-6s | %-4s | %-6s | %-6s | %-4s | %-8s | %-10s | %-10s",
                "ID","Name","X","Y","Date","FromX","FromY","FromZ","ToX","ToY","ToZ","Distance","Price","Author");
        io.writeLineIO(header + "\n");
        io.writeLineIO("-".repeat(header.length()) + "\n");
        for (Route r : routes) {
            io.writeLineIO(String.format(
                    "%-3s | %-15s | %-3s | %-3s | %-20s | %-6s | %-6s | %-4s | %-6s | %-6s | %-4s | %-8s | %-10s | %-10s\n",
                    r.getId(), trunc(r.getName(),15),
                    r.getCoordinates().getX(), r.getCoordinates().getY(),
                    r.getCreationDate().toString().substring(0,19),
                    r.getFrom().getX(), r.getFrom().getY(), r.getFrom().getZ(),
                    r.getTo().getX(),   r.getTo().getY(),   r.getTo().getZ(),
                    r.getDistance(), r.getPrice(), trunc(r.getAuthor(),10)));
        }
    }

    private void printSee(Map<String, Object> data, ResponsePacket p) {
        if (p.getStatusCode() != Codes.OK) { printSimple(p); return; }
        if (data == null) { io.writeLineIO(p.getMessage() + "\n", Colors.YELLOW); return; }

        @SuppressWarnings("unchecked")
        List<Route> routes   = (List<Route>) data.get("routes");
        int page       = (int) data.get("page");
        int totalPages = (int) data.get("totalPages");
        int total      = (int) data.get("total");
        int pageSize   = (int) data.get("pageSize");

        io.writeLineIO(String.format("Страница %d из %d | Всего: %d | На странице: %d\n",
                page, totalPages, total, pageSize), Colors.YELLOW);
        io.writeLineIO("-".repeat(80) + "\n", Colors.BLUE);

        String header = String.format("%-5s | %-20s | %-10s", "ID", "Name", "Distance");
        io.writeLineIO(header + "\n", Colors.GREEN);
        io.writeLineIO("-".repeat(header.length()) + "\n");

        for (Route r : routes) {
            io.writeLineIO(String.format("%-5d | %-20s | %-10d\n",
                    r.getId(), r.getName(), r.getDistance()));
        }
        io.writeLineIO("-".repeat(header.length()) + "\n");

        if (totalPages > 1) {
            io.writeLineIO("\nНавигация:\n", Colors.YELLOW);
            if (page > 1)         io.writeLineIO("  see " + (page-1) + " - предыдущая\n", Colors.YELLOW);
            if (page < totalPages) io.writeLineIO("  see " + (page+1) + " - следующая\n",  Colors.YELLOW);
            io.writeLineIO("  see " + page + " " + pageSize + " - обновить\n", Colors.YELLOW);
        }
    }

    private void printTaskStatus(Map<String, Object> data, ResponsePacket p) {
        if (p.getStatusCode() == Codes.WARNING) {
            io.writeLineIO("Задача не найдена\n", Colors.RED); return;
        }
        if (p.getStatusCode() != Codes.OK) { printSimple(p); return; }
        if (data == null) { io.writeLineIO(p.getMessage() + "\n", Colors.YELLOW); return; }

        @SuppressWarnings("unchecked")
        Map<String, String> d = (Map<String, String>) (Object) data;
        String status     = d.getOrDefault("status",   "UNKNOWN");
        String message    = d.getOrDefault("message",  "");
        String command    = d.getOrDefault("command",  "");
        String taskId     = d.getOrDefault("taskId",   "?");
        String createdAt  = d.getOrDefault("created",  "-1");
        String finishedAt = d.getOrDefault("finished", "-1");

        Colors sc = switch (status) {
            case "DONE"        -> Colors.GREEN;
            case "ERROR"       -> Colors.RED;
            case "IN_PROGRESS" -> Colors.BLUE;
            default            -> Colors.YELLOW;
        };

        io.writeLineIO("┌─ Статус задачи ──────────────────────────\n");
        io.writeLineIO("│ Task ID : " + taskId + "\n");
        io.writeLineIO("│ Команда : " + command + "\n");
        io.writeLineIO("│ Статус  : " + status + "\n", sc);
        io.writeLineIO("│ Сообщение: " + message + "\n");
        try {
            long created = Long.parseLong(createdAt);
            if (created > 0) io.writeLineIO("│ Создана : " + FMT.format(Instant.ofEpochMilli(created)) + "\n");
            long finished = Long.parseLong(finishedAt);
            if (finished > 0) {
                io.writeLineIO("│ Завершена: " + FMT.format(Instant.ofEpochMilli(finished)) + "\n");
                io.writeLineIO("│ Время   : " + (finished - created) + " мс\n");
            }
        } catch (NumberFormatException ignored) {}
        io.writeLineIO("└──────────────────────────────────────────\n");
    }

    private void printGenerateData(Map<String, Object> data, ResponsePacket p) {
        if (p.getStatusCode() != Codes.OK) { printSimple(p); return; }
        @SuppressWarnings("unchecked")
        Map<String, String> d = (Map<String, String>) (Object) data;
        String taskId = d != null ? d.get("taskId") : "?";
        String count  = d != null ? d.get("count")  : "?";
        io.writeLineIO("  Задача запущена!\n", Colors.GREEN);
        io.writeLineIO("  Команда  : generate_data\n");
        io.writeLineIO("  Элементов: " + count + "\n");
        io.writeLineIO("  Task ID  : " + taskId + "\n", Colors.YELLOW);
        io.writeLineIO("  Проверить статус: task_status " + taskId + "\n", Colors.BLUE);
    }

    private void printInfo(Map<String, Object> data, ResponsePacket p) {
        if (p.getStatusCode() != Codes.OK) { printSimple(p); return; }
        io.writeLineIO("Количество элементов: " + data.get("size") + "\n");
        io.writeLineIO("Время инициализации: " + data.get("initTime") + "\n");
        io.writeLineIO("Тип данных: Route\n");
    }

    private String trunc(String s, int n) {
        if (s == null) return "";
        return s.length() > n ? s.substring(0, n-3) + "..." : s;
    }

    public void stopPrinter() {
        running = false;
        interrupt();
    }
}