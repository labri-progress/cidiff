package org.github.gumtreediff.cidiff.parsers;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.github.gumtreediff.cidiff.LogLine;

public class FullGithubLogParser extends AbstractLogParser {
    static final Pattern LOG_LINE_REGEXP = Pattern.compile("([^\\t]+)\\t+([^\\t]+)\\t+(.*)");
    static final int TIMESTAMP_SIZE = 29; // GitHub logs have a 29 characters timestamp

    public FullGithubLogParser(Properties options) {
        super(options);
    }

    @Override
    public List<LogLine> parse(String file) throws IOException {
        final List<LogLine> log = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            int lineNumber = 0;
            int relativeIndex = 0;
            for (String line; (line = br.readLine()) != null;) {
                lineNumber++;
                final Matcher m = LOG_LINE_REGEXP.matcher(line);
                final boolean isMatching = m.matches();
                if (!isMatching)
                    continue;
                final String content = m.group(3);
                if (content.length() <= TIMESTAMP_SIZE)
                    continue;

                log.add(new LogLine(
                        content.substring(TIMESTAMP_SIZE),
                        lineNumber,
                        m.start(3) + TIMESTAMP_SIZE + 1,
                        line.length() + 1
                ));
                relativeIndex++;
            }
        }

        return log;
    }
}
