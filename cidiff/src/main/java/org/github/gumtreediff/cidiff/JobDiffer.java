package org.github.gumtreediff.cidiff;

import java.util.List;

import org.github.gumtreediff.cidiff.DiffInputProducer.Type;

public class JobDiffer {
    final DiffInputProducer input;
    final boolean displayUpdated = false;

    final static String RED_FONT = "\033[0;31m";
    final static String GREEN_FONT = "\033[0;32m";
    final static String NO_COLOR_FONT = "\033[0m";
    final static String BOLD_FONT = "\033[1m";
    final static String REGULAR_FONT = "\033[0m";

    public JobDiffer(String leftLogFile, String rightLogFile, DiffInputProducer.Type inputType) {
        if (inputType == Type.GITHUB)
            input = new DiffInputProducer.GithubDiffInputProducer(leftLogFile, rightLogFile);
        else
            input = new DiffInputProducer.ClassicDiffInputProducer(leftLogFile, rightLogFile);
        launch();
    }

    public void launch() {
        analyzeLeftSteps();
        analyzeRightSteps();
    }

    private void analyzeLeftSteps() {
        for (String leftStep : input.leftSteps.keySet()) {
            if (!input.rightSteps.containsKey(leftStep))
                System.out.println(RED_FONT + BOLD_FONT + "Deleted step [" + leftStep + "]" + REGULAR_FONT + NO_COLOR_FONT);
            else
                diffStep(leftStep);
        }
    }

    private void analyzeRightSteps() {
        for (String rightStep : input.rightSteps.keySet())
            if (!input.leftSteps.containsKey(rightStep))
                System.out.println(GREEN_FONT + BOLD_FONT + "Added step [" + rightStep + "]" + REGULAR_FONT + NO_COLOR_FONT);
    }

    public void diffStep(String step) {
        System.out.println(BOLD_FONT + "Diffing step [" + step + "]" + REGULAR_FONT);
        final List<String> leftLines = input.leftSteps.get(step);
        final List<String> rightLines = input.rightSteps.get(step);
        LogDiffer logDiffer = new LogDiffer(leftLines, rightLines);
        for (Action action : logDiffer.getLeftActions()) {
            if (action.type == ActionType.UPDATED && displayUpdated) {
                final String leftOutput = String.format("\tUpd (%d): %s", action.leftLocation + 1, leftLines.get(action.leftLocation));
                System.out.println(leftOutput);
                final String rightOutput = String.format("\t    (%d): %s", action.rightLocation + 1, rightLines.get(action.rightLocation));
                System.out.println(rightOutput);
            }
            else if (action.type == ActionType.DELETED) {
                final String output = String.format("%s\tDel (%d): %s%s", RED_FONT, action.leftLocation + 1,
                    leftLines.get(action.leftLocation), NO_COLOR_FONT);
                System.out.println(output);
            }
        }
        for (Action action : logDiffer.getRightActions()) {
            if (action.type == ActionType.ADDED) {
                final String output = String.format("%s\tAdd (%d): %s%s", GREEN_FONT, action.rightLocation + 1,
                    rightLines.get(action.rightLocation), NO_COLOR_FONT);
                System.out.println(output);
            }
        }
    }
}
