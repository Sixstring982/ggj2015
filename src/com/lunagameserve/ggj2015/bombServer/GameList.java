package com.lunagameserve.ggj2015.bombServer;

import com.lunagameserve.ggj2015.bombServer.player.PlayerMessage;
import com.lunagameserve.ggj2015.textServer.Stream;
import sun.misc.CharacterDecoder;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.HashMap;

/**
 * Created by Six on 1/24/2015.
 */
public class GameList {
    /**
     * {@link Game}s indexed by their identifiers.
     */
    private HashMap<String, Game> liveGames = new HashMap<>();

    /**
     * {@link Game}s indexed by player identifiers.
     */
    private HashMap<String, Game> playerGames = new HashMap<>();

    /**
     * Creates a new game and inserts it into this {@link GameList}.
     * @return The identifier of the created {@link Game}.
     */
    private String createGame(String creatorIdentifier) {
        Game game = new Game(liveGames.keySet(), creatorIdentifier);
        liveGames.put(game.getIdentifier(), game);
        return game.getIdentifier();
    }

    private void destroyGame(Game game) {
        liveGames.remove(game.getIdentifier());
        game.getPlayerIdentifiers().forEach(playerGames::remove);
    }

    private void joinPlayer(String playerId, String gameId, Stream serverStream) throws GameFullException,
                                                                                        DuplicatePlayerException,
                                                                                        GameInProgressException {
        Game game = liveGames.get(gameId);
        playerGames.put(playerId, game);
        game.join(playerId, serverStream);
    }

    public void broadcastShutdown(Stream stream) {
        for (String id : playerGames.keySet()) {
            stream.write(id + " The server is shutting down.");
        }
    }

    public void handleMessage(PlayerMessage message) {
        Game game = getByPlayerMessage(message);

        synchronized (Game.persistedLock) {
            if (!Game.persistedPlayerScores.containsKey(message.getPlayerID())) {
                Game.persistedPlayerScores.put(message.getPlayerID(), 0);
                for(String s : Game.getRulesStrings()) {
                    message.sendResponse(s);
                }
            }
        }
        if (game == null) {
        /* Here we handle master lobby level commands. */
            if (handleNewGameCommand("new game", message)) {
            } else if (handleNewGameCommand("new", message)) {
            } else if (handleListGamesCommand("list", message)) {
            } else if (handleListGamesCommand("list games", message)) {
            } else if (handleJoinCommand("join game", message)) {
            } else if (handleJoinCommand("join", message)) {
            } else if (Game.handleHelpMessage("rules", message)) {
            } else if (Game.handleHelpMessage("?", message)) {
            } else if (Game.handleHelpMessage("help", message)) {
            } else {
                message.sendResponse("Unrecognized command. Text 'help' for help.");
            }
        } else {
            game.handleMessage(message);

            if (game.isOver()) {
                destroyGame(game);
            }
        }
    }

    private boolean handleNewGameCommand(String command, PlayerMessage message) {
        if(!message.getMessage().equals(command)) {
            return false;
        }
        String newGameID = createGame(message.getPlayerID());
        try {
            joinPlayer(message.getPlayerID(), newGameID, message.getServerStream());
        } catch(Exception e) {
            /* This will not be thrown. */
            Log.error("Impossibly thrown exception in handleNewGameCommand: " + e.getMessage());
            System.exit(2);
        }
        message.sendResponse("You have joined game " + newGameID + ".");
        return true;
    }

    private boolean handleListGamesCommand(String command, PlayerMessage message) {
        if(!message.getMessage().equals(command)) {
            return false;
        }
        StringBuilder builder = new StringBuilder();
        for (String s : liveGames.keySet()) {
            builder.append(s + (liveGames.get(s).hasStarted() ? " (in progress)" : "") + "\n");
        }
        message.sendResponse("Games:\n" + builder.toString());
        return true;
    }

    private static String getUnicodeChar(int codepoint) {
        return new String(Character.toChars(codepoint));
    }

    private static String getUtf8Char(int[] bytes) {
        Charset utf8 = Charset.forName("UTF-8");
        byte[] bbytes = new byte[bytes.length];
        for(int i = 0; i < bytes.length; ++i) {
            bbytes[i] = (byte)bytes[i];
        }
        ByteBuffer buffer = ByteBuffer.wrap(bbytes);
        return utf8.decode(buffer).toString();
    }

    private boolean handleJoinCommand(String command, PlayerMessage message) {
        if(!message.getMessage().startsWith(command+" ")) {
            return false;
        }
        String joinId = message.getMessage().substring((command+" ").length(), message.getMessage().length());
        if (liveGames.keySet().contains(joinId)) {
            try {
                joinPlayer(message.getPlayerID(), joinId, message.getServerStream());
                message.sendResponse("You have joined game " + joinId);
            } catch (GameFullException e) {
                message.sendResponse("Game " + joinId + " is full.");
            } catch (DuplicatePlayerException e) {
                message.sendResponse("You have already joined this game.");
            } catch (GameInProgressException e) {
                message.sendResponse("This game has already started.");
            }
        } else {
            message.sendResponse("Game " + joinId + " does not exist.");
        }
        return true;
    }

    private Game getByPlayerID(String playerID) {
        return playerGames.get(playerID);
    }

    private Game getByPlayerMessage(PlayerMessage message) {
        return playerGames.get(message.getPlayerID());
    }

    private Game getByGameID(String gameID) {
        return liveGames.get(gameID);
    }
}
