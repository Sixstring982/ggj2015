package com.lunagameserve.ggj2015.bombServer.player;

import java.io.PrintStream;

/**
 * Created by six on 1/23/15.
 */
public class Player {
    private final String identifier;

    private final PlayerAlignment alignment;

    private String displayName = null;

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public PlayerAlignment getAlignment() {
        return alignment;
    }

    public String getIdentifier() {
        return identifier;
    }

    public Player(String identifier, PlayerAlignment alignment) {
        this.identifier = identifier;
        this.alignment = alignment;
    }

    public void printFullStatus(PrintStream out) {
        if (displayName != null) {
            out.println("[" + identifier + " (aka " + displayName + ")]: " + alignment);
        } else {
            out.println("[" + identifier + "]: " + alignment);
        }
    }

    public void printStatus(PrintStream out) {
        String toShow = displayName;
        if (toShow == null) {
            toShow = identifier;
        }
        out.println(toShow);
    }

    public String generateResponse(String message) {
        return identifier + " " + message;
    }
}
