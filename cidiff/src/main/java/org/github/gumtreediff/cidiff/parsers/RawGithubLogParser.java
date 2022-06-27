package org.github.gumtreediff.cidiff.parsers;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.github.gumtreediff.cidiff.LogLine;

public final class RawGithubLogParser extends AbstractLogParser {
    private static final int TIMESTAMP_SIZE = 29; // GitHub logs have a 29 characters prefix

    public RawGithubLogParser(Properties options) {
        super(options);
    }

    public List<LogLine> parse(String file) throws IOException {
        final List<LogLine> log = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            int lineNumber = 0;
            int relativeIndex = 0;
            for (String line; (line = br.readLine()) != null;) {
                lineNumber++;
                if (line.length() > TIMESTAMP_SIZE) {
                    final String value = line.substring(TIMESTAMP_SIZE);
                    log.add(new LogLine(value, lineNumber, relativeIndex,
                            TIMESTAMP_SIZE + 1, TIMESTAMP_SIZE + value.length() + 1));
                    relativeIndex++;
                }
            }
        }

        return log;
    }
}
