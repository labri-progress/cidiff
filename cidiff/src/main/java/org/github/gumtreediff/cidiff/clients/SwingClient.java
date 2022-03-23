package org.github.gumtreediff.cidiff.clients;

import org.github.gumtreediff.cidiff.*;

import javax.swing.*;
import java.util.Properties;

public class SwingClient extends AbstractDiffClient {
    final static String TITLE = "CiDiff";

    public SwingClient(String leftFile, String rightFile, Properties options) {
        super(leftFile, rightFile, options);
    }

    public void execute() {
        javax.swing.SwingUtilities.invokeLater(() -> createAndShow());
    }

    private void createAndShow() {
        JFrame frame = new JFrame(TITLE);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new LogsPanel(super.lines, super.actions));
        frame.pack();
        frame.setVisible(true);
    }
}
