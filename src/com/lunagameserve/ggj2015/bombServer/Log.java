package com.lunagameserve.ggj2015.bombServer;

/**
 * Created by Six on 1/24/2015.
 */
public class Log {

    private static LogLevel level = LogLevel.Error;

    public static void setLevel(LogLevel level) {
        Log.level = level;
    }

    public static void message(String message) {
        if (level.canPrint(LogLevel.Message)) {
            print(LogLevel.Debug, message);
        }
    }

    public static void debug(String message) {
        if (level.canPrint(LogLevel.Debug)) {
            print(LogLevel.Debug, message);
        }
    }

    public static void warning(String message) {
        if (level.canPrint(LogLevel.Warning)) {
            print(LogLevel.Warning, message);
        }
    }

    public static void error(String message) {
        if (level.canPrint(LogLevel.Error)) {
            print(LogLevel.Error, message);
        }
    }

    private static void print(LogLevel level, String message) {
        System.err.println("[Log::" + level + "]: " + message);
    }
}
