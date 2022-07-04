package org.github.gumtreediff.cidiff.benchmarks;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

import org.github.gumtreediff.cidiff.*;

public final class Benchmark {
    private static final String HEADER = "LEFT;"
            + "RIGHT;"
            + "ALGORITHM;"
            + "PRECISION;"
            + "RECALL;"
            + "FSCORE;"
            + "TIME"
            + "\n";
    private static String pathToData = "data/breakages/";
    private static String pathToOutput = "benchmark/results.csv";

    private Benchmark() {
    }

    public static void main(String[] args) throws IOException {
        if (args.length == 2) {
            pathToData = args[0];
            pathToOutput = args[1];
        }

        final FileWriter csv = new FileWriter(pathToOutput);
        csv.append(HEADER);

        final List<Path> casesFolders = collectCasesFolders(pathToData);
        for (Path caseFolder: casesFolders) {
            System.out.println(caseFolder);
            final String left = caseFolder.resolve("pass.log").toString();
            final String right = caseFolder.resolve("fail.log").toString();
            final String groundtruth = caseFolder.resolve("groundtruth").toString();

            final GroundtruthContent groundtruthContent = GroundtruthContent.fromFile(groundtruth);
            final Properties options = new Properties();
            options.setProperty(Options.PARSER, groundtruthContent.parser());
            final Pair<String> logFiles = new Pair<>(left, right);
            final Pair<List<LogLine>> logs = LogParser.parseLogs(logFiles, options);
            for (LogDiffer.Algorithm algorithm : LogDiffer.Algorithm.values()) {
                final LogDiffer differ = LogDiffer.get(algorithm, options);
                final long start = System.currentTimeMillis();
                final Pair<Map<LogLine, Action>> actions = differ.diff(logs);
                final long stop = System.currentTimeMillis();

                final Set<Integer> linesCiDiff = getAddedLines(actions.right);
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

    public static List<Path> collectCasesFolders(String rootFolder) throws IOException {
        final List<Path> casesFolders = new ArrayList<>();
        final Path startPath = Paths.get(rootFolder);
        Files.walkFileTree(startPath, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                if (Files.list(dir).anyMatch(d -> d.toFile().getName().equals("groundtruth")))
                    casesFolders.add(dir);
                return FileVisitResult.CONTINUE;
            }
        });
        return casesFolders;
    }

    public static Set<Integer> getAddedLines(Map<LogLine, Action> rightActions) {
        final Set<Integer> lines = new HashSet<>();
        for (Action a : rightActions.values())
            if (a.type == Action.Type.ADDED)
                lines.add(a.rightLogLine.lineNumber);

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
