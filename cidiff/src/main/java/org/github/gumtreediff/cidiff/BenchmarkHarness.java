package org.github.gumtreediff.cidiff;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class BenchmarkHarness {
    private final static int RUNS = 5;
    private final static String HEADER = "left;right;config;t1;t2;t3;t4;t5;unchanged;updated;deleted;added";

    public static void main(String[] args) throws IOException {
        FileWriter w = new FileWriter("benchmark/results.csv");
        w.append(HEADER + "\n");
        File dir = new File("data");
        File[] files = dir.listFiles((d, name) -> name.matches("cidiff_.*\\.log\\.csv"));
        Arrays.sort(files);
        runBruteForce(files, w);
        runLcs(files, w);
        runSeedExtend(files, w);
        w.close();
    }

    private static List<String> run(String left, String right, Properties options, String config) {
        LogDifferCli d = null;
        options.setProperty(Options.PARSER, "GITHUB");
        options.setProperty(Options.DIFFER_DELETED, "false");
        options.setProperty(Options.DIFFER_ADDED, "false");
        options.setProperty(Options.DIFFER_UPDATED, "false");
        options.setProperty(Options.DIFFER_UNCHANGED, "false");
        List<String> results = new ArrayList<>();
        results.add(left);
        results.add(right);
        results.add(config);
        for(int i = 0; i < RUNS; i++) {
            long tStart = System.currentTimeMillis();
            d = new LogDifferCli(left, right, options);
            d.diff();
            long tEnd= System.currentTimeMillis();
            results.add(Long.toString(tEnd - tStart));
        }

        results.add(Integer.toString(d.getMetrics().unchanged));
        results.add(Integer.toString(d.getMetrics().updated));
        results.add(Integer.toString(d.getMetrics().deleted));
        results.add(Integer.toString(d.getMetrics().added));

        return results;
    }

    private static void runBruteForce(File[] files, FileWriter w) throws IOException {
        for(int i = 0; i < files.length - 2; i++) {
            String left = files[i].getAbsolutePath();
            String right = files[i + 1].getAbsolutePath();
            Properties options = new Properties();
            List<String> results = run(left, right, options, "bruteforce");
            w.append(String.join(";", results) + "\n");
        }
    }

    private static void runLcs(File[] files, FileWriter w) throws IOException {
        for(int i = 0; i < files.length - 2; i++) {
            String left = files[i].getAbsolutePath();
            String right = files[i + 1].getAbsolutePath();
            Properties options = new Properties();
            options.setProperty(Options.DIFFER, "LCS");
            List<String> results = run(left, right, options, "lcs");
            w.append(String.join(";", results) + "\n");
        }
    }

    private static void runSeedExtend(File[] files, FileWriter w) throws IOException {
        for(int i = 0; i < files.length - 2; i++) {
            String left = files[i].getAbsolutePath();
            String right = files[i + 1].getAbsolutePath();
            Properties options = new Properties();
            options.setProperty(Options.DIFFER, "SEED_EXTEND");
            List<String> results = run(left, right, options, "seed_extend");
            w.append(String.join(";", results) + "\n");
        }
    }
}
