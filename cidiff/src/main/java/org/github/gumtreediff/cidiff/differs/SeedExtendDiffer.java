package org.github.gumtreediff.cidiff.differs;

import org.github.gumtreediff.cidiff.*;
import org.github.gumtreediff.cidiff.differs.AlternatingBruteForceLogDiffer;

import java.util.*;

public class SeedExtendDiffer extends AbstractLogDiffer {
    private final static int PRIME = 31;
    private final static String DEFAULT_REWRITE_MIN = "0.5";
    private final static String DEFAULT_BLOCK_SIZE = "3";
    private final static String DEFAULT_WINDOW_SIZE = "30";

    private final int blockSize;
    private final int windowSize;
    private final double rewriteMin;

    public SeedExtendDiffer(Properties options) {
        super(options);
        blockSize = Integer.parseInt(options.getProperty(Options.DIFFER_SEED_BLOCK, DEFAULT_BLOCK_SIZE));
        windowSize = Integer.parseInt(options.getProperty(Options.DIFFER_SEED_WINDOW, DEFAULT_WINDOW_SIZE));
        rewriteMin = Double.parseDouble(options.getProperty(Options.DIFFER_REWRITE_MIN, DEFAULT_REWRITE_MIN));
    }

    @Override
    public Pair<Action[]> diff(Pair<List<String>> lines) {
        if (lines.left.size() < blockSize + 2 * windowSize
                || lines.right.size() < blockSize + 2 * windowSize)
            return new AlternatingBruteForceLogDiffer(options).diff(lines); // Fallback to brute force for small logs

        Pair<Action[]> actions = new Pair<>(new Action[lines.left.size()], new Action[lines.right.size()]);
        Pair<Map<Integer, List<Integer>>> hashes = new Pair<>(new HashMap<>(), new HashMap<>());
        fillHash(lines.left, hashes.left);
        fillHash(lines.right, hashes.right);

        // Identify unchanged and updated lines
        for (int hash : hashes.left.keySet()) {
            if (hashes.right.containsKey(hash)) {
                for (Pair<Integer> matches : mappings(hash, hashes, lines)) {
                    final int leftInit = matches.left;
                    final int rightInit = matches.right;

                    for (int i = 0; i < blockSize; i++) {
                        final var action = Action.unchanged(leftInit + i, rightInit + i);
                        actions.left[leftInit + i] = action;
                        actions.right[rightInit + i] = action;
                    }

                    // left extension
                    var step = 1;
                    while (true) {
                        if (leftInit - step < 0 || rightInit - step < 0)
                            break;
                        if (actions.left[leftInit - step] != null || actions.right[rightInit - step] != null)
                            break;

                        final String leftLine = lines.left.get(leftInit - step);
                        final String rightLine = lines.right.get(rightInit - step);
                        final var action = makeAction(actions, leftLine, rightLine,
                                leftInit - step, rightInit - step);
                        if (action == null)
                            break;

                        step++;
                    }

                    // right extension
                    step = 0;
                    while (true) {
                        if (leftInit + blockSize + step >= lines.left.size()
                                ||  rightInit + blockSize + step >= lines.right.size())
                            break;
                        if (actions.left[leftInit + blockSize + step] != null
                                || actions.right[rightInit + blockSize + step] != null)
                            break;

                        final String leftLine = lines.left.get(leftInit + blockSize + step);
                        final String rightLine = lines.right.get(rightInit + blockSize + step);
                        final var action = makeAction(actions, leftLine, rightLine,
                                leftInit + blockSize + step, rightInit + blockSize + step);
                        if (action == null)
                            break;

                        step++;
                    }

                    final var leftMinBound = Math.max(0, leftInit - windowSize);
                    final var leftMaxBound = Math.min(leftInit + blockSize + windowSize, lines.left.size());
                    for (int i = leftMinBound; i < leftMaxBound; i++) {
                        if (actions.left[i] != null || (i >= leftInit && i < leftInit + blockSize))
                            continue;

                        final String leftLine = lines.left.get(i);

                        final var rightMinBound = Math.max(0, rightInit - windowSize);
                        final var rightMaxBound = Math.min(rightInit + blockSize + windowSize, lines.right.size());
                        for (int j = rightMinBound; j < rightMaxBound; j++) {
                            if (actions.right[j] != null || (j >= rightInit && i < rightInit + blockSize))
                                continue;

                            final String rightLine = lines.right.get(j);
                            if (makeAction(actions, leftLine, rightLine, i, j) != null)
                                break;
                        }
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

    private Action makeAction(final Pair<Action[]> actions, final String leftLine, final String rightLine,
                                     final int leftLocation, final int rightLocation) {
        if (leftLine.equals(rightLine)) {
            final var action = Action.unchanged(leftLocation, rightLocation);
            actions.left[leftLocation] = action;
            actions.right[rightLocation] = action;
            return action;
        }
        else if (Utils.rewriteSim(leftLine, rightLine) >= rewriteMin) {
            final var action = Action.updated(leftLocation, rightLocation);
            actions.left[leftLocation] = action;
            actions.right[rightLocation] = action;
            return action;
        }
        return null;
    }

    private List<Pair<Integer>> mappings(int hash, Pair<Map<Integer, List<Integer>>> hashes, Pair<List<String>> lines) {
        List<Pair<Integer>> results = new ArrayList<>();
        Set<Integer> rightMatched = new HashSet<>();
        List<Integer> leftMatches = hashes.left.get(hash);
        List<Integer> rightMatches = hashes.right.get(hash);

        for (int leftMatch : leftMatches) {
            int bestDist = Integer.MAX_VALUE;
            int bestMatch = -1;
            for (int rightMatch : rightMatches) {
                if (rightMatched.contains(rightMatch))
                    continue;

                boolean ensureEquals = true;
                for (int i = 0; i < blockSize; i++) {
                    if (lines.left.get(leftMatch + i).length() != lines.right.get(rightMatch + i).length()) {
                        ensureEquals = false;
                        break;
                    }
                }

                if (ensureEquals) {
                    var dist = Math.abs(leftMatch - rightMatch);
                    if (dist < bestDist) {
                        bestDist = dist;
                        bestMatch = rightMatch;
                    }
                }
            }

            if (bestMatch != -1) {
                results.add(new Pair<>(leftMatch, bestMatch));
                rightMatched.add(bestMatch);
            }
        }

        return results;
    }

    private void fillHash(final List<String> lines, final Map<Integer, List<Integer>> hashes) {
        if (lines.size() < blockSize)
            return;

        int hash = 0;
        for (int i = 0; i < blockSize; i++)
            hash += lines.get(i).hashCode() * Utils.fastExponentiation(PRIME, blockSize - i - 1);

        hashes.putIfAbsent(hash, new ArrayList<>());
        hashes.get(hash).add(0);

        for (int i = blockSize; i < lines.size(); i++) {
            hash = PRIME * hash + lines.get(i).hashCode()
                    - lines.get(i - blockSize).hashCode() * Utils.fastExponentiation(PRIME, blockSize);
            hashes.putIfAbsent(hash, new ArrayList<>());
            hashes.get(hash).add(i - blockSize + 1);
        }
    }
}
