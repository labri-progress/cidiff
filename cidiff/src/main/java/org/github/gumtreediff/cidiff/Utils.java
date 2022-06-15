package org.github.gumtreediff.cidiff;

public final class Utils {
    private static final String TOKEN_SEPARATORS = "\\s+";

    private Utils() {
    }

    public static double rewriteSim(String leftLine, String rightLine) {
        final String[] leftTokens = leftLine.split(TOKEN_SEPARATORS);
        final String[] rightTokens = rightLine.split(TOKEN_SEPARATORS);

        // lowest similarity if the number of tokens is distinct
        if (leftTokens.length != rightTokens.length)
            return 0.0;

        // number of distinct-length tokens
        int sameLength = 0;
        int sameValue = 0;

        for (int i = 0; i < leftTokens.length; i++) {
            if (leftTokens[i].length() == rightTokens[i].length()) {
                sameLength++;
                if (leftTokens[i].equals(rightTokens[i]))
                    sameValue++;
            }
        }

        if (sameValue == 0)
            return 0.0;

        return (double) sameLength / (double) leftTokens.length;
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
