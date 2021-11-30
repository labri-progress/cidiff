package org.github.gumtreediff.cidiff;

import java.util.Objects;

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

    @Override
    public String toString() {
        return type.toString() + " [" + leftLocation + "," + rightLocation + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Action action = (Action) o;
        return leftLocation == action.leftLocation && rightLocation == action.rightLocation && type == action.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(leftLocation, rightLocation, type);
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
