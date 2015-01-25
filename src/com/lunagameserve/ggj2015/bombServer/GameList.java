package com.lunagameserve.ggj2015.bombServer;

import com.lunagameserve.ggj2015.bombServer.player.PlayerMessage;
import com.lunagameserve.ggj2015.textServer.Stream;

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
        if (game == null) {
        /* Here we handle master lobby level commands. */
            if (message.getMessage().equals("new game")) {
                handleNewGameCommand(message);
            } else if (message.getMessage().equals("list games")) {
                handleListGamesCommand(message);
            } else if (message.getMessage().startsWith("join ")) {
                handleJoinCommand(message);
            }
        } else {
            game.handleMessage(message);

            if (game.isOver()) {
                destroyGame(game);
            }
        }
    }

    private void handleNewGameCommand(PlayerMessage message) {
        String newGameID = createGame(message.getPlayerID());
        try {
            joinPlayer(message.getPlayerID(), newGameID, message.getServerStream());
        } catch(Exception e) {
            /* This will not be thrown. */
            Log.error("Impossibly thrown exception in handleNewGameCommand: " + e.getMessage());
            System.exit(2);
        }
        message.sendResponse("You have joined game " + newGameID + ".");
    }

    private void handleListGamesCommand(PlayerMessage message) {
        StringBuilder builder = new StringBuilder();
        for (String s : liveGames.keySet()) {
            builder.append(s + (liveGames.get(s).hasStarted() ? " (in progress)" : "") + "\n");
        }
        message.sendResponse("Games:\n" + builder.toString());
    }

    private void handleJoinCommand(PlayerMessage message) {
        String joinId = message.getMessage().substring("join ".length(), message.getMessage().length());
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
