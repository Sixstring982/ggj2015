package com.lunagameserve.ggj2015.bombServer;

/**
 * @author sixstring982
 * @since 1/23/15
 */
public class ServerDriver {
    public static void main(String[] args) {
        Log.setLevel(LogLevel.Debug);
        new BombServer();
    }
}
