package org.github.gumtreediff.cidiff.benchmarks;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

import org.github.gumtreediff.cidiff.*;

public final class PrecisionBenchmark {
    private static final String HEADER = "LEFT;RIGHT;"
            + "ALTERNATING_BRUTE_FORCE_P;ALTERNATING_BRUTE_FORCE_R;ALTERNATING_BRUTE_FORCE_T;"
            + "BRUTE_FORCE_P;BRUTE_FORCE_R;BRUTE_FORCE_T;"
            + "LCS_P;LCS_R;LCS_T;"
            + "SEED_EXTEND_P;SEED_EXTEND_R;SEED_EXTEND_T";
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
        csv.append(HEADER + "\n");

        final File breakagesFolder = new File(pathToData);
        final File[] casesFolders = breakagesFolder.listFiles(File::isDirectory);
        for (File oneCaseFolder: casesFolders) {
            System.out.println(oneCaseFolder);
            final File oneCase = new File(oneCaseFolder.getAbsolutePath());
            final File[] files = oneCase.listFiles();
            final String left = Objects.requireNonNull(files)[0].getAbsolutePath();
            final String right = files[1].getAbsolutePath();
            final String groundtruth = files[2].getAbsolutePath();
            final String parser = getParser(groundtruth);

            final Properties options = new Properties();
            options.setProperty(Options.PARSER, parser);
            final Pair<String> logs = new Pair<>(left, right);
            final StringBuilder b = new StringBuilder();
            b.append(left + ";" + right);
            for (LogDiffer.Algorithm algorithm : LogDiffer.Algorithm.values()) {
                final LogDiffer differ = LogDiffer.get(LogDiffer.Algorithm.valueOf(
                    options.getProperty(Options.DIFFER, String.valueOf(algorithm))), options);
                final Pair<List<String>> lines = LogParser.parseLogs(logs, options);
                final long start = System.currentTimeMillis();
                final Pair<Action[]> actions = differ.diff(lines);
                final long stop = System.currentTimeMillis();

                final List<int[]> intervalsCidiff = getIntervalsCidiff(actions);
                final List<int[]> intervalsGroundtruth = getIntervalsGroundtruth(groundtruth);

                final float truePositive = getIntersections(intervalsCidiff, intervalsGroundtruth);
                final float falseNegative = getComplements(intervalsGroundtruth, truePositive);
                final float falsePositive = getComplements(intervalsCidiff, truePositive);

                final float precision = truePositive / (truePositive + falsePositive);
                final float recall = truePositive / (truePositive + falseNegative);

                b.append(";" + precision + ";" + recall + ";" + (stop - start));
            }
            csv.append(b + "\n");
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

    private static List<int[]> getIntervalsGroundtruth(String groundtruth) throws Exception {
        final List<int[]> intervals = new ArrayList<>();
        final BufferedReader br = new BufferedReader(new FileReader(groundtruth));
        String oneLine;

        while ((oneLine = br.readLine()) != null) {
            if (oneLine.charAt(0) == 'E' || oneLine.charAt(0) == 'C') {
                final String[] errors = oneLine.substring(2).split("-");
                final int l = Integer.parseInt(errors[0]);
                final int r;
                if (errors.length > 1) {
                    r = Integer.parseInt(errors[1]) + 1;
                }
                else {
                    r = l + 1;
                }
                intervals.add(new int[] {l, r});
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
