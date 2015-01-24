package com.lunagameserve.ggj2015.bombServer.player;

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
}