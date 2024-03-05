package org.github.cidiff;

import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class Options {

	// TODO: 11/20/23 nhubner add back filters et processors

	private static final Option<DiffClient.Type> CLIENT = new Option<>(DiffClient.Type.CONSOLE, "client", "The client to use to view the result: CONSOLE, METRICS, JSON, SWING", DiffClient.Type::valueOf);
	private static final Option<LogDiffer.Algorithm> DIFFER = new Option<>(LogDiffer.Algorithm.BRUTE_FORCE, "differ", "The diff algorithm to use: BRUTE_FORCE, LCS, SEED, HASH", LogDiffer.Algorithm::valueOf);
	private static final Option<LogParser.Type> PARSER = new Option<>(LogParser.Type.TRIMMING, "parser", "The parser to use before diffing: TRIMMING, GITHUB", LogParser.Type::valueOf);
	private static final Option<Metric> METRIC = new Option<>(Metric.LOGSIM, "metric", "The string similarity metric to use: LOGSIM, EQUALITY", Metric::valueOf);
	private static final Option<Double> REWRITE_MIN = new Option<>(0.5, "differ.rewrite_min", "The minimum similitude value to accept two string as modified.", Double::parseDouble);
	private static final Option<Boolean> SKIP_EMPTY_LINES = new Option<>(false, "differ.bf.skip_empty", "If the brute force algorithm should skip empty lines.", Boolean::parseBoolean);
	private static final Option<Boolean> MERGE_ADJACENT_SEEDS = new Option<>(false, "differ.seed.merge_seeds", "If the seed-and-extends algorithm should merge adjacent seeds.", Boolean::parseBoolean);
	private static final Option<Boolean> RECURSIVE_SEARCH = new Option<>(false, "differ.seed.recursive_search", "If the seed-and-extends algorithm should search unique lines recursively.", Boolean::parseBoolean);
	private static final Option<Boolean> EVEN_IDENTICAL = new Option<>(false, "differ.seed.even", "If the seed-and-extends algorithm should start with even identical lines", Boolean::parseBoolean);
	private static final Option<Integer> DEFAULT_TRIM = new Option<>(0, "parser.trimming.trim", "The amount of character to remove at the start of each lines with the TRIMMING parser", Integer::parseInt);
	private static final Option<Boolean> DISPLAY_UPDATED = new Option<>(false, "client.console.updated", "If the updated lines should be shown", Boolean::parseBoolean);
	private static final Option<Boolean> DISPLAY_UNCHANGED = new Option<>(false, "client.console.unchanged", "If the unchanged lines should be shown", Boolean::parseBoolean);
	private static final Option<Boolean> DISPLAY_ADDED = new Option<>(true, "client.console.added", "If the added lines should be shown", Boolean::parseBoolean);
	private static final Option<Boolean> DISPLAY_DELETED = new Option<>(true, "client.console.deleted", "If the deleted lines should be shown", Boolean::parseBoolean);
	private static final Option<Boolean> DISPLAY_SKIPPED_NOTICE = new Option<>(true, "client.swing.skipped_notice", "If a notice line should be added when some are removed with a filter", Boolean::parseBoolean);
	private static final Option<String> DISPLAY_COLUMNS = new Option<>("", "client.swing.columns", "If \"left\" or \"right\", will display only the corresponding column", str -> str);

	private static final List<Option<?>> options = List.of(CLIENT, DIFFER, PARSER, METRIC, REWRITE_MIN, SKIP_EMPTY_LINES, MERGE_ADJACENT_SEEDS, RECURSIVE_SEARCH, EVEN_IDENTICAL, DEFAULT_TRIM, DISPLAY_UPDATED, DISPLAY_UNCHANGED, DISPLAY_ADDED, DISPLAY_DELETED, DISPLAY_SKIPPED_NOTICE, DISPLAY_COLUMNS);

	private Options() {
	}

	public static void setup(Properties options) {
		Options.options.forEach(opt -> opt.setup(options));
	}

	public static void setup() {
		setup(new Properties());
	}

	public static Set<String> allOptions() {
		return options.stream().map(Option::name).collect(Collectors.toSet());
	}

	public static String getDescription() {
		StringBuilder description = new StringBuilder("""
				CiDiff - awesome differ for CI build logs
				                
				Usage:    cidiff <left_log_path> <right_log_path> [-o <flag> <value>]...
				                
				Flags:
				""");
		int max = options.stream().mapToInt(opt -> opt.name.length()).max().orElse(0);
		for (Option<?> option : options) {
			description.append("  ").append(option.name).append(" ".repeat(max - option.name.length() + 1)).append(option.description).append(" (default: ").append(option.defaultValue).append(")\n");
		}
		return description.toString();
	}

	public static DiffClient.Type getClientType() {
		return CLIENT.value;
	}

	public static LogDiffer.Algorithm getAlgorithm() {
		return DIFFER.value;
	}

	public static LogParser.Type getParser() {
		return PARSER.value;
	}

	public static Metric metric() {
		return METRIC.value;
	}
	public static double getRewriteMin() {
		return REWRITE_MIN.value;
	}

	public static boolean getMergeAdjacentLInes() {
		return MERGE_ADJACENT_SEEDS.value;
	}

	public static boolean getRecursiveSearch() {
		return RECURSIVE_SEARCH.value;
	}

	public static boolean getEvenIdentical() {
		return EVEN_IDENTICAL.value;
	}

	public static boolean getSkipEmptyLines() {
		return SKIP_EMPTY_LINES.value;
	}

	public static int getParserDefaultTrim() {
		return DEFAULT_TRIM.value;
	}

	public static boolean getConsoleDisplayUpdated() {
		return DISPLAY_UPDATED.value;
	}

	public static boolean getConsoleDisplayUnchanged() {
		return DISPLAY_UNCHANGED.value;
	}

	public static boolean getConsoleDisplayAdded() {
		return DISPLAY_ADDED.value;
	}

	public static boolean getConsoleDisplayDeleted() {
		return DISPLAY_DELETED.value;
	}

	public static boolean getSwingDisplaySkippedNotice() {
		return DISPLAY_SKIPPED_NOTICE.value;
	}

	public static String getSwingColumns() {
		return DISPLAY_COLUMNS.value;
	}

	public static final class Option<T> {

		private final T defaultValue;
		private final String name;
		private final String description;
		private final Function<String, T> parse;
		private T value;

		public Option(T defaultValue, String name, String description, Function<String, T> parse) {
			this.defaultValue = defaultValue;
			this.parse = parse;
			this.value = this.defaultValue;
			this.name = name;
			this.description = description;
		}

		public T value() {
			return value;
		}

		public String name() {
			return name;
		}

		public String description() {
			return description;
		}

		public void setup(Properties properties) {
			if (properties.containsKey(this.name)) {
				this.value = parse.apply(properties.getProperty(this.name));
			} else {
				this.value = this.defaultValue;
			}
		}

	}

}
