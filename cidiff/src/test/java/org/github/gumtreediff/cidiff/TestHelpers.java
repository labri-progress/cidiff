package org.github.gumtreediff.cidiff;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public final class TestHelpers {
    private TestHelpers() {
    }

    public static List<LogLine> makeLog(String... lines) {
        final List<LogLine> log = new ArrayList<>(lines.length);
        for (int i = 0; i < lines.length; i++)
            log.add(new LogLine(lines[i], i, i, 0, lines[i].length()));

        return log;
    }

    public static Properties makeOptions(String... values) {
        if (values.length % 2 != 0)
                throw new IllegalArgumentException("Options are key values pairs.");

        final Properties options = new Properties();
        for (int i = 0; i < values.length; i = i + 2)
            options.setProperty(values[i], values[i + 1]);

        return options;
    }
}
