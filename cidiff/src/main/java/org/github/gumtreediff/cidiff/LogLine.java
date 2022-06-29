package org.github.gumtreediff.cidiff;

import java.util.Objects;

public final class LogLine {
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
        return value.equals(logLine.value) && lineNumber == logLine.lineNumber;
    }

    public boolean hasSameValue(LogLine line) {
        return value.equals(line.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, lineNumber);
    }

    @Override
    public String toString() {
        return lineNumber + " " + value;
    }
}
