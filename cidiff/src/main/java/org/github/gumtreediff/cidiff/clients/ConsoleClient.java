package org.github.gumtreediff.cidiff.clients;

import java.util.Properties;

import org.github.gumtreediff.cidiff.Action;
import org.github.gumtreediff.cidiff.Options;
import org.github.gumtreediff.cidiff.Pair;

public final class ConsoleClient extends AbstractDiffClient {
    static final String RED_FONT = "\033[0;31m";
    static final String GREEN_FONT = "\033[0;32m";
    static final String NO_COLOR_FONT = "\033[0m";
    static final String BOLD_FONT = "\033[1m";
    static final String REGULAR_FONT = "\033[0m";

    final boolean displayUpdated;
    final boolean displayUnchanged;
    final boolean displayAdded;
    final boolean displayDeleted;

    public ConsoleClient(String leftFile, String rightFile, Properties options) {
        super(leftFile, rightFile, options);
        this.displayUpdated = Boolean.parseBoolean(options.getProperty(Options.CONSOLE_UPDATED, "false"));
        this.displayUnchanged = Boolean.parseBoolean(options.getProperty(Options.CONSOLE_UNCHANGED, "false"));
        this.displayAdded = Boolean.parseBoolean(options.getProperty(Options.CONSOLE_ADDED, "true"));
        this.displayDeleted = Boolean.parseBoolean(options.getProperty(Options.CONSOLE_DELETED, "true"));
    }

    public void execute() {
        final var leftLines = lines.left;
        final var rightLines = lines.right;
        final var actions = differ.diff(new Pair<>(leftLines, rightLines));
        final int maxLineNumberSize = Integer.toString(Math.max(actions.left.length,
                actions.right.length)).length();
        final var lineFormat = "%0" + maxLineNumberSize + "d";
        var lastDisplayed = 0;
        for (int i = 0; i < actions.left.length; i++) {
            final var action = actions.left[i];

            if (action.type == Action.Type.UPDATED) {
                if (displayUpdated) {
                    final boolean newLine = lastDisplayed != 0 && lastDisplayed != i - 1;
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
                if (displayUnchanged) {
                    final boolean newLine = lastDisplayed != 0 && lastDisplayed != i - 1;
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
                if (displayDeleted) {
                    final boolean newLine = lastDisplayed != 0 && lastDisplayed != i - 1;
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
                if (displayAdded) {
                    final boolean newLine = lastDisplayed == 0 || lastDisplayed != i - 1;
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
