package org.github.gumtreediff.cidiff;

import java.util.List;

public class BruteForceStepDiffer implements StepDiffer {
    Pair<List<String>> lines;
    Pair<Action[]> actions;

    final static String TOKEN_SEPARATORS = "\\s+";
    final static double MIN_REWRITE_SIM = 0.5;

    @Override
    public Pair<Action[]> diffStep(Pair<List<String>> lines) {
        this.lines = lines;
        this.actions = new Pair<>(new Action[lines.left.size()], new Action[lines.right.size()]);

        // Identify unchanged lines
        for (int i = 0; i < lines.left.size(); i++) {
            final String leftLine = lines.left.get(i);
            for (int j = 0; j < lines.right.size(); j++) {
                if (actions.right[j] != null)
                    continue;

                final String rightLine = lines.right.get(j);
                if (leftLine.equals(rightLine)) {
                    Action action = Action.unchanged(i, j);
                    actions.left[i] = action;
                    actions.right[j] = action;
                    break;
                }
            }
        }

        // Identify updated lines
        for (int i = 0; i < lines.left.size(); i++) {
            if (actions.left[i] != null)
                continue;

            final String leftLine = lines.left.get(i);
            for (int j = 0; j < lines.right.size(); j++) {
                if (actions.right[j] != null)
                    continue;

                final String rightLine = lines.right.get(j);
                final double sim = rewriteSim(leftLine, rightLine);
                if (sim >= MIN_REWRITE_SIM) {
                    Action action = Action.updated(i, j);
                    actions.left[i] = action;
                    actions.right[j] = action;
                    break;
                }
            }
        }

        // Identify added lines
        for (int i = 0; i < lines.left.size(); i++)
            if (actions.left[i] == null)
                actions.left[i] = Action.deleted(i);

        // Identify added lines
        for (int i = 0; i < lines.right.size(); i++)
            if (actions.right[i] == null)
                actions.right[i] = Action.added(i);

        return actions;
    }

    static double rewriteSim(String leftLine, String rightLine) {
        final String[] leftTokens = leftLine.split(TOKEN_SEPARATORS);
        final String[] rightTokens = rightLine.split(TOKEN_SEPARATORS);

        // lowest similarity if the number of tokens is distinct
        if (leftTokens.length != rightTokens.length)
            return 0.0;

        // number of distinct tokens
        int dist = 0;
        for (int i = 0; i < leftTokens.length; i++) {
            if (leftTokens[i].length() != rightTokens[i].length())
                dist++;
        }

        return (double) (leftTokens.length - dist) / (double) leftTokens.length;
    }
}
