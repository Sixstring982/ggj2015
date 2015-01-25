package com.lunagameserve.ggj2015;

import com.lunagameserve.ggj2015.bombServer.BombServer;
import com.lunagameserve.ggj2015.bombServer.Log;
import com.lunagameserve.ggj2015.bombServer.LogLevel;

/**
 * @author sixstring982
 * @since 1/23/15
 */
public class MainDriver {
    public static void main(String[] args) {
        Log.setLevel(LogLevel.Debug);
        new BombServer();
    }
}
