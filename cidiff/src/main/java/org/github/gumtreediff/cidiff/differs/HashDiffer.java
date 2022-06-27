package org.github.gumtreediff.cidiff.differs;

import java.util.*;

import org.github.gumtreediff.cidiff.*;

public class HashDiffer extends AbstractLogDiffer {
    private static final String DEFAULT_REWRITE_MIN = "0.5";

    private final double rewriteMin;

    public HashDiffer(Properties options) {
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

        final Map<Integer, List<LogLine>> leftHashs = new HashMap<>();
        final Map<Integer, List<LogLine>> leftTermsNumbers = new HashMap<>();
        for (LogLine leftLine : lines.left) {
            leftHashs.putIfAbsent(leftLine.hashCode(), new ArrayList<>());
            leftHashs.get(leftLine.hashCode()).add(leftLine);
            final int leftTermNumber = Utils.termsHash(leftLine).length;
            leftTermsNumbers.putIfAbsent(leftTermNumber, new ArrayList<>());
            leftTermsNumbers.get(leftTermNumber).add(leftLine);
        }

        for (LogLine rightLine : lines.right) {
            final int rightHash = rightLine.hashCode();
            if (leftHashs.containsKey(rightHash)) {
                LogLine bestLine = null;
                int minDist = Integer.MAX_VALUE;
                for (LogLine leftLine : leftHashs.get(rightHash)) {
                    if (!leftLine.value.equals(rightLine.value))
                        continue;

                    final int dist = Math.abs(rightLine.lineNumber - leftLine.lineNumber);
                    if (dist < minDist) {
                        minDist = dist;
                        bestLine = leftLine;
                    }
                }

                if (bestLine != null) {
                    final Action action = Action.unchanged(bestLine.relativeIndex, rightLine.relativeIndex);
                    actions.left[bestLine.relativeIndex] = action;
                    actions.right[rightLine.relativeIndex] = action;
                    continue;
                }
            }

            final int rightLineTermsNumber = Utils.termsHash(rightLine).length;
            if (leftTermsNumbers.containsKey(rightLineTermsNumber)) {
                LogLine bestLine = null;
                int minDist = Integer.MAX_VALUE;
                for (LogLine leftLine : leftTermsNumbers.get(rightLineTermsNumber)) {
                    final double sim = Utils.rewriteSim(leftLine.value, rightLine.value);
                    if (sim < rewriteMin)
                        continue;

                    final int dist = Math.abs(rightLine.lineNumber - leftLine.lineNumber);
                    if (dist < minDist) {
                        minDist = dist;
                        bestLine = leftLine;
                    }
                }

                if (bestLine != null) {
                    final Action action = Action.updated(bestLine.relativeIndex, rightLine.relativeIndex);
                    actions.left[bestLine.relativeIndex] = action;
                    actions.right[rightLine.relativeIndex] = action;
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
