package org.github.gumtreediff.cidiff;

import java.util.Properties;

public class LogDiffer {
    final LogParser parser;
    final StepDiffer differ;
    final Properties options;
    final boolean displayUpdated;
    final boolean displayUnchanged;
    final boolean displayAdded;
    final boolean displayDeleted;

    final static String DEFAULT_DIFFER = "BRUTE_FORCE";

    final static String RED_FONT = "\033[0;31m";
    final static String GREEN_FONT = "\033[0;32m";
    final static String NO_COLOR_FONT = "\033[0m";
    final static String BOLD_FONT = "\033[1m";
    final static String REGULAR_FONT = "\033[0m";

    Metrics metrics;

    public LogDiffer(String leftLogFile, String rightLogFile, Properties options) {
        this.options = options;
        this.differ = StepDiffer.get(StepDiffer.Algorithm.valueOf(
                options.getProperty(Options.DIFFER, DEFAULT_DIFFER)), options);
        this.parser = LogParser.get(leftLogFile, rightLogFile, options);
        this.parser.parse();
        this.displayUpdated = Boolean.parseBoolean(options.getProperty(Options.DIFFER_UPDATED, "false"));
        this.displayUnchanged = Boolean.parseBoolean(options.getProperty(Options.DIFFER_UNCHANGED, "false"));
        this.displayAdded = Boolean.parseBoolean(options.getProperty(Options.DIFFER_ADDED, "true"));
        this.displayDeleted = Boolean.parseBoolean(options.getProperty(Options.DIFFER_DELETED, "true"));
        diff();
    }

    public void diff() {
        metrics = new Metrics();
        analyzeLeftSteps();
        analyzeRightSteps();
    }

    public Metrics getMetrics() {
        return metrics;
    }

    private void analyzeLeftSteps() {
        for (String leftStep : parser.steps.left.keySet()) {
            if (!parser.steps.right.containsKey(leftStep)) {
                metrics.deleted += parser.steps.left.get(leftStep).size();
                System.out.println(RED_FONT + BOLD_FONT
                        + "Deleted step [" + leftStep + "]" + REGULAR_FONT + NO_COLOR_FONT);
            }
            else
                diffStep(leftStep);
        }
    }

    private void analyzeRightSteps() {
        for (String rightStep : parser.steps.right.keySet())
            if (!parser.steps.left.containsKey(rightStep)) {
                metrics.deleted += parser.steps.left.get(rightStep).size();
                System.out.println(GREEN_FONT + BOLD_FONT
                        + "Added step [" + rightStep + "]" + REGULAR_FONT + NO_COLOR_FONT);
            }
    }

    private void diffStep(String step) {
        System.out.println(BOLD_FONT + "Diffing step [" + step + "]" + REGULAR_FONT);
        final var leftLines = parser.steps.left.get(step);
        final var rightLines = parser.steps.right.get(step);
        final var actions = differ.diffStep(new Pair<>(leftLines, rightLines));
        final int maxLineNumberSize = Integer.toString(Math.max(actions.left.length,
                actions.right.length)).length();
        final var lineFormat = "%0" + maxLineNumberSize + "d";
        var lastDisplayed = 0;
        for (int i = 0; i < actions.left.length; i++) {
            final var action = actions.left[i];

            if (action.type == Action.Type.UPDATED) {
                metrics.updated++;
                if (displayUpdated) {
                    boolean newLine = lastDisplayed != 0 && lastDisplayed != i - 1;
                    if (newLine)
                        System.out.println();

                    lastDisplayed = i;
                    final var leftLineNumber = String.format(lineFormat, action.leftLocation + 1);
                    final var leftOutput = String.format("\t> %s %s",
                            leftLineNumber, leftLines.get(action.leftLocation));
                    System.out.println(leftOutput);
                    final var rightLineNumber = String.format(lineFormat, action.rightLocation + 1);
                    final var rightOutput = String.format("\t  %s %s",
                            rightLineNumber, rightLines.get(action.rightLocation));
                    System.out.println(rightOutput);
                }
            }
            else if (action.type == Action.Type.UNCHANGED) {
                metrics.unchanged++;
                if (displayUnchanged) {
                    boolean newLine = lastDisplayed != 0 && lastDisplayed != i - 1;
                    if (newLine)
                        System.out.println();

                    lastDisplayed = i;
                    final var leftLineNumber = String.format(lineFormat, action.leftLocation + 1);
                    final var leftOutput = String.format("\t= %s %s",
                            leftLineNumber, leftLines.get(action.leftLocation));
                    System.out.println(leftOutput);
                    final var rightLineNumber = String.format(lineFormat, action.rightLocation + 1);
                    final var rightOutput = String.format("\t  %s %s",
                            rightLineNumber, rightLines.get(action.rightLocation));
                    System.out.println(rightOutput);
                }
            }
            else if (action.type == Action.Type.DELETED) {
                metrics.deleted++;
                if (displayDeleted) {
                    boolean newLine = lastDisplayed != 0 && lastDisplayed != i - 1;
                    if (newLine)
                        System.out.println();

                    lastDisplayed = i;
                    final var leftLineNumber = String.format(lineFormat, action.leftLocation + 1);
                    final var output = String.format("%s\t- %s %s%s", RED_FONT, leftLineNumber,
                            leftLines.get(action.leftLocation), NO_COLOR_FONT);
                    System.out.println(output);
                }
            }
        }

        lastDisplayed = 0;
        for (int i = 0; i < actions.right.length; i++) {
            final var action = actions.right[i];
            if (action.type == Action.Type.ADDED) {
                metrics.added++;
                if (displayAdded) {
                    boolean newLine = lastDisplayed == 0 || lastDisplayed != i - 1;
                    if (newLine)
                        System.out.println();

                    lastDisplayed = i;
                    final var rightlineNumber = String.format(lineFormat, action.rightLocation + 1);
                    final var output = String.format("%s\t+ %s %s%s", GREEN_FONT, rightlineNumber,
                            rightLines.get(action.rightLocation), NO_COLOR_FONT);
                    System.out.println(output);
                }
            }
        }
    }

    public static final class Metrics {
        public int added;
        public int deleted;
        public int updated;
        public int unchanged;
    }
}
