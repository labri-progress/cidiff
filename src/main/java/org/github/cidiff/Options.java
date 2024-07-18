package org.github.cidiff;

import org.github.cidiff.differs.SeedDiffer;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class Options {

	public static final Option<DiffClient.Type> CLIENT = new Option<>("client", "The client to use to view the result: CONSOLE, METRICS, JSON, SWING", DiffClient.Type::valueOf);
	public static final Option<LogDiffer.Algorithm> DIFFER = new Option<>("differ", "The diff algorithm to use: BRUTE_FORCE, LCS, SEED, HASH", LogDiffer.Algorithm::valueOf);
	public static final Option<LogParser.Type> PARSER = new Option<>("parser", "The parser to use before diffing: TRIMMING, GITHUB", LogParser.Type::valueOf);
	public static final Option<Metric> METRIC = new Option<>("metric", "The string similarity metric to use: LOGSIM, EQUALITY", Metric::valueOf);
	public static final Option<Double> REWRITE_MIN = new Option<>("differ.rewrite_min", "The minimum similarity value to accept two string as modified.", Double::parseDouble);
	public static final Option<Double> QGRAM_MIN = new Option<>("differ.qgram_min", "The minimum qgram similarity value to accept two token as similar.", Double::parseDouble);
	public static final Option<Boolean> SKIP_EMPTY_LINES = new Option<>("parser.skip_empty", "If the parser should skip empty lines.", Boolean::parseBoolean);
	public static final Option<Boolean> MERGE_ADJACENT_SEEDS = new Option<>("differ.seed.merge_seeds", "If the seed-and-extends algorithm should merge adjacent seeds.", Boolean::parseBoolean);
	public static final Option<Boolean> SECOND_SEARCH = new Option<>("differ.seed.second_search", "If the seed-and-extends algorithm should search unique lines a second time.", Boolean::parseBoolean);
	public static final Option<Integer> DEFAULT_TRIM = new Option<>("parser.trimming.trim", "The amount of character to remove at the start of each lines with the TRIMMING parser", Integer::parseInt);
	public static final Option<Boolean> DISPLAY_UPDATED = new Option<>("client.console.updated", "If the updated lines should be shown", Boolean::parseBoolean);
	public static final Option<Boolean> DISPLAY_UNCHANGED = new Option<>("client.console.unchanged", "If the unchanged lines should be shown", Boolean::parseBoolean);
	public static final Option<Boolean> DISPLAY_ADDED = new Option<>("client.console.added", "If the added lines should be shown", Boolean::parseBoolean);
	public static final Option<Boolean> DISPLAY_DELETED = new Option<>("client.console.deleted", "If the deleted lines should be shown", Boolean::parseBoolean);
	public static final Option<Boolean> DISPLAY_SKIPPED_NOTICE = new Option<>("client.swing.skipped_notice", "If a notice line should be added when some are removed with a filter", Boolean::parseBoolean);
	public static final Option<String> DISPLAY_COLUMNS = new Option<>("client.swing.columns", "If \"left\" or \"right\", will display only the corresponding column", str -> str);
	public static final Option<String> OUTPUT_PATH = new Option<>("client.monaco.output", "The path to write the html file to", str -> str);

	private final Map<Option<?>, Object> map;

	public Options() {
		map = new HashMap<>();
		this.map.put(CLIENT, DiffClient.Type.CONSOLE);
		this.map.put(DIFFER, LogDiffer.Algorithm.SEED);
		this.map.put(PARSER, LogParser.Type.TRIMMING);
		this.map.put(METRIC, Metric.LOGSIM);
		this.map.put(REWRITE_MIN, 0.5);
		this.map.put(QGRAM_MIN, 0.6);
		this.map.put(SKIP_EMPTY_LINES, true);
		this.map.put(MERGE_ADJACENT_SEEDS, true);
		this.map.put(SECOND_SEARCH, true);
		this.map.put(DEFAULT_TRIM, 0);
		this.map.put(DISPLAY_UPDATED, false);
		this.map.put(DISPLAY_UNCHANGED, false);
		this.map.put(DISPLAY_ADDED, true);
		this.map.put(DISPLAY_DELETED, false);
		this.map.put(DISPLAY_SKIPPED_NOTICE, true);
		this.map.put(DISPLAY_COLUMNS, "");
		this.map.put(OUTPUT_PATH, "diff.html");
	}

	public static Options from(Properties properties) {
		Options opt = new Options();
		for (Option<?> option : opt.map.keySet()) {
			if (properties.containsKey(option.name)) {
				opt.map.put(option, option.parse.apply(properties.getProperty(option.name)));
			}
		}
		return opt;
	}

	public static Set<String> allOptions() {
		return new Options().map.keySet().stream().map(option -> option.name).collect(Collectors.toSet());
	}

	public static String getDescription() {
		StringBuilder description = new StringBuilder("""
				CiDiff - awesome differ for CI build logs

				Usage:    cidiff <left_log_path> <right_log_path> [-o <flag> <value>]...

				Flags:
				""");
		Options options = new Options();
		int max = options.map.keySet().stream().mapToInt(opt -> opt.name.length()).max().orElse(0);
		for (Option<?> option : options.map.keySet()) {
			description.append("  ").append(option.name)
					.append(" ".repeat(max - option.name.length() + 1))
					.append(option.description)
					.append(" (default: ").append(options.get(option)).append(")\n");
		}
		return description.toString();
	}

	public <T> Options with(Option<T> option, T value) {
		this.map.put(option, value);
		return this;
	}

	@SuppressWarnings("unchecked")
	public <T> T get(Option<T> option) {
		return (T) this.map.get(option);
	}

	public DiffClient.Type clientType() {
		return this.get(CLIENT);
	}

	public LogDiffer.Algorithm algorithm() {
		return this.get(DIFFER);
	}

	public LogParser.Type parser() {
		return this.get(PARSER);
	}

	public Metric metric() {
		return this.get(METRIC);
	}

	public double rewriteMin() {
		return this.get(REWRITE_MIN);
	}

	public double qGramMin() {
		return this.get(QGRAM_MIN);
	}

	public boolean mergeAdjacentLines() {
		return this.get(MERGE_ADJACENT_SEEDS);
	}

	public boolean secondSearch() {
		return this.get(SECOND_SEARCH);
	}

	public boolean skipEmptyLines() {
		return this.get(SKIP_EMPTY_LINES);
	}

	public int parserDefaultTrim() {
		return this.get(DEFAULT_TRIM);
	}

	public boolean consoleDisplayUpdated() {
		return this.get(DISPLAY_UPDATED);
	}

	public boolean consoleDisplayUnchanged() {
		return this.get(DISPLAY_UNCHANGED);
	}

	public boolean consoleDisplayAdded() {
		return this.get(DISPLAY_ADDED);
	}

	public boolean consoleDisplayDeleted() {
		return this.get(DISPLAY_DELETED);
	}

	public boolean swingDisplaySkippedNotice() {
		return this.get(DISPLAY_SKIPPED_NOTICE);
	}

	public String swingColumns() {
		return this.get(DISPLAY_COLUMNS);
	}

	public String monacoOutput() {
		return this.get(OUTPUT_PATH);
	}

	public static final class Option<T> {

		private final String name;
		private final String description;
		private final Function<String, T> parse;

		public Option(String name, String description, Function<String, T> parse) {
			this.parse = parse;
			this.name = name;
			this.description = description;
		}

		public String name() {
			return name;
		}

	}

}
