package com.lunagameserve.ggj2015.bombServer;

import com.lunagameserve.ggj2015.bombServer.player.PlayerMessage;
import com.lunagameserve.ggj2015.textServer.Stream;
import com.lunagameserve.ggj2015.textServer.StreamFactory;

import java.util.Random;

/**
 * @author six
 * @since 1/23/15
 */
public class BombServer {
    private static Random rand;
    static {
        rand = new Random();
        setSeed(System.nanoTime());
    }

    public static Random getRandom() {
        return rand;
    }

    public static void setSeed(long seed) {
        Log.debug("New seed: " + seed);
        rand.setSeed(seed);
        Log.debug("First PRInt: " + rand.nextInt());
    }

    private Stream textServerStream = StreamFactory.createStream();

    private GameList gameList = new GameList();

    private boolean running = true;

    public BombServer() {
        run();
    }

    private void stop() {
        running = false;
        Log.debug("Server shutting down.");
        gameList.broadcastShutdown(textServerStream);
    }

    private void run() {
        running = true;
        String incomingLine;
        PlayerMessage incomingMessage;
        Log.debug("Server running.");
        textServerStream.start();
        while (running) {
            incomingLine = textServerStream.read(100);

            if (incomingLine != null) {
                try {
                    incomingMessage = new PlayerMessage(incomingLine, textServerStream);
                } catch(IllegalArgumentException ex) {
                    continue;
                }

                handleMessage(incomingMessage);
            }
        }
        textServerStream.stop();
    }

    private void handleServerMessage(PlayerMessage message) {
        if (message.getMessage().equals("server_shutdown")) {
            stop();
        }
    }

    private void handleMessage(PlayerMessage message) {
        Log.debug("Incoming message: " + message);
        if (message.getMessage().startsWith("server_") &&
                message.getPlayerID().equals("stdin")) {
            handleServerMessage(message);
        } else {
            gameList.handleMessage(message);
        }
    }
}
