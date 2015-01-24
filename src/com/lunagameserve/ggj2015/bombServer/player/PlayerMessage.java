package com.lunagameserve.ggj2015.bombServer.player;

import com.lunagameserve.ggj2015.util.StringUtil;

/**
 * Created by Six on 1/24/2015.
 */
public class PlayerMessage {
    private final String playerID;

    public String getPlayerID() {
        return playerID;
    }

    private final String message;

    public String getMessage() {
        return message;
    }

    public PlayerMessage(String rawMessage) throws IllegalArgumentException {
        assertValidMessage(rawMessage);
        playerID = readPlayerID(rawMessage);
        message = readMessage(rawMessage);
    }

    public String generateResponse(String response) {
        return playerID + " " + response;
    }

    private void assertValidMessage(String rawMessage) throws IllegalArgumentException {
        if (!rawMessage.contains(" ")) {
            throw new IllegalArgumentException("PlayerMessage <" + rawMessage + "> does not contain an identifier.");
        }
    }

    private String readPlayerID(String rawMessage) {
        return StringUtil.readUntil(rawMessage, ' ');
    }

    private String readMessage(String rawMessage) {
        return StringUtil.readAfter(rawMessage, ' ');
    }

    @Override
    public String toString() {
        return "[" + playerID + "]: " + message;
    }
}
