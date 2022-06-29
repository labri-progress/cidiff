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
    public Pair<Map<LogLine, Action>> diff(Pair<List<LogLine>> lines) {
        final Pair<Map<LogLine, Action>> actions = new Pair<>(
                new HashMap<>(), new HashMap<>()
        );

        final Map<Integer, List<LogLine>> leftHashs = new HashMap<>();
        for (LogLine leftLine : lines.left) {
            final int leftHash = leftLine.value.hashCode();
            leftHashs.putIfAbsent(leftHash, new ArrayList<>());
            leftHashs.get(leftHash).add(leftLine);
        }

        for (LogLine rightLine : lines.right) {
            final int rightHash = rightLine.value.hashCode();
            if (!leftHashs.containsKey(rightHash))
                continue;

            LogLine bestLine = null;
            int minDist = Integer.MAX_VALUE;

            for (LogLine leftLine : leftHashs.get(rightHash)) {
                if (actions.left.containsKey(leftLine) || !leftLine.hasSameValue(rightLine))
                    continue;

                final int dist = Math.abs(rightLine.lineNumber - leftLine.lineNumber);
                if (dist < minDist) {
                    minDist = dist;
                    bestLine = leftLine;
                }
            }

            if (bestLine != null) {
                final Action action = Action.unchanged(bestLine, rightLine);
                actions.left.put(bestLine, action);
                actions.right.put(rightLine, action);
            }
        }

        final Map<Bucket, Integer> termsFrequencies = buildTermsFrequencies(lines);
        final Map<Bucket, List<LogLine>> leftTermsHashs = new HashMap<>();
        final Map<String, int[]> valueFingerprints = new HashMap<>();
        for (LogLine leftLine : lines.left) {
            if (actions.left.containsKey(leftLine))
                continue;

            final String[] terms = Utils.split(leftLine);
            final Bucket bucket = buildBucket(terms, termsFrequencies);
            leftTermsHashs.putIfAbsent(bucket, new ArrayList<>());
            leftTermsHashs.get(bucket).add(leftLine);
            valueFingerprints.put(leftLine.value, fingerprint(terms));
        }

        for (LogLine rightLine : lines.right) {
            if (actions.right.containsKey(rightLine))
                continue;

            final String[] terms = Utils.split(rightLine);
            final Bucket bucket = buildBucket(terms, termsFrequencies);
            valueFingerprints.put(rightLine.value, fingerprint(terms));

            if (!leftTermsHashs.containsKey(bucket))
                continue;

            LogLine bestLine = null;
            int minDist = Integer.MAX_VALUE;
            for (LogLine leftLine : leftTermsHashs.get(bucket)) {
                if (actions.left.containsKey(leftLine))
                    continue;

                final double similarity = similarity(valueFingerprints.get(leftLine.value),
                        valueFingerprints.get(rightLine.value));
                if (similarity < rewriteMin)
                    continue;

                final int dist = Math.abs(rightLine.lineNumber - leftLine.lineNumber);
                if (dist < minDist) {
                    minDist = dist;
                    bestLine = leftLine;
                }
            }

            if (bestLine != null) {
                final Action action = Action.updated(bestLine, rightLine);
                actions.left.put(bestLine, action);
                actions.right.put(rightLine, action);
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

    private static double similarity(int[] leftFingerprint, int[] rightFingerprint) {
        if (leftFingerprint.length != rightFingerprint.length)
            return 0.0;
        else {
            int same = 0;
            for (int i = 0; i < leftFingerprint.length; i++)
                if (leftFingerprint[i] == rightFingerprint[i])
                    same++;

            return (double) same / (double) leftFingerprint.length;
        }
    }

    private static int[] fingerprint(String[] terms) {
        final int[] fingerprint = new int[terms.length];
        for (int i = 0; i < terms.length; i++)
            fingerprint[i] = terms[i].length();
        return fingerprint;
    }

    private static Bucket buildBucket(String[] terms, Map<Bucket, Integer> termsFrequencies) {
        Bucket bestBucket = null;
        int bestFrequency = -1;
        for (int i = 0; i < terms.length; i++) {
            final Bucket currentBucket = new Bucket(i, terms[i]);
            if (termsFrequencies.get(currentBucket) > bestFrequency) {
                bestFrequency = termsFrequencies.get(currentBucket);
                bestBucket = currentBucket;
            }
        }

        return bestBucket;
    }

    private static Map<Bucket, Integer> buildTermsFrequencies(Pair<List<LogLine>> logs) {
        final Map<Bucket, Integer> termMap = new HashMap<>();
        for (LogLine line : logs.left) {
            final String[] terms = Utils.split(line);
            for (int i = 0; i < terms.length; i++) {
                final Bucket b = new Bucket(i, terms[i]);
                termMap.put(b, termMap.getOrDefault(b, 0) + 1);
            }
        }

        for (LogLine line : logs.right) {
            final String[] terms = Utils.split(line);
            for (int i = 0; i < terms.length; i++) {
                final Bucket b = new Bucket(i, terms[i]);
                termMap.put(b, termMap.getOrDefault(b, 0) + 1);
            }
        }

        return termMap;
    }

    private record Bucket(int pos, String term) {
    }
}
