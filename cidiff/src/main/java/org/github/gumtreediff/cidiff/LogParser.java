package org.github.gumtreediff.cidiff;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class LogParser {
    public enum Type {
        GITHUB,
        DEFAULT
    }

    public final String leftLogFile;
    public final String rightLogFile;
    public final Map<String, List<String>> leftSteps;
    public final Map<String, List<String>> rightSteps;
    public final Properties options;

    private LogParser(String leftLogFile, String rightLogFile, Properties options) {
        this.options = options;
        this.leftLogFile = leftLogFile;
        this.rightLogFile = rightLogFile;
        this.leftSteps = new LinkedHashMap<>();
        this.rightSteps = new LinkedHashMap<>();
        parse();
    }
    
    protected void parse() {
        try {
            loadLog(leftLogFile, leftSteps);
            loadLog(rightLogFile, rightSteps);
        }
        catch (IOException e) {
            throw new IllegalArgumentException("Error parsing logs");
        }
    }

    protected abstract void loadLog(String logFile, Map<String, List<String>> logSteps) throws IOException;

    public static LogParser getParser(String leftLogFile, String rightLogFile, Properties options) {
        Type type = Type.valueOf(options.getProperty(Options.PARSER, "DEFAULT"));
        switch (type) {
            case GITHUB:
                return new GithubLogParser(leftLogFile, rightLogFile, options);
            case DEFAULT:
                return new DefaultLogParser(leftLogFile, rightLogFile, options);
        }

        throw new IllegalArgumentException("Unable to create parser");
    }

    public final static class DefaultLogParser extends LogParser {
        final static String DEFAULT_STEP = "default";
        final static String DEFAULT_TIMESTAMP_SIZE = "0";
        final int timestampSize;

        private DefaultLogParser(String leftLogFile, String rightLogFile, Properties options) {
            super(leftLogFile, rightLogFile, options);
            this.timestampSize = Integer.parseInt(options.getProperty(
                    Options.PARSER_DEFAULT_TIMESTAMPSIZE, DEFAULT_TIMESTAMP_SIZE));
        }

        protected void loadLog(String logFile, Map<String, List<String>> logSteps) throws IOException {
            Files.lines(Paths.get(logFile)).forEach(
                line -> {
                    if (line.length() < timestampSize)
                        throw new IllegalArgumentException("Illegal log format: " + line);
                    
                    logSteps.putIfAbsent(DEFAULT_STEP, new ArrayList<>());
                    logSteps.get(DEFAULT_STEP).add(line.substring(timestampSize));
                }
            );
        }
    }

    public final static class GithubLogParser extends LogParser {
        final static Pattern LOG_LINE_REGEXP = Pattern.compile("([^\\t]+)\\t([^\\t]+)\\t(.*)");
        final static int TIMESTAMP_SIZE = 29;

        private GithubLogParser(String leftLogFile, String rightLogFile, Properties options) {
            super(leftLogFile, rightLogFile, options);
        }

        protected void loadLog(String logFile, Map<String, List<String>> logSteps) throws IOException {
            Files.lines(Paths.get(logFile)).forEach(
                line -> {
                    final Matcher m = LOG_LINE_REGEXP.matcher(line);
                    final boolean isMatching = m.matches();
                    if (!isMatching)
                        throw new IllegalArgumentException("Illegal log format: " + line);
                    // final String job = m.group(1); in case of multiple jobs
                    final String step = m.group(2);
                    final String content = m.group(3);
                    if (content.length() < TIMESTAMP_SIZE)
                        throw new IllegalArgumentException("Illegal log format: " + line);
                    
                    logSteps.putIfAbsent(step, new ArrayList<>());
                    logSteps.get(step).add(content.substring(TIMESTAMP_SIZE)); // GITHUB has a 29 character timestamp
                }
            );
        }
    }
}
