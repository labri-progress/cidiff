package org.github.gumtreediff.cidiff.benchmarks;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

import org.github.gumtreediff.cidiff.*;

public final class PrecisionBenchmark {
    private static final String HEADER = "LEFT;RIGHT;"
            + "ALGORITHM;"
            + "PRECISION;"
            + "RECALL;"
            + "FSCORE;"
            + "TIME";
    private static String pathToData = "data/breakages/";
    private static String pathToOutput = "benchmark/precision_recall.csv";

    private PrecisionBenchmark() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length == 2) {
            pathToData = args[0];
            pathToOutput = args[1];
        }

        final FileWriter csv = new FileWriter(pathToOutput);
        csv.append(HEADER).append("\n");

        final File breakagesFolder = new File(pathToData);
        final File[] casesFolders = breakagesFolder.listFiles(File::isDirectory);
        Objects.requireNonNull(casesFolders);
        for (File oneCaseFolder: casesFolders) {
            System.out.println(oneCaseFolder);
            final String left = oneCaseFolder.toPath().resolve("pass.log").toString();
            final String right = oneCaseFolder.toPath().resolve("fail.log").toString();
            final String groundtruth = oneCaseFolder.toPath().resolve("groundtruth").toString();
            final String parser = getParser(groundtruth);

            final Properties options = new Properties();
            options.setProperty(Options.PARSER, parser);
            final Pair<String> logs = new Pair<>(left, right);

            for (LogDiffer.Algorithm algorithm : LogDiffer.Algorithm.values()) {
                final StringBuilder b = new StringBuilder();
                b.append(left).append(";").append(right).append(";").append(algorithm);
                final LogDiffer differ = LogDiffer.get(LogDiffer.Algorithm.valueOf(
                    options.getProperty(Options.DIFFER, String.valueOf(algorithm))), options);
                final Pair<List<LogLine>> lines = LogParser.parseLogs(logs, options);
                final long start = System.currentTimeMillis();
                final Pair<Action[]> actions = differ.diff(lines);
                final long stop = System.currentTimeMillis();

                final List<int[]> intervalsCidiff = getIntervalsCidiff(actions);
                final List<int[]> intervalsGroundtruth = getIntervalsGroundtruth(groundtruth, lines.right);

                final float truePositive = getIntersections(intervalsCidiff, intervalsGroundtruth);
                final float falseNegative = getComplements(intervalsGroundtruth, truePositive);
                final float falsePositive = getComplements(intervalsCidiff, truePositive);

                final float precision = truePositive / (truePositive + falsePositive);
                final float recall = truePositive / (truePositive + falseNegative);
                final float fscore = (2 * precision * recall) / (precision + recall);

                b.append(";").append(precision).append(";").append(recall);
                b.append(";").append(fscore).append(";").append(stop - start);
                csv.append(b).append("\n");
            }
        }
        csv.close();
    }

    private static String getParser(String groundtruth) throws Exception {
        final BufferedReader br = new BufferedReader(new FileReader(groundtruth));
        final String parser = br.readLine();
        br.close();
        return parser;
    }

    private static List<int[]> getIntervalsCidiff(Pair<Action[]> actions) {
        int start = -1;
        final List<int[]> intervals = new ArrayList<>();

        for (Action a: actions.right) {
            if (a.type == Action.Type.ADDED && start == -1) {
                start = a.rightLocation;
            }
            else if (a.type != Action.Type.ADDED && start != -1) {
                intervals.add(new int[]{start, a.rightLocation});
                start = -1;
            }
            else if (a.type == Action.Type.ADDED && a.rightLocation == actions.right.length - 1) {
                intervals.add(new int[] {start, a.rightLocation + 1});
                start = -1;
            }
        }
        return intervals;
    }

    private static List<int[]> getIntervalsGroundtruth(String groundtruth, List<LogLine> lines) throws Exception {
        final List<int[]> intervals = new ArrayList<>();
        final BufferedReader br = new BufferedReader(new FileReader(groundtruth));
        String oneLine;

        final HashMap<Integer, Integer> logHashMap = new HashMap<Integer, Integer>();
        for (int i = 0; i < lines.size(); i++) {
            logHashMap.put(lines.get(i).lineNumber, i);
        }

        while ((oneLine = br.readLine()) != null) {
            if (oneLine.charAt(0) == 'E' || oneLine.charAt(0) == 'C') {
                final String[] errors = oneLine.substring(2).split("-");
                final int l = Integer.parseInt(errors[0]);
                final int r;
                if (errors.length > 1) {
                    r = Integer.parseInt(errors[1]);
                }
                else {
                    r = l;
                }
                intervals.add(new int[] {logHashMap.get(l), logHashMap.get(r) + 1});
            }
        }
        br.close();
        return intervals;
    }

    private static float getIntersections(List<int[]> intervalsCidiff, List<int[]> intervalsGroundtruth) {
        float truePositive = 0;
        int i = 0;
        int j = 0;

        while (i < intervalsCidiff.size() && j < intervalsGroundtruth.size()) {
            final int l = Math.max(intervalsCidiff.get(i)[0], intervalsGroundtruth.get(j)[0]);
            final int r = Math.min(intervalsCidiff.get(i)[1], intervalsGroundtruth.get(j)[1]);

            if (l <= r) {
                truePositive += r - l;
            }

            if (intervalsCidiff.get(i)[1] < intervalsGroundtruth.get(j)[1])
                i += 1;
            else
                j += 1;
        }
        return truePositive;
    }

    private static float getComplements(List<int[]> intervals, float truePositive) {
        float complement = 0;
        for (int[] r: intervals)
            complement += r[1] - r[0];
        return complement - truePositive;
    }
}
