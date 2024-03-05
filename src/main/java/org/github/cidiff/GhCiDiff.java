package org.github.cidiff;

import org.apache.commons.io.IOUtils;
import org.kohsuke.github.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class GhCiDiff {
    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            System.out.println("Usage: GhCiDiff <repo>");
            System.exit(1);
        }

        final String repo = args[0];
        final GitHub github = GitHubBuilder.fromEnvironment().build();
        final GHRepository repository = github.getRepository(repo);

        Optional<GHWorkflow> workflow;
        if (args.length == 1)
            workflow = repository.listWorkflows().toList().stream().findFirst();
        else
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

        final List<String> leftLog = getLog(runs.get(1));
        final Path leftLogFile = Files.createTempFile(null, null);
        Files.write(leftLogFile, leftLog);
        final List<String> rightLog = getLog(runs.get(0));
        final Path rightLogFile = Files.createTempFile(null, null);
        Files.write(rightLogFile, rightLog);

        CiDiff.main(new String[] {
                leftLogFile.toString(), rightLogFile.toString(), "-o", "parser", "GITHUB", "-o", "client", "SWING", "-o", "differ", "SEED"}
        );
    }

    private static List<String> getLog(String repo, long runId) throws IOException {
        final GitHub github = GitHubBuilder.fromEnvironment().build();
        final GHRepository repository = github.getRepository(repo);
        final GHWorkflowRun run = repository.getWorkflowRun(runId);
        return run.downloadLogs((InputStream inputStream) -> {
            final ZipInputStream zis = new ZipInputStream(inputStream);
            ZipEntry entry = zis.getNextEntry();
            while (entry != null) {
                if (!entry.isDirectory()) {
                    if (!entry.getName().contains("/")) {
                        final List<String> log = IOUtils.readLines(zis, StandardCharsets.UTF_8);
                        if (log.size() == 0 || log.get(0).contains("Starting: Prepare job")) {
                            entry = zis.getNextEntry();
                            continue;
                        }
                        else
                            return log;
                    }
                }
                entry = zis.getNextEntry();
            }
            zis.close();
            return null;
        });
    }

    private static List<String> getLog(GHWorkflowRun run) throws IOException {
        return run.downloadLogs((InputStream inputStream) -> {
            final ZipInputStream zis = new ZipInputStream(inputStream);
            ZipEntry entry = zis.getNextEntry();
            while (entry != null) {
                if (!entry.isDirectory()) {
                    if (!entry.getName().contains("/")) {
                        final List<String> log = IOUtils.readLines(zis, StandardCharsets.UTF_8);
                        if (log.size() == 0 || log.get(0).contains("Starting: Prepare job")) {
                            entry = zis.getNextEntry();
                            continue;
                        }
                        else
                            return log;
                    }
                }
                entry = zis.getNextEntry();
            }
            zis.close();
            return null;
        });
    }
}
