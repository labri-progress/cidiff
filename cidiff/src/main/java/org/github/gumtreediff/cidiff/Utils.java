package org.github.gumtreediff.cidiff;

public final class Utils {
    private final static String TOKEN_SEPARATORS = "\\s+";
    public static double rewriteSim(String leftLine, String rightLine) {
        final String[] leftTokens = leftLine.split(TOKEN_SEPARATORS);
        final String[] rightTokens = rightLine.split(TOKEN_SEPARATORS);

        // lowest similarity if the number of tokens is distinct
        if (leftTokens.length != rightTokens.length)
            return 0.0;

        // number of distinct-length tokens
        int dist = 0;
        for (int i = 0; i < leftTokens.length; i++) {
            if (leftTokens[i].length() != rightTokens[i].length())
                dist++;
        }

        return (double) (leftTokens.length - dist) / (double) leftTokens.length;
    }

    public static int fastExponentiation(int base, int exponent) {
        if (exponent == 0)
            return 1;
        if (exponent == 1)
            return base;
        int result = 1;
        while (exponent > 0) {
            if ((exponent & 1) != 0)
                result *= base;
            exponent >>= 1;
            base *= base;
        }
        return result;
    }

    private Utils() {}
}
