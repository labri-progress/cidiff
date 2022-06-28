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
            log.add(new LogLine(lines[i], i + 1, i, 0, lines[i].length()));

        return log;
    }

    public static Action[] makeActions(Action.Type... types) {
        final Action[] actions = new Action[types.length];
        for (int i = 0; i < actions.length; i++)
            actions[i] = switch (types[i]) {
                case ADDED -> Action.added(i);
                case DELETED -> Action.deleted(i);
                case UPDATED -> Action.updated(i, i);
                case UNCHANGED -> Action.unchanged(i, i);
                default -> Action.unchanged(i, i);
            };
        return actions;
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
