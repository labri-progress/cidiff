package org.github.gumtreediff.cidiff.clients;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.util.List;

import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;

import org.github.gumtreediff.cidiff.Action;
import org.github.gumtreediff.cidiff.LogLine;
import org.github.gumtreediff.cidiff.Pair;

public class LogsPanel extends JPanel {
    static final Color COLOR_ADDED = new Color(20, 127, 20, 134);
    static final Color COLOR_DELETED = new Color(255, 0, 0, 129);
    static final Color COLOR_UPDATED = new Color(255, 173, 0, 184);
    static final Color COLOR_UNCHANGED = new Color(255, 255, 255);

    static final Font FONT_NORMAL = new Font(Font.MONOSPACED, Font.PLAIN, 12);
    static final Font FONT_SELECTED = new Font(Font.MONOSPACED, Font.ITALIC, 12);

    static final Color COLOR_NORMAL = new Color(0, 0, 0);
    static final Color COLOR_SELECTED = new Color(0, 0, 255);

    final JList<LogLine> leftLines;
    final JList<LogLine> rightLines;
    final JScrollBar leftBar = new JScrollBar(JScrollBar.VERTICAL);
    final JScrollBar rightBar = new JScrollBar(JScrollBar.VERTICAL);
    final Pair<Action[]> actions;

    public LogsPanel(Pair<List<LogLine>> lines, Pair<Action[]> actions) {
        super(new GridLayout(1, 2));
        this.actions = actions;
        final LogLine[] leftData = new LogLine[lines.left.size()];
        lines.left.toArray(leftData);
        leftLines = new JList<>(leftData);
        leftLines.setCellRenderer(new LogLineCellRenderer(actions.left));
        leftLines.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        leftLines.addMouseListener(new LeftLinesSelectionListener());
        final JScrollPane panLeftLines = new JScrollPane(leftLines);
        leftBar.setUI(makeScrollBarUi(leftLines, actions.left));
        leftBar.setUnitIncrement(10);
        panLeftLines.setVerticalScrollBar(leftBar);
        this.add(panLeftLines);

        final LogLine[] rightData = new LogLine[lines.right.size()];
        lines.right.toArray(rightData);
        rightLines = new JList<>(rightData);
        rightLines.setCellRenderer(new LogLineCellRenderer(actions.right));
        rightLines.addMouseListener(new RightLinesSelectionListener());
        rightLines.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        final JScrollPane panRightLines = new JScrollPane(rightLines);
        rightBar.setUI(makeScrollBarUi(rightLines, actions.right));
        panRightLines.setVerticalScrollBar(rightBar);
        rightBar.setUnitIncrement(10);

        this.add(panRightLines);
        this.setPreferredSize(new Dimension(1024, 768));
    }

    private BasicScrollBarUI makeScrollBarUi(JList<LogLine> lines, Action[] scrollActions) {
        return new BasicScrollBarUI() {
            @Override protected void paintTrack(
                    Graphics g, JComponent c, Rectangle trackBounds) {
                super.paintTrack(g, c, trackBounds);
                final Rectangle rect = lines.getBounds();
                final double sy = trackBounds.getHeight() / rect.getHeight();
                final AffineTransform at = AffineTransform.getScaleInstance(1.0, sy);
                for (int i = 0; i < scrollActions.length; i++) {
                    final Action a = scrollActions[i];
                    if (a.type == Action.Type.UNCHANGED)
                        continue;

                    final Rectangle r = lines.getCellBounds(i, i);
                    final Rectangle s = at.createTransformedShape(r).getBounds();
                    final int h = 2; //Math.max(2, s.height-2);
                    if (a.type == Action.Type.DELETED) {
                        g.setColor(COLOR_DELETED);
                        g.fillRect(trackBounds.x + 2, trackBounds.y + 1 + s.y, trackBounds.width, h);
                    }
                    else if (a.type == Action.Type.ADDED) {
                        g.setColor(COLOR_ADDED);
                        g.fillRect(trackBounds.x + 2, trackBounds.y + 1 + s.y, trackBounds.width, h);
                    }
                    else if (a.type == Action.Type.UPDATED) {
                        g.setColor(COLOR_UPDATED);
                        g.fillRect(trackBounds.x + 2, trackBounds.y + 1 + s.y, trackBounds.width, h);
                    }
                }
            }
        };
    }

