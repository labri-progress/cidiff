package org.cidiff.cidiff;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

public final class Options {
    // TODO: 11/20/23 nhubner add back filters et processors

    private static Options instance;

    private DiffClient.Type clientType = DiffClient.Type.CONSOLE;
    private LogDiffer.Algorithm algorithm = LogDiffer.Algorithm.BRUTE_FORCE;
    private LogParser.Type parser = LogParser.Type.TRIMMING;
//    private LogFilter.Type[] filters = new LogFilter.Type[0];
//    private DiffProcessor.Type postProcessor = DiffProcessor.Type.NOOP;
    private double rewriteMin = 0.5;
    private boolean skipEmptyLines = false;
    private int seedBlockSize = 1;
    private int seedWindowSize = 30;
    private int parserDefaultTrim = 0;
    private boolean consoleDisplayUpdated = false;
    private boolean consoleDisplayUnchanged = false;
    private boolean consoleDisplayAdded = true;
    private boolean consoleDisplayDeleted = true;
    private boolean swingDisplaySkippedNotice = true;
    private boolean swingDisplayDebug = false;
    private String swingColumns = "";

    private Options() {
    }

    public static Options getInstance() {
        return instance;
    }

    public static void setup(Properties options) {
        instance = new Options();
        if (options.containsKey(Names.CLIENT))
            instance.clientType = DiffClient.Type.valueOf(options.getProperty(Names.CLIENT));
        if (options.containsKey(Names.DIFFER))
            instance.algorithm = LogDiffer.Algorithm.valueOf(options.getProperty(Names.DIFFER));
        if (options.containsKey(Names.PARSER))
            instance.parser = LogParser.Type.valueOf(options.getProperty(Names.PARSER));
//        if (options.containsKey(Names.DIFF_PROCESSOR))
//            instance.postProcessor = DiffProcessor.Type.valueOf(options.getProperty(Names.DIFF_PROCESSOR));
//        if (options.containsKey(Names.FILTER)) {
//            final String[] split = options.getProperty(Names.FILTER).split(",");
//            instance.filters = new LogFilter.Type[split.length];
//            for (int i = 0; i < split.length; i++) {
//                instance.filters[i] = LogFilter.Type.valueOf(split[i]);
//            }
//        }
        if (options.containsKey(Names.DIFFER_REWRITE_MIN))
            instance.rewriteMin = Double.parseDouble(options.getProperty(Names.DIFFER_REWRITE_MIN));
        if (options.containsKey(Names.DIFFER_BF_SKIP_EMPTY))
            instance.skipEmptyLines = Boolean.parseBoolean(options.getProperty(Names.DIFFER_BF_SKIP_EMPTY));
        if (options.containsKey(Names.DIFFER_SEED_BLOCK))
            instance.seedBlockSize = Integer.parseInt(options.getProperty(Names.DIFFER_SEED_BLOCK));
        if (options.containsKey(Names.DIFFER_SEED_WINDOW))
            instance.seedWindowSize = Integer.parseInt(options.getProperty(Names.DIFFER_SEED_WINDOW));
        if (options.containsKey(Names.PARSER_DEFAULT_TRIM))
            instance.parserDefaultTrim = Integer.parseInt(options.getProperty(Names.PARSER_DEFAULT_TRIM));
        if (options.containsKey(Names.CONSOLE_UPDATED))
            instance.consoleDisplayUpdated = Boolean.parseBoolean(options.getProperty(Names.CONSOLE_UPDATED));
        if (options.containsKey(Names.CONSOLE_UNCHANGED))
            instance.consoleDisplayUnchanged = Boolean.parseBoolean(options.getProperty(Names.CONSOLE_UNCHANGED));
        if (options.containsKey(Names.CONSOLE_ADDED))
            instance.consoleDisplayAdded = Boolean.parseBoolean(options.getProperty(Names.CONSOLE_ADDED));
        if (options.containsKey(Names.CONSOLE_DELETED))
            instance.consoleDisplayDeleted = Boolean.parseBoolean(options.getProperty(Names.CONSOLE_DELETED));
        if (options.containsKey(Names.SWING_DISPLAY_SKIPPED_NOTICE))
            instance.swingDisplaySkippedNotice = Boolean
                    .parseBoolean(options.getProperty(Names.SWING_DISPLAY_SKIPPED_NOTICE));
        if (options.containsKey(Names.SWING_DISPLAY_DEBUG))
            instance.swingDisplayDebug = Boolean.parseBoolean(options.getProperty(Names.SWING_DISPLAY_DEBUG));
        if (options.containsKey(Names.SWING_COLUMNS))
            instance.swingColumns = options.getProperty(Names.SWING_COLUMNS);
    }

