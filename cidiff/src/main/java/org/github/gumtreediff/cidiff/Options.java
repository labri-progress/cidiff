package org.github.gumtreediff.cidiff;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

public final class Options {
    public static final String DIFFER = "differ";
    public static final String DIFFER_UPDATED = "differ.updated";
    public static final String DIFFER_UNCHANGED = "differ.unchanged";
    public static final String DIFFER_ADDED = "differ.added";
    public static final String DIFFER_DELETED = "differ.deleted";

    public static final String DIFFER_REWRITE_MIN = "differ.rewrite.min";

    public static final String DIFFER_SEED_BLOCK = "differ.seed.block";
    public static final String DIFFER_SEED_WINDOW = "differ.seed.window";

    public static final String PARSER = "parser";
    public static final String PARSER_DEFAULT_TRIM = "parser.default.trim";

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
