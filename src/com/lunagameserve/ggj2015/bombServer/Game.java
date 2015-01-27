package com.lunagameserve.ggj2015.bombServer;

import com.lunagameserve.ggj2015.bombServer.bomb.Bomb;
import com.lunagameserve.ggj2015.bombServer.bomb.BombState;
import com.lunagameserve.ggj2015.bombServer.bomb.WireState;
import com.lunagameserve.ggj2015.bombServer.player.Player;
import com.lunagameserve.ggj2015.bombServer.player.PlayerAlignment;
import com.lunagameserve.ggj2015.bombServer.player.PlayerMessage;
import com.lunagameserve.ggj2015.textServer.Stream;

import java.io.PrintStream;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BooleanSupplier;

/**
 * @author six
 * @since 1/23/15
 */
public class Game {

    private static final int MIN_PLAYERS = 3;
    private static final int MAX_PLAYERS = 10;
    private static final int INFO_SEND_DELAY_MS = 10000;

    public static Object persistedLock = new Object();
    public static HashMap<String, Integer> persistedPlayerScores = new HashMap<>();
    public static HashMap<String, String> persistedPlayerDisplayNames = new HashMap<>();

    /**
     * Players in this game, indexed by their identifiers.
     */
    private HashMap<String, Player> players = new HashMap<>();
    private String defuserIdentifier;
    private HashSet<Player> informants;

    private Bomb bomb;
    private AtomicBoolean started = new AtomicBoolean(false);

    private AtomicBoolean gameOver = new AtomicBoolean(false);

    private final String creatingPlayerIdentifier;

    private final String identifier;

    public String getIdentifier() {
        return identifier;
    }

    public Game(Collection<String> existingIdentifiers, String creatingPlayerIdentifier) {
        this.creatingPlayerIdentifier = creatingPlayerIdentifier;
        this.identifier = GenerateIdentifier(existingIdentifiers);
    }

    public void join(String playerId, Stream serverStream) throws GameFullException,
                                                                  DuplicatePlayerException, GameInProgressException {
        if (gameOver.get()) {
            throw new GameInProgressException("The game is over.");
        } else if(players.containsKey(playerId)) {
            throw new DuplicatePlayerException("This player is already in this game.");
        } else if (players.size() >= MAX_PLAYERS) {
            throw new GameFullException("This game is full.");
        } else {
            Player newPlayer = new Player(playerId, serverStream);
            players.put(playerId, newPlayer);
            synchronized (persistedLock) {
                if (persistedPlayerDisplayNames.containsKey(newPlayer.getIdentifier())) {
                    newPlayer.setDisplayName(persistedPlayerDisplayNames.get(newPlayer.getIdentifier()));
                }
            }
            if(started.get()) {
                if(BombServer.getRandom().nextBoolean()) {
                    newPlayer.setAlignment(PlayerAlignment.Evil);
                } else {
                    newPlayer.setAlignment(PlayerAlignment.Good);
                }
            }
            broadcast(newPlayer.getDisplayName()+" has joined the game.");
        }
    }

    public void start() {
        started.set(true);
        bomb = new Bomb(players.size());
        assignDefuser();
        assignAlignments();
        broadcastAlignments();
        Thread infoSendingThread = new Thread(this::sendInfoLoop);
        infoSendingThread.start();
    }

    public void stop() {
        started.set(false);
        gameOver.set(true);
    }

    private void sendInfoLoop() {
        Log.debug("Starting info thread.");
        while(started.get()) {
            Log.debug("Sending info...");
            bomb.sendInformation(informants);

            try {
                Thread.sleep(INFO_SEND_DELAY_MS);
            } catch (InterruptedException e) {
                Log.error("Interrupted while sleeping in sendInfoLoop.");
                System.exit(3);
            }

            bomb.tickTimer();
            if (bomb.outOfTime()) {
                broadcastGameEnd("BOOM!!! The bomb exploded.", PlayerAlignment.Evil);
                stop();
            }
        }
        Log.debug("Stopping info thread.");
    }

