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
    private String createGame() {
        Game game = new Game(liveGames.keySet());
        liveGames.put(game.getIdentifier(), game);
        return game.getIdentifier();
    }

    private void destroyGame(Game game) {
        liveGames.remove(game.getIdentifier());
        game.getPlayerIdentifiers().forEach(playerGames::remove);
    }

    private void joinPlayer(String playerId, String gameId) throws GameFullException, DuplicatePlayerException {
        Game game = liveGames.get(gameId);
        playerGames.put(playerId, game);
        game.join(playerId);
    }

    public void broadcastShutdown(Stream stream) {
        for (String id : playerGames.keySet()) {
            stream.write(id + " The server is shutting down.");
        }
    }

    public void handleMessage(PlayerMessage message, Stream serverStream) {
        /* Here we handle master lobby level commands. */
        if (message.getMessage().equals("new game")) {

        } else {
            /* Not master lobby level? Find the game and have it handle the command. */
            Game game = liveGames.get(message.getPlayerID());
            if (game == null) {
                serverStream.write(message.generateResponse("You are not in a game. First, join or create one."));
            } else {
                game.handleMessage(message, serverStream);

                if (game.isOver()) {
                    destroyGame(game);
                }
            }
        }
    }
}
