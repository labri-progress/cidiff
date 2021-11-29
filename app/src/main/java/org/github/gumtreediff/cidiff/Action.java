package org.github.gumtreediff.cidiff;

public class Action {
    public final int leftLocation;
    public final int rightLocation;
    public final ActionType type;

    public static final int NO_LOCATION = -1;

    public Action(int leftLocation, int rightLocation, ActionType type) {
        this.leftLocation = leftLocation;
        this.rightLocation = rightLocation;
        this.type = type;
    }
}
