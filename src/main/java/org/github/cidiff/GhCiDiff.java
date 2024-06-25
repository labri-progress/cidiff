package org.github.cidiff;

import org.kohsuke.github.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class GhCiDiff {
    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            System.out.println("Usage: GhCiDiff <repo>");
            System.exit(1);
        }

        final String repo = args[0];
        final GitHub github = GitHubBuilder.fromEnvironment().build();
        final GHRepository repository = github.getRepository(repo);

        Optional<GHWorkflow> workflow = Optional.empty();
        if (args.length == 1 || (args.length > 1 && !args[2].equals("-o")))
            workflow = repository.listWorkflows().toList().stream().findFirst();
        else if (args.length > 1 && !args[1].equals("-o"))
            workflow = Optional.of(repository.getWorkflow(args[1]));

        if (workflow.isEmpty()) {
            System.out.println("No workflow found");
            System.exit(0);
        }

        List<GHWorkflowRun> runs = workflow.get().listRuns().toList();
        if (runs.size() < 2) {
            System.out.println("Not enough runs found");
            System.exit(0);
        }

        final Path leftLogFile = getLog(runs.get(1));
        final Path rightLogFile = getLog(runs.get(0));

        String[] logFiles = new String[] {
                leftLogFile.toFile().getAbsolutePath(), rightLogFile.toFile().getAbsolutePath(), "-o", "parser", "GITHUB"
        };
        String[] ciDiffArgs = new String[0];
        if (Arrays.binarySearch(args, "-o") != -1)
            ciDiffArgs = Arrays.copyOfRange(args, Arrays.binarySearch(args, "-o"), args.length);
        String[] both = Stream.concat(Arrays.stream(logFiles), Arrays.stream(ciDiffArgs)).toArray(String[]::new);

        CiDiff.main(both);
    }

    private static Path getLog(GHWorkflowRun run) throws IOException {
        GHWorkflowJob job = run.listJobs().iterator().next();
        Path log = Files.createTempFile("cidiff", ".log");
        job.downloadLogs(s -> Files.copy(s, log, java.nio.file.StandardCopyOption.REPLACE_EXISTING));
        return log;
    }
}
