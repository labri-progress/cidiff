package org.github.gumtreediff.cidiff;

import java.util.List;
import java.util.Properties;

public final class AlternatingBruteForceStepDiffer extends AbstractStepDiffer {
    private final static String DEFAULT_REWRITE_MIN = "0.5";

    private final double rewriteMin;

    public AlternatingBruteForceStepDiffer(Properties options) {
        super(options);
        rewriteMin = Double.parseDouble(options.getProperty(Options.DIFFER_REWRITE_MIN, DEFAULT_REWRITE_MIN));
    }

    @Override
    public Pair<Action[]> diffStep(Pair<List<String>> lines) {
        Pair<Action[]> actions = new Pair<>(new Action[lines.left.size()], new Action[lines.right.size()]);

        // Identify unchanged lines
        int lastRightUnchanged = 0; // Last mapped right position
        for (int i = 0; i < lines.left.size(); i++) {
            final String leftLine = lines.left.get(i);
            for (int j = 0; j < lines.right.size(); j++) {
                int upperIndex = lastRightUnchanged + j;
                if (upperIndex < actions.right.length  && actions.right[upperIndex] == null) {
                    final String upperRightLine = lines.right.get(upperIndex);
                    if (leftLine.equals(upperRightLine)) {
                        lastRightUnchanged = upperIndex;
                        Action action = Action.unchanged(i, upperIndex);
                        actions.left[i] = action;
                        actions.right[upperIndex] = action;
                        break;
                    }
                }

                int lowerIndex = lastRightUnchanged - j;
                if (lowerIndex > 0 && lowerIndex != upperIndex && actions.right[lowerIndex] == null) {
                    final String lowerRightLine = lines.right.get(lowerIndex);
                    if (leftLine.equals(lowerRightLine)) {
                        lastRightUnchanged = lowerIndex;
                        Action action = Action.unchanged(i, lowerIndex);
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

            final String leftLine = lines.left.get(i);
            for (int j = 0; j < lines.right.size(); j++) {
                int upperIndex = lastRightUpdated + j;
                if (upperIndex < actions.right.length  && actions.right[upperIndex] == null) {
                    final String upperRightLine = lines.right.get(upperIndex);
                    final double upperSim = Utils.rewriteSim(leftLine, upperRightLine);
                    if (upperSim >= rewriteMin) {
                        lastRightUpdated = upperIndex;
                        Action action = Action.updated(i, upperIndex);
                        actions.left[i] = action;
                        actions.right[upperIndex] = action;
                        break;
                    }
                }

                int lowerIndex = lastRightUpdated - j;
                if (lowerIndex > 0 && lowerIndex != upperIndex && actions.right[lowerIndex] == null) {
                    final String lowerRightLine = lines.right.get(lowerIndex);
                    final double lowerSim = Utils.rewriteSim(leftLine, lowerRightLine);
                    if (lowerSim >= rewriteMin) {
                        lastRightUpdated = lowerIndex;
                        Action action = Action.updated(i, lowerIndex);
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
