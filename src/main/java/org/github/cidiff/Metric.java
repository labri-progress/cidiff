package org.github.cidiff;

import org.simmetrics.metrics.StringMetrics;

import java.util.function.BiFunction;

public enum Metric {

	LOGSIM(Metric::logsim),
	EQUALITY(Metric::equality),
	JARO_WINKLER((line, line2) -> (double) StringMetrics.jaroWinkler().compare(line.value(), line2.value())),
	LEVENSHTEIN((line, line2) -> (double) StringMetrics.levenshtein().compare(line.value(), line2.value())),
	COSINE((line, line2) -> (double) StringMetrics.cosineSimilarity().compare(line.value(), line2.value())),
	MONGE_ELKMAN((line, line2) -> (double) StringMetrics.mongeElkan().compare(line.value(), line2.value())),
	SMITH_WATERMAN((line, line2) -> (double) StringMetrics.smithWaterman().compare(line.value(), line2.value())),
	JACCARD((line, line2) -> (double) StringMetrics.generalizedJaccard().compare(line.value(), line2.value()))
	;

	private final BiFunction<Line, Line, Double> function;

	Metric(BiFunction<Line, Line, Double> function) {
		this.function = function;
	}
	public double sim(Line left, Line right) {
		return this.function.apply(left, right);
	}

	public static double logsim(Line leftLine, Line rightLine) {
		return logsim(leftLine.value(), rightLine.value());
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

	private static double equality(Line leftLine, Line rightLine) {
		return leftLine.value().equals(rightLine.value()) ? 1.0 : 0.0;
	}

}
