package org.github.gumtreediff.cidiff;

import java.io.IOException;

public class CiDiff {
    public static void main(String[] args) throws IOException {
        String leftLog = args[0];
        String rightLog = args[1];
        JobDiffer.diff(leftLog, rightLog);
    }
}
