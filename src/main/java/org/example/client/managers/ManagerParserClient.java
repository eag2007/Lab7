package org.example.client.managers;

import org.example.client.commands.*;
import org.example.client.interfaces.Command;
import org.example.client.enums.Colors;

import java.util.*;

import static org.example.client.Client.managerInputOutput;
import static org.example.client.Client.server;

public class ManagerParserClient {
    private final HashMap<String, Command> commands;
    private final List<String> historyCommands;
    private static final int MAX_SIZE_LEN_HISTORY = 14;

    public ManagerParserClient() {
        this.commands = new HashMap<String, Command>();
        this.historyCommands = new ArrayList<>(MAX_SIZE_LEN_HISTORY);

        this.commands.put("add", new Add());
        this.commands.put("add_if_max", new AddIfMax());
        this.commands.put("average_of_distance", new AverageOfDistance());
        this.commands.put("clear", new Clear());
        this.commands.put("execute_script", new ExecuteScript());
        this.commands.put("exit", new Exit());
        this.commands.put("filter_less_than_distance", new FilterLessThanDistance());
        this.commands.put("help", new Help());
        this.commands.put("history", new History());
        this.commands.put("info", new Info());
        this.commands.put("remove_all_by_distance", new RemoveAllByDistance());
        this.commands.put("remove_by_id", new RemoveById());
        this.commands.put("remove_first", new RemoveFirst());
        this.commands.put("show", new Show());
        this.commands.put("update", new Update());
        this.commands.put("ls", new Show());
        this.commands.put("status", new Status());
        this.commands.put("logout", new Logout());
    }

    public List<String> getCommandNames() {
        return new ArrayList<>(this.commands.keySet());
    }

    public List<Command> getCommands() {
        return new ArrayList<>(this.commands.values());
    }

    public List<String> getHistoryCommands() {
        return historyCommands;
    }

    public void parserCommand(String s) {
        String[] command = s.trim().replaceAll("\\s+", " ").split(" ");

        String cmdName = command[0];
        String[] args = Arrays.copyOfRange(command, 1, command.length);

        if (this.commands.containsKey(cmdName)) {
            Command cmd = this.commands.get(cmdName);

            if (this.historyCommands.size() >= MAX_SIZE_LEN_HISTORY) {
                this.historyCommands.remove(0);
            }
            this.historyCommands.add(s);

            cmd.executeCommand(args, server);
        } else {
            managerInputOutput.writeLineIO("Неизвестная команда\n", Colors.RED);
        }
    }
}