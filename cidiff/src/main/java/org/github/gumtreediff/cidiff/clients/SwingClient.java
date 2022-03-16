package org.github.gumtreediff.cidiff.clients;

import org.github.gumtreediff.cidiff.*;

import javax.swing.*;
import java.util.Properties;

public class SwingClient extends AbstractDiffClient {
    public static void main(String[] args) {
        final String leftLogFile = args[0];
        final String rightLogFile = args[1];
        final Properties options = CiDiff.parseOptions(args);
        new SwingClient(leftLogFile, rightLogFile, options);
    }

    public SwingClient(String leftFile, String rightFile, Properties options) {
        super(leftFile, rightFile, options);
    }

    public void execute() {
        javax.swing.SwingUtilities.invokeLater(() -> createAndShow());
    }

    private void createAndShow() {
        JFrame frame = new JFrame("Logs diff");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new LogsPanel(super.lines, super.actions));
        frame.pack();
        frame.setVisible(true);
    }
}
