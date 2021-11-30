package org.github.gumtreediff.cidiff;

import java.util.List;

public class StepDiffer {
    final List<String> leftLines;
    final List<String> rightLines;
    final Action[] leftActions;
    final Action[] rightActions;

    final static String TOKEN_SEPARATORS = "\\s+|=|:";
    final static double MIN_REWRITE_SIM = 0.5;

    public StepDiffer(List<String> leftLines, List<String> rightLines) {
        this.leftLines = leftLines;
        this.rightLines = rightLines;
        this.leftActions = new Action[leftLines.size()];
        this.rightActions = new Action[rightLines.size()];
        diff();
    }

    public Action[] getLeftActions() {
        return this.leftActions;
    }

    public Action[] getRightActions() {
        return this.rightActions;
    }
    
    public void diff() {
        // Identify unchanged lines
        for (int i = 0; i < leftLines.size(); i++) {
            final String leftLine = leftLines.get(i);
            for (int j = 0; j < rightLines.size(); j++) {
                final String rightLine = rightLines.get(j);
                if (leftLine.equals(rightLine) && rightActions[j] == null) {
                    Action action = Action.unchanged(i, j);
                    leftActions[i] = action;
                    rightActions[j] = action;
                    break;
                }
            }
        }

        // Identify updated lines
        for (int i = 0; i < leftLines.size(); i++) {
            if (leftActions[i] != null)
                continue;

            final String leftLine = leftLines.get(i);
            for (int j = 0; j < rightLines.size(); j++) {
                if (rightActions[j] != null)
                    continue;

                final String rightLine = rightLines.get(j);
                final double sim = rewriteSim(leftLine, rightLine);
                if (sim >= MIN_REWRITE_SIM) {
                    Action action = Action.updated(i, j);
                    leftActions[i] = action;
                    rightActions[j] = action;
                    break;
                }
            }
        }

        // Identify added lines
        for (int i = 0; i < leftLines.size(); i++)
            if (leftActions[i] == null)
                leftActions[i] = Action.deleted(i);

        // Identify added lines
        for (int i = 0; i < rightLines.size(); i++)
            if (rightActions[i] == null)
                rightActions[i] = Action.added(i);
    }

    static double rewriteSim(String leftLine, String rightLine) {
        final String[] leftTokens = leftLine.split(TOKEN_SEPARATORS);
        final String[] rightTokens = rightLine.split(TOKEN_SEPARATORS);

        // lowest similarity if the number of tokens is distinct
        if (leftTokens.length != rightTokens.length)
            return 0.0;

        // number of distinct tokens
        int dist = 0;
        for (int i = 0; i < leftTokens.length; i++)
            if (!leftTokens[i].equals(rightTokens[i]))
                dist++;

        return (double) (leftTokens.length - dist) / (double) leftTokens.length;
    }
}
