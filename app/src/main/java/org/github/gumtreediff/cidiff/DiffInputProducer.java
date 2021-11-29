package org.github.gumtreediff.cidiff;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class DiffInputProducer {
    public static enum Type {
        GITHUB,
        CLASSIC
    }

    public final String leftLogFile;
    public final String rightLogFile;
    public final Map<String, List<String>> leftSteps;
    public final Map<String, List<String>> rightSteps;

    public DiffInputProducer(String leftLogFile, String rightLogFile) {
        this.leftLogFile = leftLogFile;
        this.rightLogFile = rightLogFile;
        this.leftSteps = new LinkedHashMap<>();
        this.rightSteps = new LinkedHashMap<>();
        parse();
    }
    
    public void parse() {
        try {
            loadLog(leftLogFile, leftSteps);
            loadLog(rightLogFile, rightSteps);
        }
        catch (IOException e) {
            throw new IllegalArgumentException("Error parsing logs");
        }
    }

    public abstract void loadLog(String logFile, Map<String, List<String>> logSteps) throws IOException;

    public final static class ClassicDiffInputProducer  extends DiffInputProducer {
        final static String DEFAULT_STEP = "default";
        final int timestampSize;

        public ClassicDiffInputProducer(String leftLogFile, String rightLogFile) {
            this(leftLogFile, rightLogFile, 0);
        }

        public ClassicDiffInputProducer(String leftLogFile, String rightLogFile, int timestampSize) {
            super(leftLogFile, rightLogFile);
            this.timestampSize = timestampSize;
        }

        public void loadLog(String logFile, Map<String, List<String>> logSteps) throws IOException {
            Files.lines(Paths.get(logFile)).forEach(
                line -> {
                    if (line.length() < timestampSize)
                        throw new IllegalArgumentException("Illegal log format: " + line);
                    
                    logSteps.putIfAbsent(DEFAULT_STEP, new ArrayList<>());
                    logSteps.get(DEFAULT_STEP).add(line.substring(timestampSize));
                }
            );
        }
    }

    public final static class GithubDiffInputProducer  extends DiffInputProducer {
        final static Pattern LOG_LINE_REGEXP = Pattern.compile("([^\\t]+)\\t([^\\t]+)\\t(.*)");
        final static int TIMESTAMP_SIZE = 29;

        public GithubDiffInputProducer(String leftLogFile, String rightLogFile) {
            super(leftLogFile, rightLogFile);
        }

        public void loadLog(String logFile, Map<String, List<String>> logSteps) throws IOException {
            Files.lines(Paths.get(logFile)).forEach(
                line -> {
                    final Matcher m = LOG_LINE_REGEXP.matcher(line);
                    m.matches();
                    // final String job = m.group(1);
                    final String step = m.group(2);
                    final String content = m.group(3);
                    if (content.length() < TIMESTAMP_SIZE)
                        throw new IllegalArgumentException("Illegal log format: " + line);
                    
                    logSteps.putIfAbsent(step, new ArrayList<>());
                    logSteps.get(step).add(content.substring(TIMESTAMP_SIZE)); // GITHUB has a 29 character timestamp
                }
            );
        }
    }
}
