package com.lunagameserve.ggj2015.bombServer;

import com.lunagameserve.ggj2015.textServer.Stream;

import java.util.HashMap;
import java.util.Random;

/**
 * Created by six on 1/23/15.
 */
public class BombServer {
    private static Random rand;
    static {
        rand = new Random(System.nanoTime());
    }

    public static Random getRandom() {
        return rand;
    }

    private Stream textServerStream;

    private HashMap<String, Game> liveGames = new HashMap<>();

    private boolean running = true;

    public BombServer() {
        run();
    }

    private void stop() {
        running = false;
    }

    private void run() {
        running = true;
        String incomingLine;
        while (running) {
            incomingLine = textServerStream.read(100);
        }
    }
}
