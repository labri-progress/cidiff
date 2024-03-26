package org.github.cidiff;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Set;

public final class CiDiff {
	private CiDiff() {}

	public static void main(String[] args) {
//		System.out.println(Arrays.toString(args));
		if (args.length == 0 || Arrays.stream(args).anyMatch(arg -> arg.equals("-h") || arg.equals("--help"))) {
			System.out.println(Options.getDescription());
			return;
		}
		String leftFile = args[0];
		String rightFile = args[1];
		Properties properties = parseOptions(args);
		Options options = Options.from(properties);
		DiffClient.Type clientType = options.clientType();

		LogDiffer differ = options.algorithm().construct();
//		var processor = DiffProcessor.get(Options.getInstance().getPostProcessor());
		LogParser parser = options.parser().construct();
		List<Line> leftLines = parser.parse(leftFile, options);
		List<Line> rightLines = parser.parse(rightFile, options);
//		for (LogFilter.Type type : Options.getInstance().getFilters()) {
//			LogFilter filter = LogFilter.get(type);
//			filter.filter(leftLines);
//			filter.filter(rightLines);
//		}
		Pair<List<Action>> actions = differ.diff(leftLines, rightLines, options);
//		processor.process(lines, actions);
		DiffClient client = clientType.construct(new Pair<>(leftLines, rightLines), actions);
		client.execute(options);
	}

	public static Properties parseOptions(String[] args) {
		final Set<String> allOptions = Options.allOptions();
		final Properties options = new Properties();
		if (args.length > 2) {
			if ((args.length - 2) % 3 != 0) {
				System.out.println(Arrays.toString(args));
				throw new IllegalArgumentException("Wrong number of arguments " + args.length);
			}
			for (int i = 2; i < args.length; i = i + 3) {
				final String option = args[i];
				if (!"-o".equals(option))
					throw new IllegalArgumentException("Illegal option flag: " + option);

				final String key = args[i + 1];
				if (!allOptions.contains(key))
					throw new IllegalArgumentException("Illegal option: " + key);

				final String value = args[i + 2];
				options.setProperty(key, value);
			}
		}
		return options;
	}
}
