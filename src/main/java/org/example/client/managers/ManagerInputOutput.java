package org.example.client.managers;

import org.example.client.enums.Colors;
import org.jline.reader.*;
import org.jline.reader.impl.completer.StringsCompleter;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Stack;

public class ManagerInputOutput {
    private static ManagerInputOutput managerInputOutput;
    private static LineReader lineReader;
    private static Terminal terminal;
    private Stack<BufferedReader> readerStack;
    private boolean executeScript = false;

    private ManagerInputOutput() {
        this.readerStack = new Stack<>();
        try {
            terminal = TerminalBuilder.builder()
                    .system(true)
                    .build();
            lineReader = LineReaderBuilder.builder()
                    .terminal(terminal)
                    .build();
        } catch (IOException e) {
            throw new RuntimeException("Не удалось инициализировать терминал: " + e.getMessage());
        }
    }

    public static ManagerInputOutput getInstance() {
        if (managerInputOutput == null) {
            managerInputOutput = new ManagerInputOutput();
        }
        return managerInputOutput;
    }

    public void setCommands(List<String> commandNames) {
        try {
            Completer completer = new StringsCompleter(commandNames);
            lineReader = LineReaderBuilder.builder()
                    .terminal(terminal)
                    .completer(completer)
                    .build();
        } catch (Exception e) {
            System.out.println("Ошибка при установке автодополнения: " + e.getMessage());
        }
    }

    public void pushFileExecute(BufferedReader reader) {
        this.readerStack.push(reader);
        this.executeScript = true;
    }

    public void popFileExecute() {
        if (!readerStack.isEmpty()) {
            try {
                BufferedReader currentReader = readerStack.peek();
                if (currentReader != null) {
                    currentReader.close();
                }
            } catch (IOException e) {
                System.out.println("Ошибка при закрытии ридера: " + e.getMessage());
            } finally {
                readerStack.pop();
                if (readerStack.isEmpty()) {
                    this.executeScript = false;
                }
            }
        }
    }

    public boolean isScriptMode() {
        return this.executeScript || !readerStack.isEmpty();
    }

    public boolean isCurrentReader(BufferedReader reader) {
        return !readerStack.isEmpty() && readerStack.peek() == reader;
    }

    public String readLineIO(String prompt) {
        while (!readerStack.isEmpty()) {
            BufferedReader currentReader = readerStack.peek();
            try {
                String line = currentReader.readLine();
                if (line != null) {
                    System.out.println("[Значение из скрипта] " + line);
                    return line;
                } else {
                    popFileExecute();
                    return null;
                }
            } catch (IOException e) {
                popFileExecute();
                return null;
            }
        }
        try {
            return lineReader.readLine(prompt);
        } catch (UserInterruptException e) {
            return "";
        } catch (EndOfFileException e) {
            throw new NoSuchElementException("EOF");
        }
    }

    public String readLineIO() {
        return readLineIO("");
    }

    public void writeLineIO(String message) {
        System.out.print(message);
    }

    public void writeLineIO(String message, Colors color) {
        System.out.print(color + message + "\u001B[0m");
    }

    public void closeIO() {
        while (!readerStack.isEmpty()) {
            try {
                BufferedReader reader = readerStack.pop();
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                System.out.println("Ошибка при закрытии ридера: " + e.getMessage());
            }
        }
        try {
            if (terminal != null) {
                terminal.close();
            }
        } catch (IOException e) {
            System.out.println("Ошибка при закрытии терминала: " + e.getMessage());
        }
        writeLineIO("IO закрыт\n");
    }
}