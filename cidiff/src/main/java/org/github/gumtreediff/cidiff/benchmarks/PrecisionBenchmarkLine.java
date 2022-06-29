package org.github.gumtreediff.cidiff.benchmarks;

import java.io.*;
import java.util.*;

import org.github.gumtreediff.cidiff.*;

public final class PrecisionBenchmarkLine {
    private static final String HEADER = "LEFT;"
            + "RIGHT;"
            + "ALGORITHM;"
            + "PRECISION;"
            + "RECALL;"
            + "FSCORE;"
            + "TIME"
            + "\n";
    private static String pathToData = "data/breakages/";
    private static String pathToOutput = "benchmark/precision_recall.csv";

    private PrecisionBenchmarkLine() {
    }

    public static void main(String[] args) throws IOException {
        if (args.length == 2) {
            pathToData = args[0];
            pathToOutput = args[1];
        }

        final FileWriter csv = new FileWriter(pathToOutput);
        csv.append(HEADER);

        final File breakagesFolder = new File(pathToData);
        final File[] casesFolders = breakagesFolder.listFiles(File::isDirectory);
        Objects.requireNonNull(casesFolders);
        for (File caseFolder: casesFolders) {
            System.out.println(caseFolder);
            final String left = caseFolder.toPath().resolve("pass.log").toString();
            final String right = caseFolder.toPath().resolve("fail.log").toString();
            final String groundtruth = caseFolder.toPath().resolve("groundtruth").toString();

            final GroundtruthContent groundtruthContent = GroundtruthContent.fromFile(groundtruth);
            final Properties options = new Properties();
            options.setProperty(Options.PARSER, groundtruthContent.parser());
            final Pair<String> logFiles = new Pair<>(left, right);
            final Pair<List<LogLine>> logs = LogParser.parseLogs(logFiles, options);
            for (LogDiffer.Algorithm algorithm : LogDiffer.Algorithm.values()) {
                final LogDiffer differ = LogDiffer.get(algorithm, options);
                final long start = System.currentTimeMillis();
                final Pair<Action[]> actions = differ.diff(logs);
                final long stop = System.currentTimeMillis();

                final Set<Integer> linesCiDiff = getAddedLines(actions.right, logs.right);
                final Set<Integer> linesGroundtruth = groundtruthContent.lines();

                final Set<Integer> truePositives = new HashSet<>(linesCiDiff);
                truePositives.retainAll(linesGroundtruth);

                final Set<Integer> falseNegatives = new HashSet<>(linesGroundtruth);
                falseNegatives.removeAll(linesCiDiff);

                final Set<Integer> falsePositives = new HashSet<>(linesCiDiff);
                falsePositives.removeAll(linesGroundtruth);

                final double precision = (double) truePositives.size()
                        / (double) (truePositives.size() + falsePositives.size());
                final double recall = (double) truePositives.size()
                        / (double) (truePositives.size() + falseNegatives.size());
                final double fscore = (2D * precision * recall) / (precision + recall);

                final String csvLine = String.format(Locale.US, "%s;%s;%s;%.02f;%.02f;%.02f;%d\n",
                        left, right, algorithm, precision, recall, fscore, stop - start);

                csv.append(csvLine);
            }
        }
        csv.close();
    }

    public static Set<Integer> getAddedLines(Action[] rightActions, List<LogLine> rightLog) {
        final Set<Integer> lines = new HashSet<>();
        for (int i = 0; i < rightActions.length; i++)
            if (rightActions[i].type == Action.Type.ADDED)
                lines.add(rightLog.get(i).lineNumber);

        return lines;
    }

    public record GroundtruthContent(String parser, Set<Integer> lines) {
        static GroundtruthContent fromFile(String file) throws IOException {
            final BufferedReader br = new BufferedReader(new FileReader(file));
            final String parser = br.readLine();
            final Set<Integer> lines = new HashSet<>();
            String oneLine;
            while ((oneLine = br.readLine()) != null) {
                if (oneLine.charAt(0) == 'E' || oneLine.charAt(0) == 'C') {
                    final String[] errors = oneLine.substring(2).split("-");
                    if (errors.length == 1)
                        lines.add(Integer.parseInt(errors[0]));
                    else
                        for (int i = Integer.parseInt(errors[0]); i <= Integer.parseInt(errors[1]); i = i + 1)
                            lines.add(i);
                }
            }
            br.close();
            return new GroundtruthContent(parser, lines);
        }
    }
}
