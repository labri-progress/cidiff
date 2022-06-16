package org.github.gumtreediff.cidiff.benchmarks;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.github.gumtreediff.cidiff.Options;
import org.github.gumtreediff.cidiff.clients.ConsoleClient;

public final class PerformanceBenchmark {
    private static final int RUNS = 5;
    private static final String HEADER =
            "left;right;config;t1;t2;t3;t4;t5;unchanged;updated;deleted;added";

    private PerformanceBenchmark() {
    }

    public static void main(String[] args) throws IOException {
        final FileWriter w = new FileWriter("benchmark/results.csv");
        w.append(HEADER + "\n");
        final File dir = new File("data");
        final File[] files = dir.listFiles((d, name) -> name.matches("cidiff_.*\\.log\\.csv"));
        Arrays.sort(files);
        runBruteForce(files, w);
        runLcs(files, w);
        runSeedExtend(files, w);
        w.close();
    }

    private static List<String> run(String left, String right, Properties options, String config) {
        ConsoleClient d = null;
        options.setProperty(Options.PARSER, "GITHUB");
        options.setProperty(Options.CONSOLE_DELETED, "false");
        options.setProperty(Options.CONSOLE_ADDED, "false");
        options.setProperty(Options.CONSOLE_UPDATED, "false");
        options.setProperty(Options.CONSOLE_UNCHANGED, "false");
        final List<String> results = new ArrayList<>();
        results.add(left);
        results.add(right);
        results.add(config);
        for (int i = 0; i < RUNS; i++) {
            final long tStart = System.currentTimeMillis();
            d = new ConsoleClient(left, right, options);
            d.execute();
            final long tEnd = System.currentTimeMillis();
            results.add(Long.toString(tEnd - tStart));
        }

        return results;
    }

    private static void runBruteForce(File[] files, FileWriter writer) throws IOException {
        for (int i = 0; i < files.length - 2; i++) {
            final String left = files[i].getAbsolutePath();
            final String right = files[i + 1].getAbsolutePath();
            final Properties options = new Properties();
            final List<String> results = run(left, right, options, "bruteforce");
            writer.append(String.join(";", results) + "\n");
        }
    }

    private static void runLcs(File[] files, FileWriter writer) throws IOException {
        for (int i = 0; i < files.length - 2; i++) {
            final String left = files[i].getAbsolutePath();
            final String right = files[i + 1].getAbsolutePath();
            final Properties options = new Properties();
            options.setProperty(Options.DIFFER, "LCS");
            final List<String> results = run(left, right, options, "lcs");
            writer.append(String.join(";", results) + "\n");
        }
    }

    private static void runSeedExtend(File[] files, FileWriter writer) throws IOException {
        for (int i = 0; i < files.length - 2; i++) {
            final String left = files[i].getAbsolutePath();
            final String right = files[i + 1].getAbsolutePath();
            final Properties options = new Properties();
            options.setProperty(Options.DIFFER, "SEED_EXTEND");
            final List<String> results = run(left, right, options, "seed_extend");
            writer.append(String.join(";", results) + "\n");
        }
    }
}
