package org.github.gumtreediff.cidiff.differs;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.github.gumtreediff.cidiff.*;

public final class BruteForceLogDiffer extends AbstractLogDiffer {
    private static final String DEFAULT_REWRITE_MIN = "0.5";

    private final double rewriteMin;

    public BruteForceLogDiffer(Properties options) {
        super(options);
        rewriteMin = Double.parseDouble(
                options.getProperty(Options.DIFFER_REWRITE_MIN, DEFAULT_REWRITE_MIN)
        );
    }

    @Override
    public Pair<Map<LogLine, Action>> diff(Pair<List<LogLine>> lines) {
        final Pair<Map<LogLine, Action>> actions = new Pair<>(
                new HashMap<>(), new HashMap<>()
        );

        // Identify unchanged lines
        for (int i = 0; i < lines.left.size(); i++) {
            final LogLine leftLine = lines.left.get(i);
            for (int j = 0; j < lines.right.size(); j++) {
                final LogLine rightLine = lines.right.get(j);
                if (actions.right.containsKey(rightLine))
                    continue;

                if (leftLine.hasSameValue(rightLine)) {
                    final Action action = Action.unchanged(leftLine, rightLine);
                    actions.left.put(leftLine, action);
                    actions.right.put(rightLine, action);
                    break;
                }
            }
        }

        // Identify updated lines
        for (int i = 0; i < lines.left.size(); i++) {
            final LogLine leftLine = lines.left.get(i);
            if (actions.left.containsKey(leftLine))
                continue;

            for (int j = 0; j < lines.right.size(); j++) {
                final LogLine rightLine = lines.right.get(j);
                if (actions.right.containsKey(rightLine))
                    continue;

                final double sim = Utils.rewriteSim(leftLine, rightLine);
                if (sim >= rewriteMin) {
                    final Action action = Action.updated(leftLine, rightLine);
                    actions.left.put(leftLine, action);
                    actions.right.put(rightLine, action);
                    break;
                }
            }
        }

        // Identify deleted lines
        for (LogLine leftLine : lines.left)
            if (!actions.left.containsKey(leftLine))
                actions.left.put(leftLine, Action.deleted(leftLine));

        // Identify added lines
        for (LogLine rightLine : lines.right)
            if (!actions.right.containsKey(rightLine))
                actions.right.put(rightLine, Action.added(rightLine));

        return actions;
    }
}
