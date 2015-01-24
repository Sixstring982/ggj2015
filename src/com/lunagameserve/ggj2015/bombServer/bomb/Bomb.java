package com.lunagameserve.ggj2015.bombServer.bomb;

import com.lunagameserve.ggj2015.bombServer.BombServer;
import com.lunagameserve.ggj2015.bombServer.player.Player;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 * Created by six on 1/23/15.
 */
public class Bomb {

    private BombState state = BombState.Active;

    public BombState getState() {
        return state;
    }

    public synchronized void setState(BombState state) {
        this.state = state;
    }

    private Wire[] wires;

    private HashMap<String, Wire> wireLookup = new HashMap<>();

    private final int badWires = 2;
    private final int goodWires;

    public int getBadWires() {
        return badWires;
    }

    public int getGoodWires() {
        return goodWires;
    }

    public Bomb(int wireCount) {
        wireCount = Math.max(wireCount, 4);
        this.goodWires = (int)(Math.ceil(wireCount / 2));
        initWires(wireCount, goodWires);
    }

    private void initWires(int wireCount, int goodWires) {
        wires = new Wire[wireCount];
        List<Wire> unassignedWires = new ArrayList<>();
        for (int i = 0; i < wireCount; i++) {
            wires[i] = new Wire(i);
            wires[i].setState(WireState.Neutral);
            wireLookup.put(wires[i].getIdentifier(), wires[i]);
            unassignedWires.add(wires[i]);
        }

        /* Assign bad wires */
        for (int i = 0; i < 2; i++) {
            int badIdx = BombServer.getRandom().nextInt(unassignedWires.size());
            Wire badWire = unassignedWires.get(badIdx);
            unassignedWires.remove(badWire);
            badWire.setState(WireState.Bad);
        }

        /* Assign good wires */
        for (int i = 0; i < goodWires; i++) {
            int goodIdx = BombServer.getRandom().nextInt(unassignedWires.size());
            Wire goodWire = unassignedWires.get(goodIdx);
            unassignedWires.remove(goodWire);
            goodWire.setState(WireState.Good);
        }
    }

    public void cutWire(String identifier) throws IllegalArgumentException {
        Wire wireToCut = wireLookup.get(identifier);
        if (wireToCut == null) {
            throw new IllegalArgumentException("Wire <" + identifier + "> does not exist.");
        } else {
            wireToCut.cut();
            recalculateBombState();
        }
    }

    private void recalculateBombState() {
        int goodCount = 0;
        int badCount = 0;
        for (Wire w : wires) {
            if (w.getState().equals(WireState.Bad)) {
                badCount++;
            } else if (w.getState().equals(WireState.Good)) {
                goodCount++;
            }
        }

        if (goodCount == 0) {
            this.state = BombState.Defused;
        } else if (badCount == 0) {
            this.state = BombState.Exploded;
        }
    }

    public void sendInformation(Collection<Player> players) {
        int informantIdx = BombServer.getRandom().nextInt(players.size());
        for (Player p : players) {
            if (informantIdx == 0) {
                p.sendResponse(generateInformation());
            }
            informantIdx--;
        }
    }

    private String generateInformation() {
        int wireIdx = BombServer.getRandom().nextInt(wires.length);
        return "The " + wires[wireIdx].getIdentifier() + " wire is a " + wires[wireIdx].getState() + " wire.";
    }

    public void printStatus(PrintStream out) {
        out.println("Status: " + state);
        for (Wire w : wires) {
            out.println("Wire " + w.getIdentifier() + ": " + w.getStateString());
        }
    }
}
