package com.lunagameserve.ggj2015.client;

import com.lunagameserve.ggj2015.bombServer.Log;
import com.lunagameserve.ggj2015.bombServer.LogLevel;
import com.lunagameserve.ggj2015.client.gui.MainFrame;

/**
 * Created by six on 1/24/15.
 */
public class ClientDriver {
    public static void main(String[] args) {
        Log.setLevel(LogLevel.Debug);
        new MainFrame(new Arguments(args));
    }
}
