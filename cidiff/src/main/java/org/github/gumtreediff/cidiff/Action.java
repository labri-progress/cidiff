package org.github.gumtreediff.cidiff;

import java.util.Objects;

public final class Action {
    public static final int NO_LOCATION = -1;

    public enum Type {
        ADDED,
        DELETED,
        UPDATED,
        UNCHANGED,
    }

    public final LogLine leftLogLine;
    public final LogLine rightLogLine;
    public final Type type;

    private Action(LogLine leftLogLine, LogLine rightLogLine, Type type) {
        this.leftLogLine = leftLogLine;
        this.rightLogLine = rightLogLine;
        this.type = type;
    }

    @Override
    public String toString() {
        if (leftLogLine == null)
            return type + " [" + rightLogLine.lineNumber + "]";
        else if (rightLogLine == null)
            return type + " [" + leftLogLine.lineNumber + "]";
        else
            return type + " [" + leftLogLine.lineNumber + "-" + rightLogLine.lineNumber + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        final Action action = (Action) o;
        return leftLogLine == action.leftLogLine
                && rightLogLine == action.rightLogLine
                && type == action.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(leftLogLine, rightLogLine, type);
    }

    public static Action added(LogLine rightLogLine) {
        return new Action(null, rightLogLine, Type.ADDED);
    }

    public static Action deleted(LogLine leftLogLine) {
        return new Action(leftLogLine, null, Type.DELETED);
    }

    public static Action unchanged(LogLine leftLogLine, LogLine rightLogLine) {
        return new Action(leftLogLine, rightLogLine, Type.UNCHANGED);
    }

    public static Action updated(LogLine leftLogLine, LogLine rightLogLine) {
        return new Action(leftLogLine, rightLogLine, Type.UPDATED);
    }
}
