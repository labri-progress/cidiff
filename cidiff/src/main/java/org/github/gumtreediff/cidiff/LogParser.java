package org.github.gumtreediff.cidiff;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class LogParser {
    static String DEFAULT_PARSER = "DEFAULT";

    public enum Type {
        RAW_GITHUB,
        FULL_GITHUB,
        DEFAULT
    }

    public static LogParser get(String leftLogFile, String rightLogFile, Properties options) {
        Type type = Type.valueOf(options.getProperty(Options.PARSER, DEFAULT_PARSER));
        return switch (type) {
            case RAW_GITHUB -> new RawGithubLogParser(leftLogFile, rightLogFile, options);
            case FULL_GITHUB -> new FullGithubLogParser(leftLogFile, rightLogFile, options);
            case DEFAULT -> new DefaultLogParser(leftLogFile, rightLogFile, options);
        };
    }

    public final Pair<String> files;
    public final Pair<List<String>> lines;
    public final Properties options;

    private LogParser(String leftLogFile, String rightLogFile, Properties options) {
        this.options = options;
        this.files = new Pair<>(leftLogFile, rightLogFile);
        this.lines = new Pair<>(new ArrayList<>(), new ArrayList<>());
    }
    
    public void parse() {
        try {
            loadLog(files.left, lines.left);
            loadLog(files.right, lines.right);
        }
        catch (IOException e) {
            throw new IllegalArgumentException("Error parsing logs: " + e);
        }
    }

    protected abstract void loadLog(String file, List<String> lines) throws IOException;

    public static class DefaultLogParser extends LogParser {
        final static String DEFAULT_TRIM = "0";
        final int trim;

        private DefaultLogParser(String leftLogFile, String rightLogFile, Properties options) {
            super(leftLogFile, rightLogFile, options);
            this.trim = Integer.parseInt(options.getProperty(
                    Options.PARSER_DEFAULT_TRIM, DEFAULT_TRIM));
        }

        protected void loadLog(String file, List<String> lines) throws IOException {
            Files.lines(Paths.get(file)).forEach(
                line -> {
                    if (line.length() < trim)
                        throw new IllegalArgumentException("Illegal log format: " + line);

                    lines.add(line.substring(trim));
                }
            );
        }
    }

    public final static class RawGithubLogParser extends LogParser {
        final static int TIMESTAMP_SIZE = 29; // GitHub logs have a 29 characters prefix

        private RawGithubLogParser(String leftLogFile, String rightLogFile, Properties options) {
            super(leftLogFile, rightLogFile, options);
        }

        protected void loadLog(String file, List<String> lines) throws IOException {
            Files.lines(Paths.get(file)).forEach(
                line -> {
                    if (line.length() < TIMESTAMP_SIZE)
                        throw new IllegalArgumentException("Illegal log format: " + line);

                    lines.add(line.substring(TIMESTAMP_SIZE));
                }
            );
        }
    }

    public final static class FullGithubLogParser extends LogParser {
        final static Pattern LOG_LINE_REGEXP = Pattern.compile("([^\\t]+)\\t([^\\t]+)\\t(.*)");
        final static int TIMESTAMP_SIZE = 29; // GitHub logs have a 29 characters timestamp

        private FullGithubLogParser(String leftLogFile, String rightLogFile, Properties options) {
            super(leftLogFile, rightLogFile, options);
        }

        protected void loadLog(String file, List<String> lines) throws IOException {
            Files.lines(Paths.get(file)).forEach(
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

                        lines.add(content.substring(TIMESTAMP_SIZE));
                    }
            );
        }
    }
}
