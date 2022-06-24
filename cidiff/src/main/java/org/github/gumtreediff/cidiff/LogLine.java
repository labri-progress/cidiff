package org.github.gumtreediff.cidiff;

import java.util.Objects;

public class LogLine {
    public final String value;

    public final int lineNumber;

    public final int startOffset;

    public final int endOffset;

    public LogLine(String value, int lineNumber, int startOffset, int endOffset) {
        Objects.requireNonNull(value);
        this.value = value;
        this.lineNumber = lineNumber;
        this.startOffset = startOffset;
        this.endOffset = endOffset;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        final LogLine logLine = (LogLine) o;
        return value.equals(logLine.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
