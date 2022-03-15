package org.github.gumtreediff.cidiff;

import org.github.gumtreediff.cidiff.clients.ConsoleClient;
import org.github.gumtreediff.cidiff.clients.MetricsClient;
import org.github.gumtreediff.cidiff.clients.SwingClient;

import java.util.Properties;

public interface LogClient {
    enum Type {
        SWING,
        CONSOLE,
        METRICS
    }

    public static LogClient get(String leftFile, String rightFile,
                                Properties options, Type type) {
        return switch (type) {
            case SWING -> new SwingClient(leftFile, rightFile, options);
            case CONSOLE -> new ConsoleClient(leftFile, rightFile, options);
            case METRICS -> new MetricsClient(leftFile, rightFile, options);
        };
    }

    void execute();
}
