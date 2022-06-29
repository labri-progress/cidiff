package org.github.gumtreediff.cidiff.differs;

import java.util.*;

import org.github.gumtreediff.cidiff.*;

public class SeedExtendDiffer extends AbstractLogDiffer {
    private static final int PRIME = 31;
    private static final String DEFAULT_REWRITE_MIN = "0.5";
    private static final String DEFAULT_BLOCK_SIZE = "3";
    private static final String DEFAULT_WINDOW_SIZE = "30";

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
    public Pair<Map<LogLine, Action>> diff(Pair<List<LogLine>> lines) {
        if (lines.left.size() < blockSize + 2 * windowSize
                || lines.right.size() < blockSize + 2 * windowSize)
            return new BruteForceLogDiffer(options).diff(lines); // Fallback to brute force for small logs

        final Pair<Map<LogLine, Action>> actions = new Pair<>(
                new HashMap<>(), new HashMap<>()
        );

        final Pair<Map<Integer, List<Integer>>> hashes = new Pair<>(new HashMap<>(), new HashMap<>());
        fillHash(lines.left, hashes.left);
        fillHash(lines.right, hashes.right);

        // Identify unchanged and updated lines
        for (int hash : hashes.left.keySet()) {
            if (hashes.right.containsKey(hash)) {
                for (Pair<Integer> matches : mappings(hash, hashes, lines)) {
                    final int leftInit = matches.left;
                    final int rightInit = matches.right;

                    for (int i = 0; i < blockSize; i++) {
                        final LogLine leftLine = lines.left.get(leftInit + 1);
                        final LogLine rightLine = lines.right.get(rightInit + 1);
                        final var action = Action.unchanged(leftLine, rightLine);
                        actions.left.put(leftLine, action);
                        actions.right.put(rightLine, action);
                    }

                    // left extension
                    var step = 1;
                    while (true) {
                        if (leftInit - step < 0 || rightInit - step < 0)
                            break;

                        final LogLine leftLine = lines.left.get(leftInit - step);
                        final LogLine rightLine = lines.right.get(rightInit - step);
                        if (actions.left.containsKey(leftLine) || actions.right.containsKey(rightLine))
                            break;

                        final var action = makeAction(actions, leftLine, rightLine);
                        if (action == null)
                            break;

                        step++;
                    }

                    // right extension
                    step = 0;
                    while (true) {
                        if (leftInit + blockSize + step >= lines.left.size()
                                || rightInit + blockSize + step >= lines.right.size())
                            break;

                        final LogLine leftLine = lines.left.get(leftInit + blockSize + step);
                        final LogLine rightLine = lines.right.get(rightInit + blockSize + step);

                        if (actions.left.containsKey(leftLine) || actions.right.containsKey(rightLine))
                            break;

                        final var action = makeAction(actions, leftLine, rightLine);
                        if (action == null)
                            break;

                        step++;
                    }

                    final var leftMinBound = Math.max(0, leftInit - windowSize);
                    final var leftMaxBound = Math.min(leftInit + blockSize + windowSize, lines.left.size());
                    for (int i = leftMinBound; i < leftMaxBound; i++) {
                        final LogLine leftLine = lines.left.get(i);
                        if (actions.left.containsKey(leftLine) || i >= leftInit && i < leftInit + blockSize)
                            continue;

                        final var rightMinBound = Math.max(0, rightInit - windowSize);
                        final var rightMaxBound = Math.min(rightInit + blockSize + windowSize, lines.right.size());
                        for (int j = rightMinBound; j < rightMaxBound; j++) {
                            final LogLine rightLine = lines.right.get(j);
                            if (actions.right.containsKey(rightLine) || j >= rightInit && i < rightInit + blockSize)
                                continue;

                            if (makeAction(actions, leftLine, rightLine) != null)
                                break;
                        }
                    }
                }
            }
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

    private Action makeAction(Pair<Map<LogLine, Action>> actions, LogLine leftLine, LogLine rightLine) {
        if (leftLine.hasSameValue(rightLine)) {
            final var action = Action.unchanged(leftLine, rightLine);
            actions.left.put(leftLine, action);
            actions.right.put(rightLine, action);
            return action;
        }
        else if (Utils.rewriteSim(leftLine, rightLine) >= rewriteMin) {
            final var action = Action.updated(leftLine, rightLine);
            actions.left.put(leftLine, action);
            actions.right.put(rightLine, action);
            return action;
        }
        return null;
    }

    private List<Pair<Integer>> mappings(int hash, Pair<Map<Integer,
            List<Integer>>> hashes, Pair<List<LogLine>> lines) {
        final List<Pair<Integer>> results = new ArrayList<>();
        final Set<Integer> rightMatched = new HashSet<>();
        final List<Integer> leftMatches = hashes.left.get(hash);
        final List<Integer> rightMatches = hashes.right.get(hash);

        for (int leftMatch : leftMatches) {
            int bestDist = Integer.MAX_VALUE;
            int bestMatch = -1;
            for (int rightMatch : rightMatches) {
                if (rightMatched.contains(rightMatch))
                    continue;

                boolean ensureEquals = true;
                for (int i = 0; i < blockSize; i++) {
                    if (lines.left.get(leftMatch + i).value.length()
                            != lines.right.get(rightMatch + i).value.length()) {
                        ensureEquals = false;
                        break;
                    }
                }

                if (ensureEquals) {
                    final var dist = Math.abs(leftMatch - rightMatch);
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

    private void fillHash(final List<LogLine> lines, final Map<Integer, List<Integer>> hashes) {
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
