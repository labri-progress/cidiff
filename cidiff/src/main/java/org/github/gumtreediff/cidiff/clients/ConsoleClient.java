package org.github.gumtreediff.cidiff.clients;

import java.util.Properties;

import org.github.gumtreediff.cidiff.*;

public final class ConsoleClient extends AbstractDiffClient {
    static final String RED_FONT = "\033[0;31m";
    static final String GREEN_FONT = "\033[0;32m";
    static final String NO_COLOR_FONT = "\033[0m";
    static final String BOLD_FONT = "\033[0;1m";
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
        final int maxLineNumberSize = Integer.toString(Math.max(leftLines.get(leftLines.size() - 1).lineNumber,
                rightLines.get(rightLines.size() - 1).lineNumber)).length();
        final var lineFormat = "%0" + maxLineNumberSize + "d";
        var lastDisplayed = 0;
        for (LogLine leftLine : lines.left) {
            final var action = actions.left.get(leftLine);

            if (action.type == Action.Type.UPDATED) {
                if (displayUpdated) {
                    final boolean newLine = lastDisplayed != 0 && lastDisplayed != leftLine.lineNumber - 1;
                    if (newLine)
                        System.out.println();

                    lastDisplayed = leftLine.lineNumber;
                    final String[] leftTokens = Utils.split(action.leftLogLine);
                    final String[] rightTokens = Utils.split(action.rightLogLine);
                    final StringBuilder leftValue = new StringBuilder();
                    final StringBuilder rightValue = new StringBuilder();
                    for (int i = 0; i < leftTokens.length; i++) {
                        if (leftTokens[i].equals(rightTokens[i])) {
                            leftValue.append(leftTokens[i]);
                            rightValue.append(rightTokens[i]);
                        }
                        else {
                            leftValue.append(BOLD_FONT + leftTokens[i] + REGULAR_FONT);
                            rightValue.append(BOLD_FONT + rightTokens[i] + REGULAR_FONT);
                        }
                        leftValue.append(" ");
                        rightValue.append(" ");
                    }

                    final var leftLineNumber = String.format(lineFormat, action.leftLogLine.lineNumber);
                    final var leftOutput = String.format("\t> %s %s",
                            leftLineNumber, leftValue);
                    System.out.println(leftOutput);
                    final var rightLineNumber = String.format(lineFormat, action.rightLogLine.lineNumber);
                    final var rightOutput = String.format("\t  %s %s",
                            rightLineNumber, rightValue);
                    System.out.println(rightOutput);
                }
            }
            else if (action.type == Action.Type.UNCHANGED) {
                if (displayUnchanged) {
                    final boolean newLine = lastDisplayed != 0 && lastDisplayed != leftLine.lineNumber - 1;
                    if (newLine)
                        System.out.println();

                    lastDisplayed = leftLine.lineNumber;
                    final var leftLineNumber = String.format(lineFormat, action.leftLogLine.lineNumber);
                    final var leftOutput = String.format("\t= %s %s",
                            leftLineNumber, action.leftLogLine.value);
                    System.out.println(leftOutput);
                    final var rightLineNumber = String.format(lineFormat, action.rightLogLine.lineNumber);
                    final var rightOutput = String.format("\t  %s %s",
                            rightLineNumber, action.rightLogLine.value);
                    System.out.println(rightOutput);
                }
            }
            else if (action.type == Action.Type.DELETED) {
                if (displayDeleted) {
                    final boolean newLine = lastDisplayed != 0 && lastDisplayed != leftLine.lineNumber - 1;
                    if (newLine)
                        System.out.println();

                    lastDisplayed = leftLine.lineNumber;
                    final var leftLineNumber = String.format(lineFormat, action.leftLogLine.lineNumber);
                    final var output = String.format("%s\t- %s %s%s", RED_FONT, leftLineNumber,
                            action.leftLogLine.value, NO_COLOR_FONT);
                    System.out.println(output);
                }
            }
        }

        lastDisplayed = 0;
        for (LogLine rightLine : lines.right) {
            final var action = actions.right.get(rightLine);
            if (action.type == Action.Type.ADDED) {
                if (displayAdded) {
                    final boolean newLine = lastDisplayed == 0 || lastDisplayed != rightLine.lineNumber - 1;
                    if (newLine)
                        System.out.println();

                    lastDisplayed = rightLine.lineNumber;
                    final var rightlineNumber = String.format(lineFormat, action.rightLogLine.lineNumber);
                    final var output = String.format("%s\t+ %s %s%s", GREEN_FONT, rightlineNumber,
                            action.rightLogLine.value, NO_COLOR_FONT);
                    System.out.println(output);
                }
            }
        }
    }
}