    private void broadcastGameEnd(String prefix, PlayerAlignment winner) {
        boolean didPlayerWin;
        StringBuilder response;
        for (Player p : players.values()) {
            didPlayerWin = p.getAlignment() == winner;
            synchronized (persistedLock) {
                int playerScore = 0;
                if (persistedPlayerScores.containsKey(p.getIdentifier())) {
                    playerScore = persistedPlayerScores.get(p.getIdentifier());
                    if(didPlayerWin) {
                        playerScore++;
                    }
                }
                persistedPlayerScores.put(p.getIdentifier(), playerScore);
            }
        }
        for (Player p : players.values()) {
            response = new StringBuilder();
            didPlayerWin = p.getAlignment() == winner;
            response.append(prefix).append(" YOU ").append(didPlayerWin ? "WIN" : "LOSE").append("!");
            int thisPlayerScore = 0;
            synchronized (persistedLock) {
                if (persistedPlayerScores.containsKey(p.getIdentifier())) {
                    thisPlayerScore = persistedPlayerScores.get(p.getIdentifier());
                }
            }
            p.sendResponse(response.toString());

            response = new StringBuilder();
            response.append("You: ("+p.getAlignment()+"): "+thisPlayerScore+"\n");

            for (Player pp : players.values()) {
                if(pp == p) {
                    continue;
                }
                int playerScore = 0;
                synchronized (persistedLock) {
                    if (persistedPlayerScores.containsKey(pp.getIdentifier())) {
                        playerScore = persistedPlayerScores.get(pp.getIdentifier());
                    }
                }
                response.append(pp.getDisplayName()).append(" (").append(pp.getAlignment()).append("): ").append(playerScore).append("\n");
            }
            p.sendResponse(response.toString());
        }
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

    private void assignDefuser() {
        int defuserIdx = BombServer.getRandom().nextInt(players.size());
        informants = new HashSet<>();
        List<Player> playerList = new ArrayList<>();

        players.values().forEach(playerList::add);

        for (int i = 0; i < players.size(); i++) {
            if (i == defuserIdx) {
                defuserIdentifier = playerList.get(i).getIdentifier();
            } else {
                informants.add(playerList.get(i));
            }
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

    private void broadcastAlignments() {
        StringBuilder response;
        for (Player p : players.values()) {
            response = new StringBuilder();
            boolean isDefuser = p.getIdentifier().equals(defuserIdentifier);
            boolean isEvil = (p.getAlignment() == PlayerAlignment.Evil);
            response.append("You are in a room with a bomb."
                    +" "+bomb.getGoodWires()+" wires are GOOD."
                    +" "+bomb.getBadWires() + " wires are BAD.");
            p.sendResponse(response.toString());

            response = new StringBuilder();
            if(isDefuser) {
                response.append("You are the DEFUSER, " + (isEvil ? "but you are EVIL!!" : " and you are GOOD."));
            } else {
                response.append("\nYou are " + (isEvil ? "an EVIL " : "a GOOD ") + "INFORMANT.");
                response.append(" "+defuserIdentifier+" is the defuser.");
            }
            p.sendResponse(response.toString());

            response = new StringBuilder();
            response.append("The bomb will explode in 5 minutes. GO!!!");
            p.sendResponse(response.toString());
        }
    }

    public boolean isOver() {
        return gameOver.get();
    }

    public Collection<String> getPlayerIdentifiers() {
        return players.keySet();
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
            // gke: changing this so we have shorter ids
            newId = Integer.toString(idLoops); // Long.toString(Math.abs(BombServer.getRandom().nextInt()));
            idLoops++;

            if (idLoops > 1000) {
                System.err.println("Error creating game: 1000 random loops");
                System.exit(1);
            }
        } while (existingIdentifiers.contains(newId));

        return newId;
    }

    public void handleMessage(PlayerMessage message) {
        if (handleStartGameMessage("start", message)) {
        } else if (handleStartGameMessage("start game", message)) {
        } else if (handleDestroyGameMessage("destroy game", message)) {
        } else if (handleCutWireMessage("cut", message)) {
        } else if (handleCutWireMessage("cut wire", message)) {
        } else if (handleChangeNameMessage("i'm", message)) {
        } else if (handleChangeNameMessage("i am", message)) {
        } else if (handleChangeNameMessage("my name is", message)) {
        } else if (handleChangeNameMessage("name", message)) {
        } else if (handleVoteKillMessage("votekill", message)) {
        } else if (handleVoteKillMessage("kill", message)) {
        } else if (handleVoteKillMessage("vote kill", message)) {
        } else if (handleHelpMessage("rules", message)) {
        } else if (handleHelpMessage("?", message)) {
        } else if (handleHelpMessage("help", message)) {
        } else {
            message.sendResponse("Unrecognized command. Text 'help' for help.");
        }

        printStatus(System.err);
    }

    private boolean handleVoteKillMessage(String command, PlayerMessage message) {
        if(!message.getMessage().equals(command) && !message.getMessage().startsWith(command+" ")) {
            return false;
        }
        if (message.getPlayerID().equals(defuserIdentifier)) {
            message.sendResponse("You are the defuser, so you cannot vote to kill the defuser.");
        } else {
            boolean newValue;
            if(message.getMessage().equals(command)) {
                newValue = true;
            } else {
                String vkStatus = message.getMessage().substring((command + " ").length()).trim();
                switch (vkStatus) {
                    case "on":
                        newValue = true;
                        break;
                    case "off":
                        newValue = false;
                        break;
                    default:
                        message.sendResponse("Votekill may be turned on or off, e.g. 'votekill off'.");
                        return true;
                }
            }
            Player player = players.get(message.getPlayerID());
            player.setVoteKill(newValue);
            broadcast(player.getDisplayName() + (newValue ? " votes to kill the defuser!" :
                                                            " retracts their vote to kill the defuser."));
            recalculateKillVotes();
        }
        return true;
    }

    private void recalculateKillVotes() {
        int votesRemaining = (int) (Math.ceil(informants.size() / 2) + 1);
        for (Player p : players.values()) {
            if (p.getVoteKill()) {
                votesRemaining--;
                if (votesRemaining <= 0) {
                    broadcastGameEnd("The informants have killed the defuser!",
                                     PlayerAlignment.opposite(players.get(defuserIdentifier).getAlignment()));
                    stop();
                }
            }
        }
    }

    private boolean handleChangeNameMessage(String command, PlayerMessage message) {
        if(!message.getMessage().startsWith(command+" ")) {
            return false;
        }
        String newName = message.getMessage().substring((command+" ").length()).trim();
        players.get(message.getPlayerID()).setDisplayName(newName);
        synchronized (persistedLock) {
            persistedPlayerDisplayNames.put(message.getPlayerID(), newName);
        }
        return true;
    }

    private boolean handleDestroyGameMessage(String command, PlayerMessage message) {
        if(!message.getMessage().equals(command)) {
            return false;
        }
        if (!message.getPlayerID().equals(creatingPlayerIdentifier)) {
            message.sendResponse("You did not create this game.");
        } else if (started.get()) {
            message.sendResponse("The game has already started.");
        } else {
            stop();
        }
        return true;
    }

    private boolean handleCutWireMessage(String command, PlayerMessage message) {
        if(!message.getMessage().startsWith(command + " ")) {
            return false;
        }
        String wireId = message.getMessage().substring("cut ".length(), message.getMessage().length());
        if (defuserIdentifier.equals(message.getPlayerID())) {
            if (bomb.hasWireIdentifier(wireId)) {
                bomb.cutWire(wireId);
                broadcast("Wire " + wireId + " was cut!" +
                          (bomb.getWireState(wireId).equals(WireState.Bad) ? " It was a bad wire!" : ""));
                if (bomb.getState().equals(BombState.Defused)) {
                    broadcastGameEnd("The bomb has been defused!", PlayerAlignment.Good);
                    stop();
                } else if (bomb.getState().equals(BombState.Exploded)) {
                    broadcastGameEnd("The bomb exploded!!!", PlayerAlignment.Evil);
                    stop();
                }
            } else {
                message.sendResponse("There is no wire named <" + wireId + ">.");
            }
        } else {
            message.sendResponse("You are not defusing the bomb.");
        }
        return true;
    }

    private boolean handleStartGameMessage(String command, PlayerMessage message) {
        if(!message.getMessage().equals(command)) {
            return false;
        }
        if (!message.getPlayerID().equals(creatingPlayerIdentifier)) {
            message.sendResponse("You did not create this game.");
        } else {
            if (players.size() < MIN_PLAYERS) {
                message.sendResponse("This game has " + players.size() +
                                     " players, but needs at least " + MIN_PLAYERS + "to begin.");
            } else {
                if (!started.get()) {
                    start();
                } else {
                    message.sendResponse("The game has already started.");
                }
            }
        }
        return true;
    }

    public static boolean handleHelpMessage(String command, PlayerMessage message) {
        if(!message.getMessage().equals(command)) {
            return false;
        }
        for(String s : getRulesStrings()) {
            message.sendResponse(s);
        }
        return true;
    }

    public static String[] getRulesStrings() {
        return new String[] {
                "THE DEFUSER is a game of trust and strategy."+
                "\nSee http://bit.ly/1L93ms6 for rules."
        };
    }

    public boolean hasStarted() {
        return started.get();
    }
}
