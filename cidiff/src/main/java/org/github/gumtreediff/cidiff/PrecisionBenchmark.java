package org.github.gumtreediff.cidiff;

import java.io.File;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class PrecisionBenchmark {
    private final static String HEADER1 = " ; ;ALTERNATING_BRUTE_FORCE;ALTERNATING_BRUTE_FORCE;BRUTE_FORCE;BRUTE_FORCE;LCS;LCS;SEED_EXTEND;SEED_EXTEND";
    private final static String HEADER2 = "left;right;precision;recall;precision;recall;precision;recall;precision;recall";
    private final static String PATH_TO_DATA = "data/breakages/";
    private final static String PATH_TO_OUTPUT = "benchmark/precision_recall.csv";

    public static void main(String[] args) throws Exception {
        FileWriter csv = new FileWriter(PATH_TO_OUTPUT);
        csv.append(HEADER1 + "\n");
        csv.append(HEADER2 + "\n");

        File breakagesFolder = new File(PATH_TO_DATA);
        File[] casesFolders = breakagesFolder.listFiles(File::isDirectory);

        for(File oneCaseFolder: casesFolders) {
            System.out.println(oneCaseFolder);
            File oneCase = new File(oneCaseFolder.getAbsolutePath());
            File[] files = oneCase.listFiles();

            String left = files[0].getAbsolutePath();
            String right = files[1].getAbsolutePath();
            String groundtruth = files[2].getAbsolutePath();
            String parser = getParser(groundtruth);

            Properties options = new Properties();
            options.setProperty(Options.PARSER, parser);
            Pair<String> logs = new Pair<>(left, right);
            String csv_string = left+";"+right+";";

            for (LogDiffer.Algorithm algorithm : LogDiffer.Algorithm.values()) { 

                LogDiffer differ = LogDiffer.get(LogDiffer.Algorithm.valueOf(
                    options.getProperty(Options.DIFFER, String.valueOf(algorithm))), options);
                Pair<List<String>> lines = LogParser.parseLogs(logs, options);
                Pair<Action[]> actions = differ.diff(lines);
                
                List<int[]> intervalsCidiff = getIntervalsCidiff(actions);
                List<int[]> intervalsGroundtruth = getIntervalsGroundtruth(groundtruth);

                float truePositive = getIntersections(intervalsCidiff,intervalsGroundtruth);
                float falseNegative = getComplements(intervalsGroundtruth, truePositive);
                float falsePositive = getComplements(intervalsCidiff, truePositive);
                
                float precision = truePositive/(truePositive+falsePositive);
                float recall = truePositive/(truePositive+falseNegative);

                csv_string += precision+";"+recall+";";
            }
            csv.append(csv_string+"\n");

        }
        csv.close();
    }

    private static String getParser(String groundtruth) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(groundtruth));
        String parser = br.readLine();
        br.close();
        return parser;
    }

    private static List<int[]> getIntervalsCidiff(Pair<Action[]> actions) {
        int start = -1;
        List<int[]> intervals = new ArrayList<int[]>();

        for(Action a: actions.right) {
            if (a.type == Action.Type.ADDED && start == -1) {
                start = a.rightLocation;
            }
            else if (a.type != Action.Type.ADDED && start != -1){
                intervals.add(new int[]{start,a.rightLocation});
                start = -1;
            }
            else if (a.type == Action.Type.ADDED && a.rightLocation == actions.right.length-1) {
                intervals.add(new int[]{start,a.rightLocation+1});
                start = -1;
            }
        }
        return intervals;
    }

    private static List<int[]> getIntervalsGroundtruth(String groundtruth) throws Exception {
        List<int[]> intervals = new ArrayList<int[]>();
        BufferedReader br = new BufferedReader(new FileReader(groundtruth));
        String oneLine;

        while ((oneLine = br.readLine()) != null) {
            if (oneLine.charAt(0) == 'E' || oneLine.charAt(0) == 'C') {
                String[] errors = oneLine.substring(2).split("-");
                int l = Integer.parseInt(errors[0]);
                int r;
                if (errors.length > 1) {
                    r = Integer.parseInt(errors[1])+1;
                }
                else {
                    r = l+1;
                }
                intervals.add(new int[]{l,r});
            }
        }
        br.close();
        return intervals;
    }

    private static float getIntersections(List<int[]> intervalsCidiff, List<int[]> intervalsGroundtruth){
        float truePositive = 0;
        int i = 0, j = 0;

        while (i < intervalsCidiff.size() && j < intervalsGroundtruth.size()) {
            int l = Math.max(intervalsCidiff.get(i)[0], intervalsGroundtruth.get(j)[0]);
            int r = Math.min(intervalsCidiff.get(i)[1], intervalsGroundtruth.get(j)[1]);

            if (l <= r) {
                truePositive += r-l;
            }

            if (intervalsCidiff.get(i)[1] < intervalsGroundtruth.get(j)[1])
                i += 1;
            else
                j += 1;
        }
        return truePositive;
    }

    private static float getComplements(List<int[]> intervals, float truePositive){
        float complement = 0;
        for(int[] r: intervals)
            complement += r[1] - r[0];
        return complement - truePositive;
    }
}
