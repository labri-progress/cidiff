package org.github.gumtreediff.cidiff.parsers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;

public final class RawGithubLogParser extends AbstractLogParser {
    private static final int TIMESTAMP_SIZE = 29; // GitHub logs have a 29 characters prefix

    public RawGithubLogParser(Properties options) {
        super(options);
    }

    public List<String> parse(String file) throws IOException {
        return Files.lines(Paths.get(file))
                .filter(line -> line.length() > TIMESTAMP_SIZE)
                .map(line -> line.substring(TIMESTAMP_SIZE))
                .toList();
    }
}
