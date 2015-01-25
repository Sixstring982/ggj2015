package com.lunagameserve.ggj2015.bombServer.player;

import com.lunagameserve.ggj2015.textServer.Stream;

import java.io.PrintStream;

/**
 * Created by six on 1/23/15.
 */
public class Player {
    private final String identifier;

    private PlayerAlignment alignment = PlayerAlignment.Evil;

    private String displayName = null;

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        if (displayName == null) {
            return identifier;
        } else {
            return displayName;
        }
    }

    public PlayerAlignment getAlignment() {
        return alignment;
    }

    private Stream serverStream;

    private boolean voteKill = false;

    public void setVoteKill(boolean value) {
        this.voteKill = value;
    }

    public boolean getVoteKill() {
        return voteKill;
    }

    public void setAlignment(PlayerAlignment alignment) {
        this.alignment = alignment;
    }

    public String getIdentifier() {
        return identifier;
    }

    public Player(String identifier, Stream serverStream) {
        this.identifier = identifier;
        this.serverStream = serverStream;
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

    public void sendResponse(String message) {
        serverStream.write(generateResponse(message));
    }
}
