package com.lunagameserve.ggj2015.bombServer.bomb;

/**
 * Created by six on 1/24/15.
 */
public class Wire {
    private boolean cut = false;
    private final String identifier;
    private WireState state;

    public WireState getState() {
        return state;
    }

    /* package protected */ void setState(WireState state) {
        this.state = state;
    }

    public String getIdentifier() {
        return identifier;
    }

    public Wire(int index) {
        this.identifier = generateIdentifier(index);
    }

    private String generateIdentifier(int index) {
        return new Integer(index).toString();
    }

    public String getStateString() {
        if (!cut) {
            return "--------------";
        } else if (state == WireState.Bad) {
            return "   CUT BAD";
        } else {
            return "   CUT";
        }
    }

    public void cut() {
        this.cut = true;
    }

    public boolean isCut() {
        return cut;
    }
}
