package org.github.gumtreediff.cidiff;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

public final class Options {
    public static final String DIFFER = "differ";

    public static final String DIFFER_REWRITE_MIN = "differ.rewrite.min";

    public static final String DIFFER_SEED_BLOCK = "differ.seed.block";
    public static final String DIFFER_SEED_WINDOW = "differ.seed.window";

    public static final String PARSER = "parser";
    public static final String PARSER_DEFAULT_TRIM = "parser.default.trim";

    public static final String CLIENT = "client";

    public static final String CONSOLE_UPDATED = "console.updated";
    public static final String CONSOLE_UNCHANGED = "console.unchanged";
    public static final String CONSOLE_ADDED = "console.added";
    public static final String CONSOLE_DELETED = "console.deleted";

    private Options() {}

    public static Set<String> allOptions() {
        var fields = Arrays.stream(Options.class.getDeclaredFields())
                .filter(f -> Modifier.isStatic(f.getModifiers())).toList();
        Set<String> options = new HashSet<>();
        try {
            for (Field f : fields)
                options.add((String) f.get(null));
        }
        catch (IllegalAccessException e) {
            System.err.println(e);
        }
        return options;
    }
}