    public static Set<String> allOptions() {
        final var fields = Arrays.stream(Names.class.getDeclaredFields())
                .filter(f -> Modifier.isStatic(f.getModifiers()))
                .toList();
        final Set<String> options = new HashSet<>();
        try {
            for (Field f : fields)
                options.add((String) f.get(null));
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return options;
    }

    public DiffClient.Type getClientType() {
        return clientType;
    }

    public LogDiffer.Algorithm getAlgorithm() {
        return algorithm;
    }

    public LogParser.Type getParser() {
        return parser;
    }

//    public LogFilter.Type[] getFilters() {
//        return filters;
//    }
//
//    public DiffProcessor.Type getPostProcessor() {
//        return postProcessor;
//    }

    public double getRewriteMin() {
        return rewriteMin;
    }

    public boolean getSkipEmptyLines() {
        return skipEmptyLines;
    }

    public int getSeedBlockSize() {
        return seedBlockSize;
    }

    public int getSeedWindowSize() {
        return seedWindowSize;
    }

    public int getParserDefaultTrim() {
        return parserDefaultTrim;
    }

    public boolean getConsoleDisplayUpdated() {
        return consoleDisplayUpdated;
    }

    public boolean getConsoleDisplayUnchanged() {
        return consoleDisplayUnchanged;
    }

    public boolean getConsoleDisplayAdded() {
        return consoleDisplayAdded;
    }

    public boolean getConsoleDisplayDeleted() {
        return consoleDisplayDeleted;
    }

    public boolean getSwingDisplaySkippedNotice() {
        return swingDisplaySkippedNotice;
    }

    public boolean getSwingDisplayDebug() {
        return swingDisplayDebug;
    }

    public String getSwingColumns() {
        return swingColumns;
    }


    public static String getDescription() {
        return """
                CiDiff - awesome differ for CI build logs
                
                Usage:    cidiff <left_log_path> <right_log_path> [-o <flag> <value>]...
                
                Flags:
                  differ                       The diff algorithm to use (default: BRUTE_FORCE):
                                               BRUTE_FORCE, ALTERNATING_BRUTE_FORCE, LCS, SEED_EXTEND, SEED, HASH
                  differ.rewrite.min           The minimum similitude value to accept two string as modified (default: 0.5)
                  differ.bf.skip_empty         If the brute force algorithm should skip empty lines. (default: false)
                  differ.seed.window           The size of the window to hash the lines in the seed diff algorithm. (default: 1)
                  parser                       The parser to use before diffing (default: DEFAULT):
                                               DEFAULT, RAW_GITHUB, FULL_GITHUB, GITHUB
                  parser.default.trim          The amount of character to remove at the start of each lines with the DEFAULT parser(default: 0)
                  filter                       Filters to use after parsing to remove lines (default: none):
                                               NOOP, REWRITE, TERM_FREQUENCY, WHITESPACE
                                               You can use multiple filers by separating them with a comma (,)
                  diffprocessor                Processor to use after the differ to modify its output (default: NOOP):
                                               NOOP, ISOLATED
                  client                       The client to use to view the result (default: CONSOLE):
                                               CONSOLE, METRICS, JSON, SWING
                  client.console.updated       If the updated lines should be shown (default: false)
                  client.console.added         If the added lines should be shown (default: true)
                  client.console.deleted       If the deleted lines should be shown (default: true)
                  client.console.unchanged     If the unchanged lines should be shown (default: false)
                  client.swing.skipped_notice  If a notice line should be added when some are removed with a filter (default: true)
                  client.swing.debug           If debug colors should be used (default: false)
                  client.swing.columns         If "left" or "right", will display only the corresponding columns. (default: "")
                """;
    }

    public static final class Names {

        public static final String DIFFER = "differ";

        public static final String DIFFER_REWRITE_MIN = "differ.rewrite.min";

        public static final String DIFFER_BF_SKIP_EMPTY = "differ.bf.skip_empty";

        public static final String DIFFER_SEED_BLOCK = "differ.seed.block";
        public static final String DIFFER_SEED_WINDOW = "differ.seed.window";

        public static final String PARSER = "parser";
        public static final String PARSER_DEFAULT_TRIM = "parser.default.trim";

        public static final String FILTER = "filter";

        public static final String DIFF_PROCESSOR = "diffprocessor";

        public static final String CLIENT = "client";

        public static final String CONSOLE_UPDATED = "client.console.updated";
        public static final String CONSOLE_UNCHANGED = "client.console.unchanged";
        public static final String CONSOLE_ADDED = "client.console.added";
        public static final String CONSOLE_DELETED = "client.console.deleted";

        public static final String SWING_DISPLAY_SKIPPED_NOTICE = "client.swing.skipped_notice";
        public static final String SWING_DISPLAY_DEBUG = "client.swing.debug";
        public static final String SWING_COLUMNS = "client.swing.columns";

    }

}
