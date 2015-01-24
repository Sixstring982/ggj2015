package com.lunagameserve.ggj2015.bombServer;

/**
 * Created by Six on 1/24/2015.
 */
public enum LogLevel {
    Disabled() {
        @Override
        public boolean canPrint(LogLevel currentLevel) {
            return false;
        }
    },
    Error () {
        @Override
        public boolean canPrint(LogLevel currentLevel) {
            return currentLevel.equals(Error);
        }
    },
    Warning() {
        @Override
        public boolean canPrint(LogLevel currentLevel) {
            return Error.canPrint(currentLevel) ||
                    currentLevel.equals(Warning);
        }
    },
    Debug() {
        @Override
        public boolean canPrint(LogLevel currentLevel) {
            return Warning.canPrint(currentLevel) ||
                    currentLevel.equals(Debug);
        }
    },
    Message() {
        @Override
        public boolean canPrint(LogLevel currentLevel) {
            return Debug.canPrint(currentLevel) ||
                    currentLevel.equals(Message);
        }
    };

    public abstract boolean canPrint(LogLevel currentLevel);
}
