package org.example.client.commands;

import org.example.client.enums.Colors;
import org.example.client.interfaces.Command;
import org.example.packet.CommandPacket;
import org.example.packet.ResponsePacket;
import org.example.packet.collection.Route;
import org.example.packet.enums.Codes;

import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.Map;

import static org.example.client.Client.*;

public class See implements Command {

    @Override
    public void executeCommand(String[] args, SocketChannel serverChannel) {
        if (args.length > 2) {
            managerInputOutput.writeLineIO("Использование: see [page] [pageSize]\n", Colors.RED);
            return;
        }

        try {
            CommandPacket packet = new CommandPacket("see", args, null, getLogin(), getPassword_hash());
            writeModule.writePacketForServer(serverChannel, packet);

            ResponsePacket response = readModule.readResponseForClient(serverChannel);

            if (response == null) {
                managerInputOutput.writeLineIO("Нет ответа от сервера\n", Colors.RED);
                return;
            }

            if (response.getStatusCode() != Codes.OK) {
                managerInputOutput.writeLineIO("Ошибка: " + response.getMessage() + "\n", Colors.RED);
                return;
            }

            if (response.getData() == null) {
                managerInputOutput.writeLineIO(response.getMessage() + "\n", Colors.YELLOW);
                return;
            }

            Map<String, Object> data = (Map<String, Object>) response.getData();

            List<Route> routes = (List<Route>) data.get("routes");

            int page = (int) data.get("page");
            int totalPages = (int) data.get("totalPages");
            int total = (int) data.get("total");
            int pageSize = (int) data.get("pageSize");

            managerInputOutput.writeLineIO(String.format("Страница %d из %d | Всего: %d | На странице: %d\n",
                    page, totalPages, total, pageSize), Colors.YELLOW);
            managerInputOutput.writeLineIO("-".repeat(80) + "\n", Colors.BLUE);

            String header = String.format("%-5s | %-20s | %-10s",
                    "ID", "Name", "Distance");
            managerInputOutput.writeLineIO(header + "\n", Colors.GREEN);
            managerInputOutput.writeLineIO("-".repeat(header.length()) + "\n");

            for (Route route : routes) {
                String line = String.format("%-5d | %-20s | %-10d",
                        route.getId(),
                        route.getName(),
                        route.getDistance());
                managerInputOutput.writeLineIO(line + "\n");
            }

            managerInputOutput.writeLineIO("-".repeat(header.length()) + "\n");

            if (totalPages > 1) {
                managerInputOutput.writeLineIO("\nНавигация:\n", Colors.YELLOW);
                if (page > 1) {
                    managerInputOutput.writeLineIO("  see " + (page - 1) + " - предыдущая\n", Colors.YELLOW);
                }
                if (page < totalPages) {
                    managerInputOutput.writeLineIO("  see " + (page + 1) + " - следующая\n", Colors.YELLOW);
                }
                managerInputOutput.writeLineIO("  see " + page + " " + pageSize + " - обновить\n", Colors.YELLOW);
            }

        } catch (Exception e) {
            managerInputOutput.writeLineIO("Ошибка: " + e.getMessage() + "\n", Colors.RED);
        }
    }

    @Override
    public String toString() {
        return "see [page] [pageSize] - показать коллекцию постранично";
    }
}