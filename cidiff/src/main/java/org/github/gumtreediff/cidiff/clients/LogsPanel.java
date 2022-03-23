package org.github.gumtreediff.cidiff.clients;

import org.github.gumtreediff.cidiff.Action;
import org.github.gumtreediff.cidiff.Pair;

import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.util.List;

public class LogsPanel extends JPanel {
    final JList<String> leftLines;
    final JList<String> rightLines;
    final JScrollBar leftBar = new JScrollBar(JScrollBar.VERTICAL);
    final JScrollBar rightBar = new JScrollBar(JScrollBar.VERTICAL);
    final Pair<Action[]> actions;

    final static Color COLOR_ADDED = new Color(20, 127, 20, 134);
    final static Color COLOR_DELETED = new Color(255, 0, 0, 129);
    final static Color COLOR_UPDATED = new Color(255, 173, 0, 184);
    final static Color COLOR_UNCHANGED = new Color(255, 255, 255);

    final static Font FONT_NORMAL = new Font(Font.MONOSPACED, Font.PLAIN, 12);
    final static Font FONT_SELECTED = new Font(Font.MONOSPACED, Font.ITALIC, 12);

    final static Color COLOR_NORMAL = new Color(0, 0,0 );
    final static Color COLOR_SELECTED = new Color(0, 0,255 );

    public LogsPanel(Pair<List<String>> lines, Pair<Action[]> actions) {
        super(new GridLayout(1, 2));
        this.actions = actions;
        String[] leftData = new String[lines.left.size()];
        lines.left.toArray(leftData);
        leftLines = new JList<>(leftData);
        leftLines.setCellRenderer(new LogLineCellRenderer(actions.left));
        leftLines.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        leftLines.addMouseListener(new LeftLinesSelectionListener());
        JScrollPane panLeftLines = new JScrollPane(leftLines);
        leftBar.setUI(makeScrollBarUI(leftLines, actions.left));
        leftBar.setUnitIncrement(10);
        panLeftLines.setVerticalScrollBar(leftBar);
        this.add(panLeftLines);

        String[] rightData = new String[lines.right.size()];
        lines.right.toArray(rightData);
        rightLines = new JList<>(rightData);
        rightLines.setCellRenderer(new LogLineCellRenderer(actions.right));
        rightLines.addMouseListener(new RightLinesSelectionListener());
        rightLines.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane panRightLines = new JScrollPane(rightLines);
        rightBar.setUI(makeScrollBarUI(rightLines, actions.right));
        panRightLines.setVerticalScrollBar(rightBar);
        rightBar.setUnitIncrement(10);

        this.add(panRightLines);
        this.setPreferredSize(new Dimension(1024, 768));
    }

    private BasicScrollBarUI makeScrollBarUI(JList<String> lines, Action[] actions) {
        return new BasicScrollBarUI() {
            @Override protected void paintTrack(
                    Graphics g, JComponent c, Rectangle trackBounds) {
                super.paintTrack(g, c, trackBounds);
                Rectangle rect = lines.getBounds();
                double sy = trackBounds.getHeight() / rect.getHeight();
                AffineTransform at = AffineTransform.getScaleInstance(1.0, sy);
                for (int i = 0; i < actions.length; i++) {
                    Action a = actions[i];
                    if (a.type == Action.Type.UNCHANGED)
                        continue;

                    Rectangle r = lines.getCellBounds(i, i);
                    Rectangle s = at.createTransformedShape(r).getBounds();
                    int h = 2; //Math.max(2, s.height-2);
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

        public LogLineCellRenderer(Action[] cellActions) {
            this.cellActions = cellActions;
        }

        public Component getListCellRendererComponent(
                JList<?> list,
                Object value,
                int index,
                boolean isSelected,
                boolean cellHasFocus) {
            Component res = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            if (!isSelected) {
                setForeground(COLOR_NORMAL);
                setFont(FONT_NORMAL);
            }
            else {
                setForeground(COLOR_SELECTED);
                setFont(FONT_SELECTED);
            }

            Action action = cellActions[index];
            if (action.type == Action.Type.ADDED)
                setBackground(COLOR_ADDED);
            else if (action.type == Action.Type.DELETED)
                setBackground(COLOR_DELETED);
            else if (action.type == Action.Type.UPDATED) {
                setBackground(COLOR_UPDATED);
                setText(toHtml(action, (String) value));
            }
            else
                setBackground(COLOR_UNCHANGED);
            return res;
        }

        private String toHtml(Action action, String text) {
            String otherText = this.cellActions == actions.left ?
                    rightLines.getModel().getElementAt(action.rightLocation) :
                    leftLines.getModel().getElementAt(action.leftLocation);
            String[] tokens = text.split("\\s+");
            String[] otherTokens = otherText.split("\\s+");
            StringBuilder b = new StringBuilder();
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
            int leftIndex = leftLines.getSelectedIndex();
            Action action = actions.left[leftIndex];
            if (action.type == Action.Type.UNCHANGED || action.type == Action.Type.UPDATED) {
                int rightIndex = action.rightLocation;
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
            int rightIndex = rightLines.getSelectedIndex();
            Action action = actions.right[rightIndex];
            if (action.type == Action.Type.UNCHANGED || action.type == Action.Type.UPDATED) {
                int leftIndex = action.leftLocation;
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
