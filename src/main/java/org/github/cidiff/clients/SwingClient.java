package org.github.cidiff.clients;

import org.github.cidiff.Action;
import org.github.cidiff.Line;
import org.github.cidiff.Options;
import org.github.cidiff.Pair;

import javax.swing.JFrame;
import java.util.List;

public final class SwingClient extends AbstractDiffClient {
    static final String TITLE = "CiDiff";

    public SwingClient(Pair<List<Line>> lines, Pair<List<Action>> actions) {
        super(lines, actions);
    }

    public void execute(Options options) {
        javax.swing.SwingUtilities.invokeLater(() -> createAndShow(options));
    }

    private void createAndShow(Options options) {
        final JFrame frame = new JFrame(TITLE);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new LogsPanel(this.lines, this.actions, options));
        frame.pack();
        frame.setVisible(true);
    }
}
