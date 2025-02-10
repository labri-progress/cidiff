package org.github.cidiff;

import org.simmetrics.metrics.StringMetrics;

import java.util.Arrays;
import java.util.function.BiFunction;

public enum Metric {

	LOGSIM(Metric::logsim),
	EQUALITY((leftLine, rightLine) -> leftLine.equals(rightLine) ? 1.0 : 0.0),
	JARO_WINKLER((line, line2) -> (double) StringMetrics.jaroWinkler().compare(line, line2)),
	LEVENSHTEIN((line, line2) -> (double) StringMetrics.levenshtein().compare(line, line2)),
	COSINE((line, line2) -> (double) StringMetrics.cosineSimilarity().compare(line, line2)),
	MONGE_ELKMAN((line, line2) -> (double) StringMetrics.mongeElkan().compare(line, line2)),
	SMITH_WATERMAN((line, line2) -> (double) StringMetrics.smithWaterman().compare(line, line2)),
	JACCARD((line, line2) -> (double) StringMetrics.generalizedJaccard().compare(line, line2)),
	DRAINSIM(Metric::drainsim);

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
			} else if (StringMetrics.qGramsDistance().compare(leftTokens[i], rightTokens[i]) >= 0.6) {
				count += 0.5;
			}
		}

		if (!ok) {
			return 0;
		}

		return count / leftTokens.length;
	}

	/**
	 * Compute the similarity between two line using the drain parser.
	 * Two lines are considered similar if they have the same template determined by drain.
	 * <br>
	 * It uses {@link Drain#INSTANCE} to check the template, so you need to set it up correctly before calling this method.
	 * The easiest is to use {@link LogParser.Type#DRAIN} to create a parser and parse the logs with it.
	 * <pre>
	 * {@code
	 * LogParser parser = LogParser.Type.DRAIN.construct();
	 * parser.parse(leftLines);
	 * parser.parse(rightLines);
	 * // now you can use drainsim
	 * double sim = Metric.DRAINSIM.sim(leftLine, rightLine);
	 * }
	 * </pre>
	 *
	 * @param leftLine
	 * @param rightLine
	 * @return 1.0 if the lines are identical, 0.75 if they are similar (same template), 0.0 otherwise
	 */
	public static double drainsim(String leftLine, String rightLine) {
		// The drain instance is set up during the creation of the DrainParser. Consequently, drainsim can only be used
		// when the DrainParser is used.
		// We're assuming drain has parsed the left and right log lines.
		// But just in case something got wrong, let's check if the instance is null.
		Drain drain = Drain.INSTANCE;
		if (drain == null) {
			return 0;
		}

		// if both lines are the same, directly return 1.0. They should have the same template too.
		if (leftLine.equals(rightLine)) {
			return 1.0;
		}

		final String[] leftTokens = Utils.split(leftLine.trim());
		final String[] rightTokens = Utils.split(rightLine.trim());

		Drain.LogCluster leftCluster = drain.treeSearch(drain.rootNode, Arrays.asList(leftTokens));
		Drain.LogCluster rightCluster = drain.treeSearch(drain.rootNode, Arrays.asList(rightTokens));

		// We decide that a wildcard template is a template of 1 or fewer sightings.
		// i.e. a template with 2 or more sightings is assumed to be a valid template.
		// A wildcard template will not match any template.
		if (leftCluster == null || rightCluster == null || leftCluster.sightings() <= 1 || rightCluster.sightings() <= 1) {
			// When one of the line has a wildcard template, we can be sure it is not identical nor similar to the other line
			// (or else they both would have had the same template)
			return 0.0;
		}
		if (leftCluster.logTemplate.equals(rightCluster.logTemplate)) {
			return 0.75;
		} else {
			return 0.0;
		}
	}

}
