package org.github.gumtreediff.cidiff.parsers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FullGithubLogParser extends AbstractLogParser {
    final static Pattern LOG_LINE_REGEXP = Pattern.compile("([^\\t]+)\\t([^\\t]+)\\t(.*)");
    final static int TIMESTAMP_SIZE = 29; // GitHub logs have a 29 characters timestamp

    public FullGithubLogParser(Properties options) {
        super(options);
    }

    @Override
    public List<String> parse(String file) throws IOException {
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
