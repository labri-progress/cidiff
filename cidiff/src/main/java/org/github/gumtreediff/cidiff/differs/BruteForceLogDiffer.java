package org.github.gumtreediff.cidiff.differs;

import java.util.List;
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
    public Pair<Action[]> diff(Pair<List<String>> lines) {
        final Pair<Action[]> actions = new Pair<>(
                new Action[lines.left.size()], new Action[lines.right.size()]
        );

        // Identify unchanged lines
        for (int i = 0; i < lines.left.size(); i++) {
            final String leftLine = lines.left.get(i);
            for (int j = 0; j < lines.right.size(); j++) {
                if (actions.right[j] != null)
                    continue;

                final String rightLine = lines.right.get(j);
                if (leftLine.equals(rightLine)) {
                    final Action action = Action.unchanged(i, j);
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
                final double sim = Utils.rewriteSim(leftLine, rightLine);
                if (sim >= rewriteMin) {
                    final Action action = Action.updated(i, j);
                    actions.left[i] = action;
                    actions.right[j] = action;
                    break;
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
