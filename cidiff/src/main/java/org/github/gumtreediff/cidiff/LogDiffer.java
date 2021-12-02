package org.github.gumtreediff.cidiff;

import java.util.List;
import java.util.Properties;

public class LogDiffer {
    final LogParser parser;
    final Properties options;
    final boolean displayUpdated;
    final boolean displayAdded;
    final boolean displayDeleted;

    final static String RED_FONT = "\033[0;31m";
    final static String GREEN_FONT = "\033[0;32m";
    final static String NO_COLOR_FONT = "\033[0m";
    final static String BOLD_FONT = "\033[1m";
    final static String REGULAR_FONT = "\033[0m";

    public LogDiffer(String leftLogFile, String rightLogFile, Properties options) {
        this.options = options;
        this.parser = LogParser.getParser(leftLogFile, rightLogFile, options);
        this.displayUpdated = Boolean.valueOf(options.getProperty(Options.DIFFER_UPDATED, "false"));
        this.displayAdded = Boolean.valueOf(options.getProperty(Options.DIFFER_ADDED, "true"));
        this.displayDeleted = Boolean.valueOf(options.getProperty(Options.DIFFER_DELETED, "true"));
        diff();
    }

    private void diff() {
        analyzeLeftSteps();
        analyzeRightSteps();
    }

    private void analyzeLeftSteps() {
        for (String leftStep : parser.leftSteps.keySet()) {
            if (!parser.rightSteps.containsKey(leftStep))
                System.out.println(RED_FONT + BOLD_FONT + "Deleted step [" + leftStep + "]" + REGULAR_FONT + NO_COLOR_FONT);
            else
                diffStep(leftStep);
        }
    }

    private void analyzeRightSteps() {
        for (String rightStep : parser.rightSteps.keySet())
            if (!parser.leftSteps.containsKey(rightStep))
                System.out.println(GREEN_FONT + BOLD_FONT + "Added step [" + rightStep + "]" + REGULAR_FONT + NO_COLOR_FONT);
    }

    private void diffStep(String step) {
        System.out.println(BOLD_FONT + "Diffing step [" + step + "]" + REGULAR_FONT);
        final List<String> leftLines = parser.leftSteps.get(step);
        final List<String> rightLines = parser.rightSteps.get(step);
        StepDiffer logDiffer = new StepDiffer(leftLines, rightLines);
        final int maxLineNumberSize = Integer.toString(Math.max(logDiffer.leftActions.length, logDiffer.rightActions.length)).length();
        final String lineFormat = "%0" + maxLineNumberSize + "d";
        for (Action action : logDiffer.getLeftActions()) {
            if (action.type == Action.Type.UPDATED && displayUpdated) {
                final String leftlineNumber = String.format(lineFormat, action.leftLocation + 1);
                final String leftOutput = String.format("\t> %s %s", leftlineNumber, leftLines.get(action.leftLocation));
                System.out.println(leftOutput);
                final String rightlineNumber = String.format(lineFormat, action.rightLocation + 1);
                final String rightOutput = String.format("\t  %s %s", rightlineNumber, rightLines.get(action.rightLocation));
                System.out.println(rightOutput);
            }
            else if (action.type == Action.Type.DELETED && displayDeleted) {
                final String leftlineNumber = String.format(lineFormat, action.leftLocation + 1);
                final String output = String.format("%s\t- %s %s%s", RED_FONT, leftlineNumber,
                    leftLines.get(action.leftLocation), NO_COLOR_FONT);
                System.out.println(output);
            }
        }
        for (Action action : logDiffer.getRightActions()) {
            if (action.type == Action.Type.ADDED && displayAdded) {
                final String rightlineNumber = String.format(lineFormat, action.rightLocation + 1);
                final String output = String.format("%s\t+ %s %s%s", GREEN_FONT, rightlineNumber,
                    rightLines.get(action.rightLocation), NO_COLOR_FONT);
                System.out.println(output);
            }
        }
    }
}
