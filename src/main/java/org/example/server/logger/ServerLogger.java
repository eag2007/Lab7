package org.example.server.logger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class    ServerLogger {
    private static final Logger logger = LogManager.getLogger("Server");

    public static void info(String message)  {
        logger.info(message);
    }

    public static void info(String message, Object... params) {
        logger.info(message, params);
    }

    public static void error(String message) {
        logger.error(message);
    }

    public static void error(String message, Object... params) {
        logger.error(message, params);
    }

    public static void debug(String message) {
        logger.debug(message);
    }

    public static void debug(String message, Object... params) {
        logger.debug(message, params);
    }
}
