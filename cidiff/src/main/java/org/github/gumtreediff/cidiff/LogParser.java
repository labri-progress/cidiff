package org.github.gumtreediff.cidiff;

import org.github.gumtreediff.cidiff.parsers.DefaultLogParser;
import org.github.gumtreediff.cidiff.parsers.FullGithubLogParser;
import org.github.gumtreediff.cidiff.parsers.RawGithubLogParser;

import java.io.IOException;
import java.util.*;

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

    static Pair<List<String>> parseLogs(Pair<String> files, Properties options) {
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

    List<String> parse(String file) throws IOException;
}
