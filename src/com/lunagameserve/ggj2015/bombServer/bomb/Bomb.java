package com.lunagameserve.ggj2015.bombServer.bomb;

import java.io.PrintStream;

/**
 * Created by six on 1/23/15.
 */
public class Bomb {

    private BombState state = BombState.Active;

    public BombState getState() {
        return state;
    }

    private int[] wires;

    private int badWires;
    private int goodWires;

    public Bomb(int wireCount, int playerCount) {
        this.wires = new int[wireCount];
    }

    public void printStatus(PrintStream out) {

    }
}
