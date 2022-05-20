package org.github.gumtreediff.cidiff;

import org.github.gumtreediff.cidiff.clients.ConsoleClient;
import org.github.gumtreediff.cidiff.clients.JsonClient;
import org.github.gumtreediff.cidiff.clients.MetricsClient;
import org.github.gumtreediff.cidiff.clients.SwingClient;

import java.util.Properties;

public interface DiffClient {
    enum Type {
        SWING,
        JSON,
        CONSOLE,
        METRICS
    }

    static DiffClient get(String leftFile, String rightFile,
                          Properties options, Type type) {
        return switch (type) {
            case SWING -> new SwingClient(leftFile, rightFile, options);
            case CONSOLE -> new ConsoleClient(leftFile, rightFile, options);
            case JSON -> new JsonClient(leftFile, rightFile, options);
            case METRICS -> new MetricsClient(leftFile, rightFile, options);
        };
    }

    void execute();
}
