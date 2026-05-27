package org.example.server.enums;

public enum Colors {
    RED("\u001B[31m"),
    GREEN("\u001B[32m"),
    BLUE("\u001B[34m"),
    YELLOW("\u001B[33m"),
    BLACK("\u001B[30m"),
    WHITE("\u001B[37m"),
    MAGENTA("\u001B[35m"),
    CYAN("\u001B[36m"),
    PUSH_BOLD("\u001B[1;35m"),
    PUSH_BG("\u001B[45;97m");

    private final String code;

    Colors(String code) {
        this.code = code;
    }

    @Override
    public String toString() {
        return code;
    }

    public static String reset() {
        return "\u001B[0m";
    }
}