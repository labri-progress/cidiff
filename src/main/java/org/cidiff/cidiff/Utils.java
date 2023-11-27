package org.cidiff.cidiff;

public final class Utils {
    private static final String TOKEN_SEPARATORS = "\\s+";

    private Utils() {
    }

    public static int[] termsHash(Line line) {
        final String[] terms = split(line);
        final int[] hash = new int[terms.length];
        for (int i = 0; i < terms.length; i++)
            hash[i] = terms[i].length();
        return hash;
    }

    public static String[] split(Line line) {
        return line.value().split(TOKEN_SEPARATORS);
    }

    public static double rewriteSim(Line leftLine, Line rightLine) {
        return rewriteSim(leftLine.value(), rightLine.value());
    }

    public static double rewriteSim(String leftLine, String rightLine) {
        final String[] leftTokens = leftLine.trim().split(TOKEN_SEPARATORS);
        final String[] rightTokens = rightLine.trim().split(TOKEN_SEPARATORS);

        // tokens amount different : we assume the lines have different log templates
        // lowest similarity if the number of tokens is distinct
        if (leftTokens.length != rightTokens.length) {
            return 0;
        }

        boolean ok = false;
        double count = 0;
        for (int i = 0; i < leftTokens.length; i++) {
            if (leftTokens[i].equals(rightTokens[i])) {
                count += 1;
                ok = true;
            } else if (leftTokens[i].length() == rightTokens[i].length()) {
                count += 0.5;
            } else if (lcsLength(leftTokens[i].toCharArray(), rightTokens[i].toCharArray())
                    >= 2 * Math.max(leftTokens[i].length(), rightTokens[i].length()) / 3) {
                count += 0.5;
            }
        }

        if (!ok) {
            return 0;
        }

        return count / leftTokens.length;
    }

    public static int lcsLength(char[] left, char[] right) {
        // https://en.wikipedia.org/wiki/Longest_common_subsequence#Computing_the_length_of_the_LCS
        final int l = left.length;
        final int r = right.length;
        final int[][] c = new int[l + 1][r + 1];
        for (int i = 1; i <= l; i++) {
            for (int j = 1; j <= r; j++) {
                if (left[i - 1] == right[j - 1]) {
                    c[i][j] = c[i - 1][j - 1] + 1;
                } else {
                    c[i][j] = Math.max(c[i][j - 1], c[i - 1][j]);
                }
            }
        }
        return c[l][r];
    }

    public static int fastExponentiation(int base, int exponent) {
        int myExponent = exponent;
        int myBase = base;
        if (myExponent == 0)
            return 1;
        if (myExponent == 1)
            return base;
        int result = 1;
        while (myExponent > 0) {
            if ((myExponent & 1) != 0)
                result *= myBase;
            myExponent >>= 1;
            myBase *= myBase;
        }
        return result;
    }
}
