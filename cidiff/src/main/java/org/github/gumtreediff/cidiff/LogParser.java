package org.github.gumtreediff.cidiff;

import java.io.IOException;
import java.util.*;

import org.github.gumtreediff.cidiff.parsers.DefaultLogParser;
import org.github.gumtreediff.cidiff.parsers.FullGithubLogParser;
import org.github.gumtreediff.cidiff.parsers.RawGithubLogParser;

/**
 * Abstract class for log parsers.
 *
 * The parse method of log parsers must be stateless.
 */
public interface LogParser {
    String DEFAULT_PARSER = "DEFAULT";

    enum Type {
        RAW_GITHUB,
        FULL_GITHUB,
        DEFAULT
    }

    static LogParser get(Properties options) {
        final Type type = Type.valueOf(options.getProperty(Options.PARSER, DEFAULT_PARSER));
        return switch (type) {
            case RAW_GITHUB -> new RawGithubLogParser(options);
            case FULL_GITHUB -> new FullGithubLogParser(options);
            case DEFAULT -> new DefaultLogParser(options);
        };
    }

    static List<String> parseLog(String file, Properties options) {
        final LogParser parser = get(options);
        try {
            return parser.parse(file);
        }
        catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    static Pair<List<String>> parseLogs(Pair<String> files, Properties options) {
        return new Pair<>(parseLog(files.left, options), parseLog(files.right, options));
    }

    List<String> parse(String file) throws IOException;
}
