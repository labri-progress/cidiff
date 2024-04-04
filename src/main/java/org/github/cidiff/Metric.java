package org.github.cidiff;

import org.simmetrics.metrics.StringMetrics;

import java.util.function.BiFunction;

public enum Metric {

	LOGSIM(Metric::logsim),
	EQUALITY(Metric::equality),
	JARO_WINKLER((line, line2) -> (double) StringMetrics.jaroWinkler().compare(line, line2)),
	LEVENSHTEIN((line, line2) -> (double) StringMetrics.levenshtein().compare(line, line2)),
	COSINE((line, line2) -> (double) StringMetrics.cosineSimilarity().compare(line, line2)),
	MONGE_ELKMAN((line, line2) -> (double) StringMetrics.mongeElkan().compare(line, line2)),
	SMITH_WATERMAN((line, line2) -> (double) StringMetrics.smithWaterman().compare(line, line2)),
	JACCARD((line, line2) -> (double) StringMetrics.generalizedJaccard().compare(line, line2))
	;

	private final BiFunction<String, String, Double> function;

	Metric(BiFunction<String, String, Double> function) {
		this.function = function;
	}

	public double sim(String left, String right) {
		return this.function.apply(left, right);
	}

	public static double logsim(String leftLine, String rightLine) {
		final String[] leftTokens = Utils.split(leftLine.trim());
		final String[] rightTokens = Utils.split(rightLine.trim());

		// tokens amount different : we assume the lines have different log templates
		// lowest similarity if the number of tokens is distinct
		if (leftTokens.length != rightTokens.length) {
			return 0;
		}

		boolean ok = false;
		double count = 0;
		for (int i = 0; i < leftTokens.length; i++) {
			if (leftTokens[i].equals(rightTokens[i])) {
				count += 1;
				ok = true;
			} else if (leftTokens[i].length() == rightTokens[i].length()) {
				count += 0.5;
			} else if (Utils.lcsLength(leftTokens[i].toCharArray(), rightTokens[i].toCharArray())
					>= 2 * Math.max(leftTokens[i].length(), rightTokens[i].length()) / 3) {
				count += 0.5;
			}
		}

		if (!ok) {
			return 0;
		}

		return count / leftTokens.length;
	}

	private static double equality(String leftLine, String rightLine) {
		return leftLine.equals(rightLine) ? 1.0 : 0.0;
	}

}