    private class LogLineCellRenderer extends DefaultListCellRenderer {
        private final Action[] cellActions;

        LogLineCellRenderer(Action[] cellActions) {
            this.cellActions = cellActions;
        }

        public Component getListCellRendererComponent(
                JList<?> list,
                Object value,
                int index,
                boolean isSelected,
                boolean cellHasFocus) {
            final Component res = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            final LogLine logLine = (LogLine) value;

            setToolTipText(logLine.lineNumber + " - " + index);

            if (!isSelected) {
                setForeground(COLOR_NORMAL);
                setFont(FONT_NORMAL);
            }
            else {
                setForeground(COLOR_SELECTED);
                setFont(FONT_SELECTED);
            }

            final Action action = cellActions[index];
            if (action.type == Action.Type.ADDED)
                setBackground(COLOR_ADDED);
            else if (action.type == Action.Type.DELETED)
                setBackground(COLOR_DELETED);
            else if (action.type == Action.Type.UPDATED) {
                setBackground(COLOR_UPDATED);
                setText(toHtml(action, logLine));
            }
            else
                setBackground(COLOR_UNCHANGED);

            return res;
        }

        private String toHtml(Action action, LogLine line) {
            final String otherText = this.cellActions == actions.left
                    ? rightLines.getModel().getElementAt(action.rightLocation).value
                    : leftLines.getModel().getElementAt(action.leftLocation).value;
            final String[] tokens = line.value.split("\\s+");
            final String[] otherTokens = otherText.split("\\s+");
            final StringBuilder b = new StringBuilder();
            b.append("<html>");
            for (int i = 0; i < Math.min(tokens.length, otherTokens.length); i++) {
                if (tokens[i].equals(otherTokens[i]))
                    b.append(tokens[i]);
                else
                    b.append("<b>" + tokens[i] + "</b>");
                b.append(" ");
            }
            for (int i = Math.min(tokens.length, otherTokens.length); i < tokens.length; i++) {
                b.append(tokens[i]);
                b.append(" ");
            }
            b.append("</html>");
            return b.toString();
        }
    }

    private class LeftLinesSelectionListener implements MouseListener {
        @Override
        public void mouseClicked(MouseEvent e) {
            final int leftIndex = leftLines.getSelectedIndex();
            final Action action = actions.left[leftIndex];
            if (action.type == Action.Type.UNCHANGED || action.type == Action.Type.UPDATED) {
                final int rightIndex = action.rightLocation;
                rightLines.ensureIndexIsVisible(rightIndex);
                rightLines.setSelectedIndex(rightIndex);
            }
        }

        @Override
        public void mousePressed(MouseEvent e) {
        }

        @Override
        public void mouseReleased(MouseEvent e) {
        }

        @Override
        public void mouseEntered(MouseEvent e) {
        }

        @Override
        public void mouseExited(MouseEvent e) {
        }
    }

    private class RightLinesSelectionListener implements MouseListener {
        @Override
        public void mouseClicked(MouseEvent e) {
            final int rightIndex = rightLines.getSelectedIndex();
            final Action action = actions.right[rightIndex];
            if (action.type == Action.Type.UNCHANGED || action.type == Action.Type.UPDATED) {
                final int leftIndex = action.leftLocation;
                leftLines.ensureIndexIsVisible(leftIndex);
                leftLines.setSelectedIndex(leftIndex);
            }
        }

        @Override
        public void mousePressed(MouseEvent e) {
        }

        @Override
        public void mouseReleased(MouseEvent e) {
        }

        @Override
        public void mouseEntered(MouseEvent e) {
        }

        @Override
        public void mouseExited(MouseEvent e) {
        }
    }
}
