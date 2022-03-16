package org.github.gumtreediff.cidiff.clients;

import org.github.gumtreediff.cidiff.Action;
import org.github.gumtreediff.cidiff.Pair;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;

public class LogsPanel extends JPanel {
    final JList<String> leftLines;
    final JList<String> rightLines;
    final Pair<Action[]> actions;

    final static Color COLOR_ADDED = new Color(20, 127, 20, 134);
    final static Color COLOR_DELETED = new Color(255, 0, 0, 129);
    final static Color COLOR_UPDATED = new Color(255, 173, 0, 184);
    final static Color COLOR_UNCHANGED = new Color(255, 255, 255);

    final static Font FONT_NORMAL = new Font(Font.MONOSPACED, Font.PLAIN, 11);
    final static Font FONT_SELECTED = new Font(Font.MONOSPACED, Font.BOLD, 11);

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
        this.add(panLeftLines);
        String[] rightData = new String[lines.right.size()];
        lines.right.toArray(rightData);
        rightLines = new JList<>(rightData);
        rightLines.setCellRenderer(new LogLineCellRenderer(actions.right));
        rightLines.addMouseListener(new RightLinesSelectionListener());
        rightLines.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane panRightLines = new JScrollPane(rightLines);
        this.add(panRightLines);
        this.setPreferredSize(new Dimension(1024, 768));
    }

    private class LogLineCellRenderer extends DefaultListCellRenderer {
        private final Action[] actions;

        public LogLineCellRenderer(Action[] actions) {
            this.actions = actions;
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

            Action action = actions[index];
            if (action.type == Action.Type.ADDED)
                setBackground(COLOR_ADDED);
            else if (action.type == Action.Type.DELETED)
                setBackground(COLOR_DELETED);
            else if (action.type == Action.Type.UPDATED)
                setBackground(COLOR_UPDATED);
            else
                setBackground(COLOR_UNCHANGED);
            return res;
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
