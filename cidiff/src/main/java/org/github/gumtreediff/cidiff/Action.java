package org.github.gumtreediff.cidiff;

public class Action {
    public enum Type {
        ADDED,
        DELETED,
        UPDATED,
        UNCHANGED,
    }

    public final int leftLocation;
    public final int rightLocation;
    public final Type type;

    public static final int NO_LOCATION = -1;

    private Action(int leftLocation, int rightLocation, Type type) {
        this.leftLocation = leftLocation;
        this.rightLocation = rightLocation;
        this.type = type;
    }

    public static Action added(int rightLocation) {
        return new Action(NO_LOCATION, rightLocation, Type.ADDED);
    }

    public static Action deleted(int leftLocation) {
        return new Action(leftLocation, NO_LOCATION, Type.DELETED);
    }

    public static Action unchanged(int leftLocation, int rightLocation) {
        return new Action(leftLocation, rightLocation, Type.UNCHANGED);
    }

    public static Action updated(int leftLocation, int rightLocation) {
        return new Action(leftLocation, rightLocation, Type.UPDATED);
    }
}
