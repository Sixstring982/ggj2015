package com.lunagameserve.ggj2015.client.game;

import com.lunagameserve.ggj2015.client.net.TCPStream;

/**
 * Created by six on 1/24/15.
 */
public class BombCommunicator {
    private boolean isDefusing = false;

    private TCPStream stream;

    public void joinGame(String gameID) {
        stream.writeLine("join " + gameID);
    }
}
