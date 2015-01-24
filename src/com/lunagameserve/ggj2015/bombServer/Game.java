package com.lunagameserve.ggj2015.bombServer;

import com.lunagameserve.ggj2015.bombServer.bomb.Bomb;
import com.lunagameserve.ggj2015.bombServer.bomb.BombState;
import com.lunagameserve.ggj2015.bombServer.player.Player;
import com.lunagameserve.ggj2015.bombServer.player.PlayerAlignment;
import com.lunagameserve.ggj2015.bombServer.player.PlayerMessage;
import com.lunagameserve.ggj2015.textServer.Stream;

import java.io.PrintStream;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by six on 1/23/15.
 */
public class Game {

    private static final int MAX_PLAYERS = 10;
    private static final int INFO_SEND_DELAY_MS = 10000;

    /**
     * Players in this game, indexed by their identifiers.
     */
    private HashMap<String, Player> players = new HashMap<>();
    private String defuserIdentifier;
    private HashSet<Player> informants;

    private Bomb bomb;
    private AtomicBoolean started = new AtomicBoolean(false);

    private Thread infoSendingThread;

    private final String identifier;

    public String getIdentifier() {
        return identifier;
    }

    public Game(Collection<String> existingIdentifiers) {
        this.identifier = GenerateIdentifier(existingIdentifiers);
    }

    public void join(String playerId, Stream serverStream) throws GameFullException,
                                                                  DuplicatePlayerException, GameInProgressException {
        if (started.get()) {
            throw new GameInProgressException("This game has already started.");
        } else if (players.containsKey(playerId)) {
            throw new DuplicatePlayerException("This player is already in this game.");
        } else if (players.size() >= MAX_PLAYERS) {
            throw new GameFullException("This game is full.");
        } else {
            players.put(playerId, new Player(playerId, serverStream));
        }
    }

    public void start(final Stream serverStream) {
        started.set(true);
        bomb = new Bomb(players.size());
        assignDefuser();
        assignAlignments();
        broadcastAlignments(serverStream);
        infoSendingThread = new Thread(new Runnable() {
            @Override
            public void run() {
                sendInfoLoop(serverStream);
            }
        });
        infoSendingThread.start();
    }

    public void stop() {
        started.set(false);
    }

    private void sendInfoLoop(Stream serverStream) {
        Log.debug("Starting info thread.");
        while(started.get()) {
            try {
                Thread.sleep(INFO_SEND_DELAY_MS);
            } catch (InterruptedException e) {
                Log.error("Interrupted while sleeping in sendInfoLoop.");
                System.exit(3);
            }

            Log.debug("Sending info...");
            bomb.sendInformation(informants);
        }
        Log.debug("Stopping info thread.");
    }

    private int badPlayerCount(int totalPlayers) {
        switch(totalPlayers) {
            case 3:  return 1;
            case 4:  return 2;
            case 5:  return 2;
            case 6:  return 2;
            case 7:  return 3;
            case 8:  return 3;
            case 9:  return 3;
            case 10: return 4;
            default: return 5;
        }
    }

    private int goodPlayerCount(int totalPlayers) {
        return totalPlayers - badPlayerCount(totalPlayers);
    }

    private void assignDefuser() {
        int defuserIdx = BombServer.getRandom().nextInt(players.size());
        informants = new HashSet<>();
        List<Player> playerList = new ArrayList<>();

        players.values().forEach(playerList::add);

        for (int i = 0; i < playerList.size(); i++) {
            if (i == defuserIdx) {
                defuserIdentifier = playerList.get(i).getIdentifier();
            } else {
                informants.add(playerList.get(0));
            }
            playerList.remove(0);
        }
    }

    private void assignAlignments() {
        int badLeft = badPlayerCount(players.size());

        List<Player> unassignedPlayers = new ArrayList<>();
        players.values().forEach(unassignedPlayers::add);

        for (int i = 0; i < badLeft; i++) {
            int badIdx = BombServer.getRandom().nextInt(unassignedPlayers.size());
            Player badPlayer = unassignedPlayers.remove(badIdx);
            badPlayer.setAlignment(PlayerAlignment.Evil);
        }

        while (unassignedPlayers.size() > 0) {
            Player p = unassignedPlayers.remove(0);
            p.setAlignment(PlayerAlignment.Good);
        }
    }

    private void broadcastAlignments(Stream serverStream) {
        for (Player p : players.values()) {
            p.sendResponse("The game has started. You are on team " + p.getAlignment() + ".");
            if (p.getIdentifier().equals(defuserIdentifier)) {
                p.sendResponse("You are defusing the bomb.");
            }
        }
    }

    public boolean isOver() {
        if (!started.get()) {
            return false;
        } else {
            return !bomb.getState().equals(BombState.Active);
        }
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
        if (bomb == null) {
            out.println("Game has not started.");
        } else {
            bomb.printStatus(out);
        }

        out.println();
        out.println("PLAYERS:");
        for (Player p : players.values()) {
            p.printFullStatus(out);
        }
    }

    private void broadcast(String message) {
        for (Player p : players.values()) {
            p.sendResponse(message);
        }
    }

    private String GenerateIdentifier(Collection<String> existingIdentifiers) {
        String newId;
        int idLoops = 0;
        do {
            newId = new Long(Math.abs(BombServer.getRandom().nextInt())).toString();
            idLoops++;

            if (idLoops > 1000) {
                System.err.println("Error creating game: 1000 random loops");
                System.exit(1);
            }
        } while (existingIdentifiers.contains(newId));

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

    public void handleMessage(PlayerMessage message) {
        if (message.getMessage().equals("start game")) {
            handleStartGameMessage(message);
        } else  if (message.getMessage().startsWith("cut ")) {

        } else {
            message.sendResponse("Unrecognized command.");
        }
        printStatus(System.err);
    }

    private void handleStartGameMessage(PlayerMessage message) {
        if (!started.get()) {
            start(message.getServerStream());
        } else {
            message.sendResponse("The game has already started.");
        }
    }

    public boolean hasStarted() {
        return started.get();
    }
}
