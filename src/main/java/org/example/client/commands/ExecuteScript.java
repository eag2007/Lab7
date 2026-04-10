package org.example.client.commands;


import org.example.client.enums.Colors;
import org.example.client.interfaces.Command;

import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.HashSet;
import java.util.Set;

import static org.example.client.Client.managerInputOutput;
import static org.example.client.Client.managerParserClient;


public class ExecuteScript implements Command {
    private static final Set<String> setPaths = new HashSet<>();
    private int lineNumber = 0;

    public void executeCommand(String[] args, SocketChannel serverChannel) {
        if (!checkArg(args)) {
            managerInputOutput.writeLineIO("Ошибка, синтаксис команды: execute_script file_name\n", Colors.RED);
            return;
        }

        String fileName = args[0];
        File file = new File("src/main/java/org/example/" + fileName);
        if (!file.exists()) {
            file = new File(fileName);
        }

        if (!file.exists()) {
            managerInputOutput.writeLineIO("Ошибка: файл '" + fileName + "' не найден\n", Colors.RED);
            return;
        }

        String pathFile = file.getAbsolutePath();

        if (setPaths.contains(pathFile)) {
            managerInputOutput.writeLineIO("Ошибка: рекурсия в скрипте " + fileName + "\n", Colors.RED);
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            setPaths.add(pathFile);

            managerInputOutput.pushFileExecute(reader);

            lineNumber = 0;

            String line;
            while (true) {
                line = managerInputOutput.readLineIO();

                if (line == null) {
                    break;
                }

                if (!managerInputOutput.isCurrentReader(reader)) {
                    break;
                }

                lineNumber++;
                line = line.trim();
                if (line.isEmpty()) continue;

                managerParserClient.parserCommand(line);

                if (!managerInputOutput.isCurrentReader(reader)) {
                    break;
                }
            }

            if (managerInputOutput.isCurrentReader(reader)) {
                managerInputOutput.popFileExecute();
            }

            managerInputOutput.writeLineIO("Скрипт '" + fileName + "' выполнен\n", Colors.GREEN);

        } catch (IOException e) {
            managerInputOutput.writeLineIO("Ошибка: " + e.getMessage() + "\n", Colors.RED);
        } finally {
            setPaths.remove(pathFile);
        }
    }

    public boolean checkArg(String[] args) {
        return args.length == 1;
    }

    @Override
    public String toString() {
        return "execute_script file_name - выполняет скрипт из файла";
    }
}