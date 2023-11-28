package org.cidiff.cidiff.differs;

import org.cidiff.cidiff.Action;
import org.cidiff.cidiff.Line;
import org.cidiff.cidiff.LogDiffer;
import org.cidiff.cidiff.Options;
import org.cidiff.cidiff.Pair;
import org.cidiff.cidiff.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

public class HashDiffer implements LogDiffer {

	private final double rewriteMin;

	public HashDiffer() {
		rewriteMin = Options.getInstance().getRewriteMin();
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

	private static Map<Bucket, Integer> buildTermsFrequencies(List<Line> leftLines, List<Line> rightLines) {
		final Map<Bucket, Integer> termMap = new HashMap<>();
		for (Line line : leftLines) {
			final String[] terms = Utils.split(line);
			for (int i = 0; i < terms.length; i++) {
				final Bucket b = new Bucket(i, terms[i]);
				termMap.put(b, termMap.getOrDefault(b, 0) + 1);
			}
		}

		for (Line line : rightLines) {
			final String[] terms = Utils.split(line);
			for (int i = 0; i < terms.length; i++) {
				final Bucket b = new Bucket(i, terms[i]);
				termMap.put(b, termMap.getOrDefault(b, 0) + 1);
			}
		}

		return termMap;
	}

	@Override
	public Pair<List<Action>> diff(List<Line> leftLines, List<Line> rightLines) {
		List<Action> leftActions = new ArrayList<>();
		for (int i = 0; i < leftLines.size(); i++) {
			leftActions.add(Action.EMPTY);
		}
		List<Action> rightActions = new ArrayList<>();
		for (int i = 0; i < rightLines.size(); i++) {
			rightActions.add(Action.EMPTY);
		}

		final Map<Integer, List<Line>> leftHashs = new HashMap<>();
		for (Line leftLine : leftLines) {
			int leftHash = leftLine.value().hashCode();
			leftHashs.putIfAbsent(leftHash, new ArrayList<>());
			leftHashs.get(leftHash).add(leftLine);
		}

		for (int j = 0; j < rightLines.size(); j++) {
			Line rightLine = rightLines.get(j);
			int rightHash = rightLine.value().hashCode();
			if (!leftHashs.containsKey(rightHash)) {
				continue;
			}

			Line bestLine = null;
			int bestI = -1;
			int minDist = Integer.MAX_VALUE;

			List<Line> leftHash = leftHashs.get(rightHash);
			for (int i = 0; i < leftHash.size(); i++) {
				Line leftLine = leftHash.get(i);
				if (!leftActions.get(i).isEmpty() || !leftLine.hasSameValue(rightLine))
					continue;

				final int dist = Math.abs(rightLine.index() - leftLine.index());
				if (dist < minDist) {
					minDist = dist;
					bestLine = leftLine;
					bestI = i;
				}
			}

			if (bestLine != null) {
				final Action action = Action.unchanged(bestLine, rightLine, 1);
				leftActions.set(bestI, action);
				rightActions.set(j, action);
			}
		}

		final Map<Bucket, Integer> termsFrequencies = buildTermsFrequencies(leftLines, rightLines);
		final Map<Bucket, List<Line>> leftTermsHashs = new HashMap<>();
		final Map<String, int[]> valueFingerprints = new HashMap<>();
		for (int i = 0; i < leftLines.size(); i++) {
			Line leftLine = leftLines.get(i);
			if (!leftActions.get(i).isEmpty()) {
				continue;
			}

			String[] terms = Utils.split(leftLine);
			Bucket bucket = buildBucket(terms, termsFrequencies);
			leftTermsHashs.putIfAbsent(bucket, new ArrayList<>());
			leftTermsHashs.get(bucket).add(leftLine);
			valueFingerprints.put(leftLine.value(), fingerprint(terms));
		}

		for (int j = 0; j < rightLines.size(); j++) {
			Line rightLine = rightLines.get(j);
			if (!rightActions.get(j).isEmpty()) {
				continue;
			}

			final String[] terms = Utils.split(rightLine);
			final Bucket bucket = buildBucket(terms, termsFrequencies);
			valueFingerprints.put(rightLine.value(), fingerprint(terms));

			if (!leftTermsHashs.containsKey(bucket))
				continue;

			Line bestLine = null;
			int bestI = -1;
			int minDist = Integer.MAX_VALUE;
			List<Line> get = leftTermsHashs.get(bucket);
			for (int i = 0; i < get.size(); i++) {
				Line leftLine = get.get(i);
				if (!leftActions.get(i).isEmpty())
					continue;

				double similarity = similarity(valueFingerprints.get(leftLine.value()), valueFingerprints.get(rightLine.value()));
				if (similarity < rewriteMin)
					continue;

				final int dist = Math.abs(rightLine.index() - leftLine.index());
				if (dist < minDist) {
					minDist = dist;
					bestLine = leftLine;
					bestI = j;
				}
			}

			if (bestLine != null) {
				final Action action = Action.updated(bestLine, rightLine, similarity(valueFingerprints.get(bestLine.value()), valueFingerprints.get(rightLine.value())));
				leftActions.set(bestI, action);
				rightActions.set(j, action);
			}
		}

		// Identify deleted lines
		IntStream.range(0, leftLines.size())
				.filter(i -> leftActions.get(i).isEmpty())
				.forEach(i -> leftActions.set(i, Action.deleted(leftLines.get(i))));

		// Identify added lines
		IntStream.range(0, rightLines.size())
				.filter(i -> rightActions.get(i).isEmpty())
				.forEach(i -> rightActions.set(i, Action.added(rightLines.get(i))));

		return new Pair<>(leftActions, rightActions);
	}

	private record Bucket(int pos, String term) {
	}
}
