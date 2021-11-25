package org.github.gumtreediff.cidiff;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JobDiffer {
    private final String leftLogFile;
    private final String rightLogFile;
    private final Map<String, List<String>> leftSteps;
    private final Map<String, List<String>> rightSteps;

    private final static String GITHUB_ACTION_DEBUG = "##[debug]";
    private final static Pattern LOG_LINE_REGEXP = Pattern.compile("([^\\t]+)\\t([^\\t]+)\\t(.*)");

    private final static double MIN_REWRITE_SIM = 0.5;

    private final static String RED_FONT = "\033[0;31m";
    private final static String GREEN_FONT = "\033[0;32m";
    private final static String NO_COLOR_FONT = "\033[0m";
    private final static String BOLD_FONT = "\033[1m";
    private final static String REGULAR_FONT = "\033[0m";

    private final static String TOKEN_SEPARATORS = "\\s+|=|:";

    public static void diff(String leftLogFile, String rightLogFile) throws IOException {
        JobDiffer d = new JobDiffer(leftLogFile, rightLogFile);
        d.launch();
    }

    public JobDiffer(String leftLogFile, String rightLogFile) {
        this.leftLogFile = leftLogFile;
        this.rightLogFile = rightLogFile;
        this.leftSteps = new LinkedHashMap<>();
        this.rightSteps = new LinkedHashMap<>();
    }

    public void launch() throws IOException {
        loadLog(leftLogFile, leftSteps);
        loadLog(rightLogFile, rightSteps);
        analyzeLeftSteps();
        analyzeRightSteps();
    }

    private void analyzeLeftSteps() {
        for (String leftStep : leftSteps.keySet()) {
            if (!rightSteps.containsKey(leftStep))
                System.out.println(RED_FONT + BOLD_FONT + "Deleted step [" + leftStep + "]" + REGULAR_FONT + NO_COLOR_FONT);
            else
                diffStep(leftStep);
        }
    }

    private void analyzeRightSteps() {
        for (String rightStep : rightSteps.keySet())
            if (!leftSteps.containsKey(rightStep))
                System.out.println(GREEN_FONT + BOLD_FONT + "Added step [" + rightStep + "]" + REGULAR_FONT + NO_COLOR_FONT);
    }

    public void diffStep(String step) {
        System.out.println(BOLD_FONT + "Diffing step [" + step + "]" + REGULAR_FONT);
        pruneSeeds(step);
        findActions(step);
    }

    private void findActions(String step) {
        final List<String> leftLines = leftSteps.get(step);
        final List<String> rightLines = rightSteps.get(step);
        Set<Integer> mappedLeftLines = new HashSet<>();
        Set<Integer> mappedRightLines = new HashSet<>();
        Map<Integer, Integer> mappings = new LinkedHashMap<>();
        for (int i = 0; i < leftLines.size(); i++) {
            for (int j = 0; j < rightLines.size(); j++) {
                final String leftLine = leftLines.get(i);
                final String rightLine = rightLines.get(j);
                final double sim = rewriteSim(leftLine, rightLine);
                if (sim >= MIN_REWRITE_SIM && !mappedRightLines.contains(j)) {
                    mappedLeftLines.add(i);
                    mappedRightLines.add(j);
                    mappings.put(i, j);
                    break;
                }
            }
        }

        for (int i = 0; i < leftLines.size(); i++) {
            if (!mappedLeftLines.contains(i)) {
                final String output = String.format("%s\tDel (%d): %s%s", GREEN_FONT, i, leftLines.get(i), NO_COLOR_FONT);
                System.out.println(output);
            }
        }

        for (int i = 0; i < rightLines.size(); i++) {
            if (!mappedRightLines.contains(i)) {
                final String output = String.format("%s\tAdd (%d): %s%s", RED_FONT, i, rightLines.get(i), NO_COLOR_FONT);
                System.out.println(output);
            }
        }

        for (int i : mappings.keySet()) {
            final String leftOutput = String.format("\tUpd (%d): %s", i, leftLines.get(i));
            System.out.println(leftOutput);
            int j = mappings.get(i);
            final String rightOutput = String.format("\t    (%d): %s", j, rightLines.get(j));
            System.out.println(rightOutput);
        }
    }

    private void pruneSeeds(String step) {
        final List<String> leftLines = leftSteps.get(step);
        final List<String> rightLines = rightSteps.get(step);
        Iterator<String> leftLinesIt = leftLines.iterator();
        while (leftLinesIt.hasNext()) {
            final String leftLine = leftLinesIt.next();
            Iterator<String> rightLinesIt = rightLines.iterator();
            while (rightLinesIt.hasNext()) {
                final String rightLine = rightLinesIt.next();
                if (leftLine.equals(rightLine)) {
                    leftLinesIt.remove();
                    rightLinesIt.remove();
                    break;
                }
            }
        }
    }

    static void loadLog(String logFile, Map<String, List<String>> logSteps) throws IOException {
        Files.lines(Paths.get(logFile)).forEach(
            line -> {
                final Matcher m = LOG_LINE_REGEXP.matcher(line);
                m.matches();
                // final String job = m.group(1);
                final String step = m.group(2);
                    String content = m.group(3);
                if (content.length() < 29)
                    return;
                
                content = content.substring(29);
                if (!content.startsWith(GITHUB_ACTION_DEBUG)) {
                    logSteps.putIfAbsent(step, new ArrayList<>());
                    logSteps.get(step).add(content);
                }
            }
        );
    }

    static double rewriteSim(String leftLine, String rightLine) {
        final String[] leftTokens = leftLine.split(TOKEN_SEPARATORS);
        final String[] rightTokens = rightLine.split(TOKEN_SEPARATORS);
        
        if (leftTokens.length != rightTokens.length)
            return 0.0;
        
        int dist = 0;
        for (int i = 0; i < leftTokens.length; i++)
            if (!leftTokens[i].equals(rightTokens[i]))
                dist++;

        return (double) (leftTokens.length - dist) / (double) leftTokens.length;
    }
}
