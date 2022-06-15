package org.github.gumtreediff.cidiff.clients;

import java.util.Properties;

import javax.swing.*;

public final class SwingClient extends AbstractDiffClient {
    static final String TITLE = "CiDiff";

    public SwingClient(String leftFile, String rightFile, Properties options) {
        super(leftFile, rightFile, options);
    }

    public void execute() {
        javax.swing.SwingUtilities.invokeLater(() -> createAndShow());
    }

    private void createAndShow() {
        final JFrame frame = new JFrame(TITLE);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new LogsPanel(super.lines, super.actions));
        frame.pack();
        frame.setVisible(true);
    }
}
