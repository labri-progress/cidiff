package org.github.gumtreediff.cidiff;

import java.io.IOException;

public class CiDiff {
    public static void main(String[] args) throws IOException {
        final String leftLogFile = args[0];
        final String rightLogFile = args[1];
        final String type = args[2];
        final JobDiffer differ = new JobDiffer(leftLogFile, rightLogFile, DiffInputProducer.Type.valueOf(type));
    }
}
