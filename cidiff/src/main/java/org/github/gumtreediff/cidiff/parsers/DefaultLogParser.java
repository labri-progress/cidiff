package org.github.gumtreediff.cidiff.parsers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;

import org.github.gumtreediff.cidiff.Options;

public final class DefaultLogParser extends AbstractLogParser {
    public static final String DEFAULT_TRIM = "0";
    public final int trim;

    public DefaultLogParser(Properties options) {
        super(options);
        this.trim = Integer.parseInt(options.getProperty(
                Options.PARSER_DEFAULT_TRIM, DEFAULT_TRIM));
    }

    public List<String> parse(String file) throws IOException {
        return Files.lines(Paths.get(file))
                .filter(line -> line.length() > trim)
                .map(line -> line.substring(trim))
                .toList();
    }
}
