package com.lunagameserve.ggj2015.client;

import com.lunagameserve.ggj2015.bombServer.Log;

/**
 * Created by six on 1/24/15.
 */
public class Arguments {
    private String serverIP = "127.0.0.1";
    private int serverPort  = 9911;
    private String gameID   = "12345678";

    public String getServerIP() {
        return serverIP;
    }

    public int getServerPort() {
        return serverPort;
    }

    public String getGameID() {
        return gameID;
    }

    public Arguments(String[] args) {
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-ip":
                case "--serverip":
                    if (i + 1 < args.length) {
                        serverIP = args[++i];
                    }
                    break;

                case "--port":
                case "--serverport":
                    if (i + 1 < args.length) {
                        serverPort = Integer.parseInt(args[++i]);
                    }
                    break;

                case "-id":
                case "--gameid":
                    if (i + 1 < args.length) {
                        gameID = args[++i];
                    }
                    break;

                default:
                    Log.warning("Unknown command line argument: <" + args[i] + ">");
                    break;
            }
        }
    }
}
