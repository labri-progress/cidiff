package org.github.gumtreediff.cidiff.differs;

import java.util.*;

import org.github.gumtreediff.cidiff.Action;
import org.github.gumtreediff.cidiff.LogLine;
import org.github.gumtreediff.cidiff.Pair;

public final class LcsLogDiffer extends AbstractLogDiffer {
    public LcsLogDiffer(Properties options) {
        super(options);
    }

    @Override
    public Pair<Map<LogLine, Action>> diff(Pair<List<LogLine>> lines) {
        final Pair<Map<LogLine, Action>> actions = new Pair<>(
                new HashMap<>(), new HashMap<>()
        );

        // Identify unchanged lines
        final List<int[]> lcs = longestCommonSubsequence(lines.left, lines.right);
        for (int[] match : lcs) {
            final LogLine leftLine = lines.left.get(match[0]);
            final LogLine rightLine = lines.right.get(match[1]);
            final Action action = Action.unchanged(leftLine, rightLine);
            actions.left.put(leftLine, action);
            actions.right.put(rightLine, action);
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

    /**
     * Returns the longest common subsequence between two strings.
     *
     * @return a list of size 2 int arrays that corresponds
     * to match of index in left sequence to index in right sequence.
     */
    static List<int[]> longestCommonSubsequence(List<LogLine> left, List<LogLine> right) {
        final int[][] lengths = new int[left.size() + 1][right.size() + 1];
        for (int i = 0; i < left.size(); i++)
            for (int j = 0; j < right.size(); j++)
                if (left.get(i).hasSameValue(right.get(j)))
                    lengths[i + 1][j + 1] = lengths[i][j] + 1;
                else
                    lengths[i + 1][j + 1] = Math.max(lengths[i + 1][j], lengths[i][j + 1]);

        return extractIndexes(lengths, left.size(), right.size());
    }

    static List<int[]> extractIndexes(int[][] lengths, int leftSize, int rightSize) {
        final List<int[]> indexes = new ArrayList<>();

        for (int x = leftSize, y = rightSize; x != 0 && y != 0;) {
            if (lengths[x][y] == lengths[x - 1][y]) x--;
            else if (lengths[x][y] == lengths[x][y - 1]) y--;
            else {
                indexes.add(new int[]{x - 1, y - 1});
                x--;
                y--;
            }
        }
        Collections.reverse(indexes);
        return indexes;
    }
}
