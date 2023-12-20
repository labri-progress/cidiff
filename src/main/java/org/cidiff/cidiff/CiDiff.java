package org.cidiff.cidiff;

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
		Properties options = parseOptions(args);
		Options.setup(options);
		DiffClient.Type clientType = Options.getInstance().getClientType();

		LogDiffer differ = Options.getInstance().getAlgorithm().construct();
//		var processor = DiffProcessor.get(Options.getInstance().getPostProcessor());
		LogParser parser = Options.getInstance().getParser().construct();
		List<Line> leftLines = parser.parse(leftFile);
		List<Line> rightLines = parser.parse(rightFile);
//		for (LogFilter.Type type : Options.getInstance().getFilters()) {
//			LogFilter filter = LogFilter.get(type);
//			filter.filter(leftLines);
//			filter.filter(rightLines);
//		}
		Pair<List<Action>> actions = differ.diff(leftLines, rightLines);
//		processor.process(lines, actions);
		DiffClient client = clientType.construct(new Pair<>(leftLines, rightLines), actions);
		client.execute();
	}

	public static Properties parseOptions(String[] args) {
		final Set<String> allOptions = Options.allOptions();
		final Properties options = new Properties();
		if (args.length > 2) {
			if ((args.length - 2) % 3 != 0)
				throw new IllegalArgumentException("Wrong number of arguments " + args.length);
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
