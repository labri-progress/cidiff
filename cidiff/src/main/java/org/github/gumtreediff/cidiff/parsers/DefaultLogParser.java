package org.github.gumtreediff.cidiff.parsers;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.github.gumtreediff.cidiff.LogLine;
import org.github.gumtreediff.cidiff.Options;

public final class DefaultLogParser extends AbstractLogParser {
    public static final String DEFAULT_TRIM = "0";
    public final int trim;

    public DefaultLogParser(Properties options) {
        super(options);
        this.trim = Integer.parseInt(options.getProperty(
                Options.PARSER_DEFAULT_TRIM, DEFAULT_TRIM));
    }

    public List<LogLine> parse(String file) throws IOException {
        final List<LogLine> log = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            int lineNumber = 0;
            int relativeIndex = 0;
            for (String line; (line = br.readLine()) != null;) {
                lineNumber++;
                if (line.length() > trim) {
                    final String value = line.substring(trim);
                    log.add(new LogLine(value, lineNumber,
                            trim + 1, trim + value.length() + 1));
                    relativeIndex++;
                }
            }
        }

        return log;
    }
}
