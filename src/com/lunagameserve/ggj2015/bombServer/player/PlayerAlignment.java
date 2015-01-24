package com.lunagameserve.ggj2015.bombServer.player;

import com.lunagameserve.ggj2015.bombServer.BombServer;

/**
 * Created by Six on 1/24/2015.
 */
public enum PlayerAlignment {
    Good() {
        @Override
        public String toString() {
            return "good";
        }
    },
    Evil() {
        @Override
        public String toString() {
            return "evil";
        }
    };

    public static PlayerAlignment generateAlignment(int totalLeft, int goodLeft) {
        if (BombServer.getRandom().nextInt(totalLeft) > goodLeft) {
            return Evil;
        } else {
            return Good;
        }
    }
}