package org.github.gumtreediff.cidiff.gh;

import org.apache.commons.io.IOUtils;
import org.github.gumtreediff.cidiff.CiDiff;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHWorkflowRun;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class CiDiffGithub {
    public static void main(String[] args) throws IOException {
        List<String> leftLog = getLog(args[0], Long.parseLong(args[1]));
        Path leftLogFile = Files.createTempFile(null, null);
        Files.write(leftLogFile, leftLog);
        Path rightLogFile = Files.createTempFile(null, null);
        List<String> rightLog = getLog(args[0], Long.parseLong(args[2]));
        Files.write(rightLogFile, rightLog);
        CiDiff.main(new String[] {leftLogFile.toString(), rightLogFile.toString(), "-o", "parser", "RAW_GITHUB", "-o", "client", "SWING"});
    }

    private static List<String> getLog(String repo, long runId) throws IOException {
        GitHub github = GitHubBuilder.fromEnvironment().build();
        GHRepository repository = github.getRepository(repo);
        GHWorkflowRun run = repository.getWorkflowRun(runId);
        return run.downloadLogs((InputStream inputStream) -> {
            ZipInputStream zis =  new ZipInputStream(inputStream) ;
            ZipEntry entry = zis.getNextEntry() ;
            while (entry != null) {
                if (!entry.isDirectory()) {
                    if (!entry.getName().contains("/")) {
                        List<String> log = IOUtils.readLines(zis, StandardCharsets.UTF_8);
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
