package com.lunagameserve.ggj2015.bombServer;

import java.net.Socket;

/**
 * Created by six on 1/23/15.
 */
public class Player {

    private final Socket socket;

    public Player(Socket socket) {
        this.socket = socket;
    }
}
