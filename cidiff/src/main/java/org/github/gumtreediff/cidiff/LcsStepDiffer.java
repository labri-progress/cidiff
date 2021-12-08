package org.github.gumtreediff.cidiff;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LcsStepDiffer implements StepDiffer {
    @Override
    public Pair<Action[]> diffStep(Pair<List<String>> lines) {
        Pair<Action[]> actions = new Pair<>(new Action[lines.left.size()], new Action[lines.right.size()]);

        // Identify unchanged lines
        List<int[]> lcs = longestCommonSubsequence(lines.left, lines.right);
        for (int[] match : lcs) {
            Action action = Action.unchanged(match[0], match[1]);
            actions.left[match[0]] = action;
            actions.right[match[1]] = action;
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

    /**
     * Returns the hunks of the longest common subsequence between s1 and s2.
     *
     * @return the hunks as a list of int arrays of size 4 with start index and end index of left sequence
     * and corresponding start index and end index in right sequence.
     */
    static List<int[]> hunks(List<String> leftLines, List<String> rightLines) {
        List<int[]> lcs = longestCommonSubsequence(leftLines, rightLines);
        List<int[]> hunks = new ArrayList<int[]>();
        int inf0 = -1;
        int inf1 = -1;
        int last0 = -1;
        int last1 = -1;
        for (int i = 0; i < lcs.size(); i++) {
            int[] match = lcs.get(i);
            if (inf0 == -1 || inf1 == -1) {
                inf0 = match[0];
                inf1 = match[1];
            } else if (last0 + 1 != match[0] || last1 + 1 != match[1]) {
                hunks.add(new int[]{inf0, last0 + 1, inf1, last1 + 1});
                inf0 = match[0];
                inf1 = match[1];
            } else if (i == lcs.size() - 1) {
                hunks.add(new int[]{inf0, match[0] + 1, inf1, match[1] + 1});
                break;
            }
            last0 = match[0];
            last1 = match[1];
        }
        return hunks;
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
