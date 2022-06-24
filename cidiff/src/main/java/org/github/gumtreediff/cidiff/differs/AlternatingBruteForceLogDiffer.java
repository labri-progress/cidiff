package org.github.gumtreediff.cidiff.differs;

import java.util.List;
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
    public Pair<Action[]> diff(Pair<List<LogLine>> lines) {
        final Pair<Action[]> actions = new Pair<>(
                new Action[lines.left.size()], new Action[lines.right.size()]
        );

        // Identify unchanged lines
        int lastRightUnchanged = 0; // Last mapped right position
        for (int i = 0; i < lines.left.size(); i++) {
            final String leftLine = lines.left.get(i).value;
            for (int j = 0; j < lines.right.size(); j++) {
                final int upperIndex = lastRightUnchanged + j;
                if (upperIndex < actions.right.length && actions.right[upperIndex] == null) {
                    final String upperRightLine = lines.right.get(upperIndex).value;
                    if (leftLine.equals(upperRightLine)) {
                        lastRightUnchanged = upperIndex;
                        final Action action = Action.unchanged(i, upperIndex);
                        actions.left[i] = action;
                        actions.right[upperIndex] = action;
                        break;
                    }
                }

                final int lowerIndex = lastRightUnchanged - j;
                if (lowerIndex > 0 && lowerIndex != upperIndex
                        && actions.right[lowerIndex] == null) {
                    final String lowerRightLine = lines.right.get(lowerIndex).value;
                    if (leftLine.equals(lowerRightLine)) {
                        lastRightUnchanged = lowerIndex;
                        final Action action = Action.unchanged(i, lowerIndex);
                        actions.left[i] = action;
                        actions.right[lowerIndex] = action;
                        break;
                    }
                }
            }
        }

        // Identify updated lines
        int lastRightUpdated = 0; // Last mapped right position
        for (int i = 0; i < lines.left.size(); i++) {
            if (actions.left[i] != null) // Left line already mapped
                continue;

            final String leftLine = lines.left.get(i).value;
            for (int j = 0; j < lines.right.size(); j++) {
                final int upperIndex = lastRightUpdated + j;
                if (upperIndex < actions.right.length && actions.right[upperIndex] == null) {
                    final String upperRightLine = lines.right.get(upperIndex).value;
                    final double upperSim = Utils.rewriteSim(leftLine, upperRightLine);
                    if (upperSim >= rewriteMin) {
                        lastRightUpdated = upperIndex;
                        final Action action = Action.updated(i, upperIndex);
                        actions.left[i] = action;
                        actions.right[upperIndex] = action;
                        break;
                    }
                }

                final int lowerIndex = lastRightUpdated - j;
                if (lowerIndex > 0 && lowerIndex
                        != upperIndex && actions.right[lowerIndex] == null) {
                    final String lowerRightLine = lines.right.get(lowerIndex).value;
                    final double lowerSim = Utils.rewriteSim(leftLine, lowerRightLine);
                    if (lowerSim >= rewriteMin) {
                        lastRightUpdated = lowerIndex;
                        final Action action = Action.updated(i, lowerIndex);
                        actions.left[i] = action;
                        actions.right[lowerIndex] = action;
                        break;
                    }
                }
            }
        }

        // Identify deleted lines
        for (int i = 0; i < lines.left.size(); i++)
            if (actions.left[i] == null)
                actions.left[i] = Action.deleted(i);

        // Identify added lines
        for (int i = 0; i < lines.right.size(); i++)
            if (actions.right[i] == null)
                actions.right[i] = Action.added(i);

        return actions;
    }
}
