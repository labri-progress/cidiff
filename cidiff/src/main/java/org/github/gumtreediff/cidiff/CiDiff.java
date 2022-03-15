package org.github.gumtreediff.cidiff;

import java.util.Properties;
import java.util.Set;

public final class CiDiff {
    public static final String DEFAULT_CLIENT = "CONSOLE";

    public static void main(String[] args) {
        final String leftFile = args[0];
        final String rightFile = args[1];
        final Properties options = parseOptions(args);
        LogClient.Type type = LogClient.Type.valueOf(
                options.getProperty(Options.CLIENT, DEFAULT_CLIENT));
        LogClient client = LogClient.get(leftFile, rightFile, options, type);
        client.execute();
    }

    public static Properties parseOptions(String args[]) {
        Set<String> allOptions = Options.allOptions();
        final Properties options = new Properties();
        if (args.length > 2) {
            if ((args.length - 2) % 3 != 0)
                throw new IllegalArgumentException("Wrong number of arguments " + args.length);
            for (int i = 2; i < args.length; i = i + 3) {
                final String option = args[i];
                if (!option.equals("-o"))
                    throw new IllegalArgumentException("Illegal option flag: " + option);

                final String key = args[i + 1];
                if (!allOptions.contains(key))
                    throw new IllegalArgumentException("Illegal option: " + key);
                
                final String value = args[i + 2];
                options.setProperty(key, value);
            }
        }
        return options;
    }
}
