package org.github.gumtreediff.cidiff.differs;

import org.github.gumtreediff.cidiff.AbstractLogDiffer;
import org.github.gumtreediff.cidiff.Action;
import org.github.gumtreediff.cidiff.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

public final class LcsLogDiffer extends AbstractLogDiffer {
    public LcsLogDiffer(Properties options) {
        super(options);
    }

    @Override
    public Pair<Action[]> diff(Pair<List<String>> lines) {
        Pair<Action[]> actions = new Pair<>(new Action[lines.left.size()], new Action[lines.right.size()]);

        // Identify unchanged lines
        List<int[]> lcs = longestCommonSubsequence(lines.left, lines.right);
        for (int[] match : lcs) {
            Action action = Action.unchanged(match[0], match[1]);
            actions.left[match[0]] = action;
            actions.right[match[1]] = action;
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

    /**
     * Returns the longest common subsequence between two strings.
     *
     * @return a list of size 2 int arrays that corresponds
     * to match of index in left sequence to index in right sequence.
     */
    static List<int[]> longestCommonSubsequence(List<String> left, List<String> right) {
        int[][] lengths = new int[left.size() + 1][right.size() + 1];
        for (int i = 0; i < left.size(); i++)
            for (int j = 0; j < right.size(); j++)
                if (left.get(i).equals(right.get(j)))
                    lengths[i + 1][j + 1] = lengths[i][j] + 1;
                else
                    lengths[i + 1][j + 1] = Math.max(lengths[i + 1][j], lengths[i][j + 1]);

        return extractIndexes(lengths, left.size(), right.size());
    }

    static List<int[]> extractIndexes(int[][] lengths, int leftSize, int rightSize) {
        List<int[]> indexes = new ArrayList<>();

        for (int x = leftSize, y = rightSize; x != 0 && y != 0; ) {
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
