package com.lunagameserve.ggj2015.bombServer;

import java.net.Socket;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by six on 1/23/15.
 */
public class Game {

    private final String identifier;

    private ConcurrentLinkedQueue<Socket> sockets = new ConcurrentLinkedQueue<>();

    public Game(List<String> existingIdentifiers) {
        this.identifier = GenerateIdentifier(existingIdentifiers);
    }

    public void acceptPlayer(Socket socket) {
        sockets.add(socket);
    }

    public void start() {

    }

    private String GenerateIdentifier(List<String> existingIdentifiers) {
        String newId;
        int idLoops = 0;
        do {
            newId = new Long(BombServer.getRandom().nextLong()).toString();
            idLoops++;

            if (idLoops > 1000) {
                System.err.println("Error creating game: 1000 random loops");
                System.exit(1);
            }
        } while (!existingIdentifiers.contains(newId));

        return newId;
    }
}
