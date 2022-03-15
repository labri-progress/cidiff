package org.github.gumtreediff.cidiff;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Abstract class for log parsers.
 *
 * The parse method of log parsers must be stateless.
 */
public abstract class LogParser {
    static String DEFAULT_PARSER = "DEFAULT";

    final Properties options;

    public enum Type {
        RAW_GITHUB,
        FULL_GITHUB,
        DEFAULT
    }

    public LogParser(Properties options) {
        this.options = options;
    }

    public static Pair<List<String>> parseLogs(Pair<String> files, Properties options) {
        Type type = Type.valueOf(options.getProperty(Options.PARSER, DEFAULT_PARSER));
        LogParser parser = switch (type) {
            case RAW_GITHUB -> new RawGithubLogParser(options);
            case FULL_GITHUB -> new FullGithubLogParser(options);
            case DEFAULT -> new DefaultLogParser(options);
        };
        try {
            return new Pair<>(parser.parse(files.left), parser.parse(files.right));
        }
        catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    protected abstract List<String> parse(String file) throws IOException;

    public static class DefaultLogParser extends LogParser {
        final static String DEFAULT_TRIM = "0";
        final int trim;

        private DefaultLogParser(Properties options) {
            super(options);
            this.trim = Integer.parseInt(options.getProperty(
                    Options.PARSER_DEFAULT_TRIM, DEFAULT_TRIM));
        }

        protected List<String> parse(String file) throws IOException {
            return Files.lines(Paths.get(file)).map(
                line -> {
                    if (line.length() < trim)
                        throw new IllegalArgumentException("Illegal log format: " + line);

                    return line.substring(trim);
                }
            ).toList();
        }
    }

    public final static class RawGithubLogParser extends LogParser {
        final static int TIMESTAMP_SIZE = 29; // GitHub logs have a 29 characters prefix

        private RawGithubLogParser(Properties options) {
            super(options);
        }

        protected List<String> parse(String file) throws IOException {
            return Files.lines(Paths.get(file)).map(
                line -> {
                    if (line.length() < TIMESTAMP_SIZE)
                        throw new IllegalArgumentException("Illegal log format: " + line);

                    return line.substring(TIMESTAMP_SIZE);
                }
            ).toList();
        }
    }

    public final static class FullGithubLogParser extends LogParser {
        final static Pattern LOG_LINE_REGEXP = Pattern.compile("([^\\t]+)\\t([^\\t]+)\\t(.*)");
        final static int TIMESTAMP_SIZE = 29; // GitHub logs have a 29 characters timestamp

        private FullGithubLogParser(Properties options) {
            super(options);
        }

        protected List<String> parse(String file) throws IOException {
            return Files.lines(Paths.get(file)).map(
                line -> {
                    final Matcher m = LOG_LINE_REGEXP.matcher(line);
                    final boolean isMatching = m.matches();
                    if (!isMatching)
                        throw new IllegalArgumentException("Illegal log format: " + line);
                    // final String job = m.group(1); in case of multiple jobs
                    final String content = m.group(3);
                    if (content.length() < TIMESTAMP_SIZE)
                        throw new IllegalArgumentException("Illegal log format: " + line);

                    return content.substring(TIMESTAMP_SIZE);
                }
            ).toList();
        }
    }
}
