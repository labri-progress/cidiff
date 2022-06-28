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
        for (LogLine leftLine : lines.left) {
            leftHashs.putIfAbsent(leftLine.hashCode(), new ArrayList<>());
            leftHashs.get(leftLine.hashCode()).add(leftLine);
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
        }

        final Map<Bucket, Integer> termsFrequencies = buildTermsFrequencies(lines);
        final Map<Bucket, List<LogLine>> leftTermsHashs = new HashMap<>();
        final Map<LogLine, int[]> logLineFingerprints = new HashMap<>();
        for (LogLine leftLine : lines.left) {
            if (actions.left[leftLine.relativeIndex] != null)
                continue;

            final String[] terms = Utils.split(leftLine);
            final Bucket bucket = buildBucket(terms, termsFrequencies);
            leftTermsHashs.putIfAbsent(bucket, new ArrayList<>());
            leftTermsHashs.get(bucket).add(leftLine);
            logLineFingerprints.put(leftLine, fingerprint(terms));
        }

        for (LogLine rightLine : lines.right) {
            if (actions.right[rightLine.relativeIndex] != null)
                continue;

            final String[] terms = Utils.split(rightLine);
            final Bucket bucket = buildBucket(terms, termsFrequencies);
            logLineFingerprints.put(rightLine, fingerprint(terms));

            if (!leftTermsHashs.containsKey(bucket))
                continue;

            LogLine bestLine = null;
            int minDist = Integer.MAX_VALUE;
            for (LogLine leftLine : leftTermsHashs.get(bucket)) {
                final double similarity = similarity(logLineFingerprints.get(leftLine),
                        logLineFingerprints.get(rightLine));
                if (similarity < rewriteMin)
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
                continue;
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
