package org.github.gumtreediff.cidiff;

import org.github.gumtreediff.cidiff.clients.ConsoleClient;
import org.github.gumtreediff.cidiff.clients.MetricsClient_modified;

import java.io.File;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import java.io.BufferedReader;


public class Precision_recall {
    private final static String HEADER = "left;right;errors(groundtruth);errors(Cidiff);not errors(Cidiff);total(Cidiff)";
    private final static String path_to_output = "benchmark/precision_recall.csv";
    private final static String path_to_data = "data/breakages/";

    public static void main(String[] args) throws Exception {
        writeHeader();
        Boolean context = getArgument(args);
        File breakages_cases = new File(path_to_data);
        File[] sub_folders = breakages_cases.listFiles(File::isDirectory);

        for(int i = 0; i < sub_folders.length; i++) {
            System.out.println(sub_folders[i]);
            File cases = new File(sub_folders[i].getAbsolutePath());
            File[] files = cases.listFiles();
            String left = files[0].getAbsolutePath();
            String right = files[1].getAbsolutePath();
            Ground groundtruth = groundtruth(files[2].getAbsolutePath(),context);

            int errors = groundtruth.high - groundtruth.low;
            FileWriter w = new FileWriter(path_to_output,true);
            w.append(left+";"+right+";"+errors+";");
            w.close();

            Properties options = new Properties();
            runCidiff(left, right, options, groundtruth.parser);
            }
    }
    
    private static void runCidiff(String left, String right, Properties options, String parser) {
        MetricsClient_modified newLines = null;
        options.setProperty(Options.PARSER, parser);
        newLines = new MetricsClient_modified(left, right, options);
        newLines.execute();
    }

    private static void writeHeader() throws IOException {
        FileWriter h = new FileWriter(path_to_output);
        h.append(HEADER + "\n");
        h.close();
    }

    private static Boolean getArgument(String[] args) {
        Boolean context=false;
        if (args.length > 1) {
            throw new IllegalArgumentException("Too many arguments: " + args.length);
        }
        else if (args.length == 1 && args[0].equals("Context")){
            context=true;
        }
        return context;
    }

    private static Ground groundtruth(String groundtruth, Boolean context) throws Exception {
        Ground res = new Ground();
        BufferedReader br = new BufferedReader(new FileReader(groundtruth));
        res.parser = br.readLine();
        String intervalles;
        while ((intervalles = br.readLine()) != null)
            if (intervalles.charAt(0) == 'E' || context == true){
                String[] errors_lines = intervalles.substring(2).split("-");
                if (errors_lines.length == 2) {
                    res.low += Integer.parseInt(errors_lines[0]);
                    res.high += Integer.parseInt(errors_lines[1])+1;
                }
                else{
                    res.high += 1;
                }
            }
        return res;
    }

    private static class Ground {
        int low = 0;
        int high = 0;
        String parser;
    }
}