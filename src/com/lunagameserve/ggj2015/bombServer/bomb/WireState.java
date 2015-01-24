package com.lunagameserve.ggj2015.bombServer.bomb;

/**
 * Created by Six on 1/24/2015.
 */
public enum WireState {
    Bad() {
        @Override
        public String toString() {
            return "bad";
        }
    },
    Good() {
        @Override
        public String toString() {
            return "good";
        }
    },
    Neutral() {
        @Override
        public String toString() {
            return "neutral";
        }
    };
}
