package org.github.gumtreediff.cidiff;

import java.util.*;

public class SeedExtendDiffer extends AbstractStepDiffer {
    private final static int PRIME = 31;
    private final static String DEFAULT_REWRITE_MIN = "0.5";
    private final static String DEFAULT_BLOCK_SIZE = "5";
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
    public Pair<Action[]> diffStep(Pair<List<String>> lines) {
        Pair<Action[]> actions = new Pair<>(new Action[lines.left.size()], new Action[lines.right.size()]);
        Pair<Map<Integer, List<Integer>>> hashes = new Pair<>(new HashMap<>(), new HashMap<>());
        fillHash(lines.left, hashes.left);
        fillHash(lines.right, hashes.right);

        // Identify unchanged and updated lines
        for (int hash : hashes.left.keySet()) {
            if (hashes.right.containsKey(hash) && hashes.left.get(hash).size() == 1
                    && hashes.right.get(hash).size() == 1) {
                final int leftInit = hashes.left.get(hash).get(0);
                final int rightInit = hashes.right.get(hash).get(0);
                for (int i = 0; i < blockSize; i++) {
                    final var action = Action.unchanged(leftInit + i, rightInit + i);
                    actions.left[leftInit + i] = action;
                    actions.right[rightInit + i] = action;
                }

                for (int i = Math.max(0, leftInit - windowSize)
                     ; i < Math.min(leftInit + blockSize + windowSize, lines.left.size()); i++) {
                    if (actions.left[i] != null || (i >= leftInit && i < leftInit + blockSize))
                        continue;

                    final String leftLine = lines.left.get(i);
                    for (int j = Math.max(0, rightInit - windowSize)
                         ; j < Math.min(rightInit + blockSize + windowSize, lines.right.size()); j++) {
                        if (actions.right[j] != null || (j >= rightInit && i < rightInit + blockSize))
                            continue;

                        final String rightLine = lines.right.get(j);
                        if (leftLine.equals(rightLine)) {
                            final var action = Action.unchanged(i, j);
                            actions.left[i] = action;
                            actions.right[j] = action;
                        }
                        else {
                            final double sim = Utils.rewriteSim(leftLine, rightLine);
                            if (sim >= rewriteMin) {
                                final var action = Action.updated(i, j);
                                actions.left[i] = action;
                                actions.right[j] = action;
                                break;
                            }
                        }
                    }
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

    private void fillHash(final List<String> lines, final Map<Integer, List<Integer>> hashes) {
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
