package com.lunagameserve.ggj2015.bombServer;

import com.lunagameserve.ggj2015.bombServer.bomb.Bomb;
import com.lunagameserve.ggj2015.bombServer.bomb.BombState;
import com.lunagameserve.ggj2015.bombServer.player.Player;
import com.lunagameserve.ggj2015.bombServer.player.PlayerAlignment;
import com.lunagameserve.ggj2015.bombServer.player.PlayerMessage;
import com.lunagameserve.ggj2015.textServer.Stream;

import java.io.PrintStream;
import java.util.Collection;
import java.util.HashMap;

/**
 * Created by six on 1/23/15.
 */
public class Game {

    private static final int MAX_PLAYERS = 10;

    /**
     * Players in this game, indexed by their identifiers.
     */
    private HashMap<String, Player> players = new HashMap<>();

    private Bomb bomb;

    private final String identifier;

    public String getIdentifier() {
        return identifier;
    }

    public Game(Collection<String> existingIdentifiers) {
        this.identifier = GenerateIdentifier(existingIdentifiers);
    }

    public void join(String playerId) throws GameFullException, DuplicatePlayerException {
        if (players.containsKey(playerId)) {
            throw new DuplicatePlayerException("This player is already in this game.");
        } else if (players.size() >= MAX_PLAYERS) {
            throw new GameFullException("This game is full.");
        } else {
            players.put(playerId, new Player(playerId,
                                             PlayerAlignment.generateAlignment(countPlayersLeft(),
                                                                               countGoodPlayers())));
        }
    }

    public void handleMessage(PlayerMessage message, Stream serverStream) {

    }

    public void start() {

    }

    public boolean isOver() {
        return !bomb.getState().equals(BombState.Active);
    }

    public Collection<String> getPlayerIdentifiers() {
        return players.keySet();
    }

    public void printLobby(PrintStream out) {
        out.println("Game " + identifier + " lobby:");
        for (Player p : players.values()) {
            p.printStatus(out);
        }
    }

    public void printStatus(PrintStream out) {
        out.println("BOMB:");
        bomb.printStatus(out);

        out.println();
        out.println("PLAYERS:");
        for (Player p : players.values()) {
            p.printFullStatus(out);
        }
    }

    private void broadcast(String message, Stream serverStream) {
        for (Player p : players.values()) {
            serverStream.write(p.generateResponse(message));
        }
    }

    private String GenerateIdentifier(Collection<String> existingIdentifiers) {
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

    private int countPlayersLeft() {
        return Math.min(MAX_PLAYERS,
                        Math.max(players.size() + 1, 3));
    }

    private int countGoodPlayers() {
        int goods = 0;
        for (Player p : players.values()) {
            goods += (p.getAlignment().equals(PlayerAlignment.Good) ? 1 : 0);
        }
        return goods;
    }
}
