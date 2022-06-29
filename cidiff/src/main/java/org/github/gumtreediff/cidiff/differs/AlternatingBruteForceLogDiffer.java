package org.github.gumtreediff.cidiff.differs;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.github.gumtreediff.cidiff.*;

public final class AlternatingBruteForceLogDiffer extends AbstractLogDiffer {
    private static final String DEFAULT_REWRITE_MIN = "0.5";

    private final double rewriteMin;

    public AlternatingBruteForceLogDiffer(Properties options) {
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
        int lastRightUnchanged = 0; // Last mapped right position
        for (int i = 0; i < lines.left.size(); i++) {
            final LogLine leftLine = lines.left.get(i);
            for (int j = 0; j < lines.right.size(); j++) {
                final int upperIndex = lastRightUnchanged + j;
                if (upperIndex < lines.right.size()) {
                    final LogLine upperRightLine = lines.right.get(upperIndex);
                    if (actions.right.containsKey(upperRightLine))
                        continue;

                    if (leftLine.hasSameValue(upperRightLine)) {
                        lastRightUnchanged = upperIndex;
                        final Action action = Action.unchanged(leftLine, upperRightLine);
                        actions.left.put(leftLine, action);
                        actions.right.put(upperRightLine, action);
                        break;
                    }
                }

                final int lowerIndex = lastRightUnchanged - j;
                if (lowerIndex > 0 && lowerIndex != upperIndex) {
                    final LogLine lowerRightLine = lines.right.get(lowerIndex);
                    if (actions.right.containsKey(lowerRightLine))
                        continue;

                    if (leftLine.hasSameValue(lowerRightLine)) {
                        lastRightUnchanged = lowerIndex;
                        final Action action = Action.unchanged(leftLine, lowerRightLine);
                        actions.left.put(leftLine, action);
                        actions.right.put(lowerRightLine, action);
                        break;
                    }
                }
            }
        }

        // Identify updated lines
        int lastRightUpdated = 0; // Last mapped right position
        for (int i = 0; i < lines.left.size(); i++) {
            final LogLine leftLine = lines.left.get(i);
            if (actions.left.containsKey(leftLine)) // Left line already mapped
                continue;

            for (int j = 0; j < lines.right.size(); j++) {
                final int upperIndex = lastRightUpdated + j;
                if (upperIndex < lines.right.size()) {
                    final LogLine upperRightLine = lines.right.get(upperIndex);
                    if (actions.right.containsKey(upperRightLine))
                        continue;
                    final double upperSim = Utils.rewriteSim(leftLine, upperRightLine);
                    if (upperSim >= rewriteMin) {
                        lastRightUpdated = upperIndex;
                        final Action action = Action.updated(leftLine, upperRightLine);
                        actions.left.put(leftLine, action);
                        actions.right.put(upperRightLine, action);
                        break;
                    }
                }

                final int lowerIndex = lastRightUpdated - j;
                if (lowerIndex > 0 && lowerIndex
                        != upperIndex) {
                    final LogLine lowerRightLine = lines.right.get(lowerIndex);
                    if (actions.right.containsKey(lowerRightLine))
                        continue;

                    final double lowerSim = Utils.rewriteSim(leftLine, lowerRightLine);
                    if (lowerSim >= rewriteMin) {
                        lastRightUpdated = lowerIndex;
                        final Action action = Action.updated(leftLine, lowerRightLine);
                        actions.left.put(leftLine, action);
                        actions.right.put(lowerRightLine, action);
                        break;
                    }
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
