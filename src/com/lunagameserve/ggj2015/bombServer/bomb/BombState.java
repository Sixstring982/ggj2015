package com.lunagameserve.ggj2015.bombServer.bomb;

/**
 * Created by six on 1/23/15.
 */
public enum BombState {
    Active() {
        @Override
        public String toString() {
            return "active";
        }
    },
    Defused() {
        @Override
        public String toString() {
            return "defused";
        }
    },
    Exploded() {
        @Override
        public String toString() {
            return "exploded";
        }
    };
}
