package org.cidiff.cidiff.clients;

import org.cidiff.cidiff.Action;
import org.cidiff.cidiff.Line;
import org.cidiff.cidiff.Options;
import org.cidiff.cidiff.Pair;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LogsPanel extends JPanel {
	static final Color COLOR_ADDED_LEFT = new Color(20, 127, 20, 45);
	static final Color COLOR_ADDED_RIGHT = new Color(20, 127, 20, 134);
	static final Color COLOR_DELETED_LEFT = new Color(255, 0, 0, 129);
	static final Color COLOR_DELETED_RIGHT = new Color(255, 0, 0, 45);
	static final Color COLOR_UPDATED = new Color(255, 173, 0, 184);
	static final Color COLOR_UNCHANGED = new Color(255, 255, 255);
	static final Color COLOR_MOVED = new Color(200, 200, 200);
	static final Color COLOR_SKIPPED = new Color(225, 225, 225, 255);
	static final Color COLOR_DEBUG_1 = new Color(0, 255, 255, 255);

	static final Font FONT_NORMAL = new Font(Font.MONOSPACED, Font.PLAIN, 12);
	static final Font FONT_SELECTED = new Font(Font.MONOSPACED, Font.ITALIC, 12);

	static final Color COLOR_NORMAL = new Color(0, 0, 0);
	static final Color COLOR_SELECTED = new Color(0, 0, 255);

	final JList<Line> leftLines;
	final JList<Line> rightLines;
	final JScrollBar leftBar = new JScrollBar(JScrollBar.VERTICAL);
	final JScrollBar rightBar = new JScrollBar(JScrollBar.VERTICAL);
	final Pair<List<Action>> actions;

	int lastGreen = -1;

	public LogsPanel(Pair<List<Line>> lines, Pair<List<Action>> actions) {
		super(new GridLayout(1, Options.getInstance().getSwingColumns().isEmpty() ? 2 : 1));
		this.actions = actions;
		insertLinesForParallelScrolling(lines, actions);
		Line[] leftData = new Line[lines.left().size()];
		lines.left().toArray(leftData);
		leftLines = new JList<>(leftData);
		leftLines.setCellRenderer(new LogLineCellRenderer(actions.left(), true));
		leftLines.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		leftLines.addMouseListener(new LeftLinesSelectionListener());
		final JScrollPane panLeftLines = new JScrollPane(leftLines);
		leftBar.setUI(makeScrollBarUi(leftLines, actions.left()));
		leftBar.setUnitIncrement(10);
		panLeftLines.setVerticalScrollBar(leftBar);
		if (Options.getInstance().getSwingColumns().isEmpty() || Options.getInstance().getSwingColumns().equalsIgnoreCase("left")) {
			this.add(panLeftLines);
		}

		Line[] rightData = new Line[lines.right().size()];
		lines.right().toArray(rightData);
		rightLines = new JList<>(rightData);
		rightLines.setCellRenderer(new LogLineCellRenderer(actions.right(), false));
		rightLines.addMouseListener(new RightLinesSelectionListener());
		rightLines.addKeyListener(new KeyboardShortcut());
		rightLines.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		final JScrollPane panRightLines = new JScrollPane(rightLines);
		rightBar.setUI(makeScrollBarUi(rightLines, actions.right()));
		panRightLines.setVerticalScrollBar(rightBar);
		rightBar.setUnitIncrement(10);
		if (Options.getInstance().getSwingColumns().isEmpty() || Options.getInstance().getSwingColumns().equalsIgnoreCase("right")) {
			this.add(panRightLines);
		}
		SyncScrollBar synchronizer = new SyncScrollBar(panLeftLines, panRightLines);
		panLeftLines.getVerticalScrollBar().addAdjustmentListener(synchronizer);
		panLeftLines.getHorizontalScrollBar().addAdjustmentListener(synchronizer);
		panRightLines.getVerticalScrollBar().addAdjustmentListener(synchronizer);
		panRightLines.getHorizontalScrollBar().addAdjustmentListener(synchronizer);

		this.setPreferredSize(new Dimension(1024, 768));
	}

	private static void insertLinesForParallelScrolling(Pair<List<Line>> pair, Pair<List<Action>> actions) {
		List<Line> left = pair.left();
		List<Line> right = pair.right();
		List<int[]> lcs = new ArrayList<>();
		for (int i = 0; i < left.size(); i++) {
			Action action = actions.left().get(i);
			if (action.type == Action.Type.UPDATED || action.type == Action.Type.UNCHANGED) {
				lcs.add(new int[]{action.leftLogLine.index(), action.rightLogLine.index()});
			}
		}
		int i = 0;
		int I = 0;
		// lines from 0 to last element in the lcs
		while (I < lcs.size()) {
			int[] match = lcs.get(I);

			while (left.get(i).index() < match[0]) {
				Line line = new Line(-1, "r" + i, "r" + i);
				right.add(i, line);
				Action old = actions.left().get(i);
				Action newAction = new Action(old.leftLogLine, line, old.type);
				actions.left().set(i, newAction);
				actions.right().add(i, newAction);
//				for (int k = I; k < lcs.size(); k++) {
//					lcs.get(k)[1]++;
//				}
				i++;
			}
			while (right.get(i).index() < match[1]) {
				Line line = new Line(-1, "l" + i, "l" + i);
				left.add(i, line);
				Action old = actions.right().get(i);
				Action newAction = new Action(line, old.rightLogLine, old.type);
				actions.left().add(i, newAction);
				actions.right().set(i, newAction);
//				for (int k = I; k < lcs.size(); k++) {
//					lcs.get(k)[0]++;
//				}
				i++;
			}
			i++;
			I++;
		}
		// lines after the lcs
		int j = i;
		while (j < left.size()) {
			Line line = new Line(-1, "r" + j, "r" + j);
			if (j < right.size()) {
				right.add(j, line);
			} else {
				right.add(line);
			}
			Action old = actions.left().get(j);
			Action newAction = new Action(old.leftLogLine, line, old.type);
			actions.left().set(j, newAction);
			if (j < actions.right().size()) {
				actions.right().add(j, newAction);
			} else {
				actions.right().add(newAction);
			}
			j++;
		}
		while (j < right.size()) {
			Line line = new Line(-1, "l" + j, "l" + j);
			left.add(line); // at this point we are sure j is bigger than the left size (in the lines and actions)
			Action old = actions.right().get(j);
			Action newAction = new Action(line, old.rightLogLine, old.type);
			actions.left().add(newAction);
			actions.right().set(j, newAction);
			j++;
		}
	}

	private BasicScrollBarUI makeScrollBarUi(JList<Line> lines, List<Action> scrollActions) {
		return new BasicScrollBarUI() {
			@Override
			protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
				super.paintTrack(g, c, trackBounds);
				final Rectangle rect = lines.getBounds();
				final double sy = trackBounds.getHeight() / rect.getHeight();
				final AffineTransform at = AffineTransform.getScaleInstance(1.0, sy);
				for (int i = 0; i < scrollActions.size(); i++) {
					Action a = scrollActions.get(i);
					if (a == null) continue;
					if (a.type == Action.Type.UNCHANGED) continue;

					Rectangle r = lines.getCellBounds(i, i);
					Rectangle s = at.createTransformedShape(r).getBounds();
					int h = 2; //Math.max(2, s.height-2);
					if (a.type == Action.Type.DELETED) {
						g.setColor(COLOR_DELETED_LEFT);
						g.fillRect(trackBounds.x + 2, trackBounds.y + 1 + s.y, trackBounds.width, h);
					} else if (a.type == Action.Type.ADDED) {
						g.setColor(COLOR_ADDED_RIGHT);
						g.fillRect(trackBounds.x + 2, trackBounds.y + 1 + s.y, trackBounds.width, h);
					} else if (a.type == Action.Type.UPDATED) {
						g.setColor(COLOR_UPDATED);
						g.fillRect(trackBounds.x + 2, trackBounds.y + 1 + s.y, trackBounds.width, h);
					} else if (a.type == Action.Type.SKIPPED) {
						g.setColor(COLOR_SKIPPED);
						g.fillRect(trackBounds.x + 2, trackBounds.y + 1 + s.y, trackBounds.width, h);
					} else if (a.type == Action.Type.MOVED_UNCHANGED || a.type == Action.Type.MOVED_UPDATED) {
						g.setColor(COLOR_MOVED);
						g.fillRect(trackBounds.x + 2, trackBounds.y + 1 + s.y, trackBounds.width, h);
					} else if (a.debug == 1 && Options.getInstance().getSwingDisplayDebug()) {
						g.setColor(COLOR_DEBUG_1);
						g.fillRect(trackBounds.x + 2, trackBounds.y + 1 + s.y, trackBounds.width, h);
					}
				}
			}
		};
	}

	public static class SyncScrollBar implements AdjustmentListener {

		JScrollBar v1;
		JScrollBar h1;
		JScrollBar v2;
		JScrollBar h2;

		public SyncScrollBar(JScrollPane sp1, JScrollPane sp2) {
			v1 = sp1.getVerticalScrollBar();
			h1 = sp1.getHorizontalScrollBar();
			v2 = sp2.getVerticalScrollBar();
			h2 = sp2.getHorizontalScrollBar();
		}

		@Override
		public void adjustmentValueChanged(AdjustmentEvent e) {
			JScrollBar scrollBar = (JScrollBar) e.getSource();
			JScrollBar target = null;

			if (scrollBar == v1)
				target = v2;
			if (scrollBar == h1)
				target = h2;
			if (scrollBar == v2)
				target = v1;
			if (scrollBar == h2)
				target = h1;

			target.setValue(scrollBar.getValue());
		}
	}

	private class LogLineCellRenderer extends DefaultListCellRenderer {
		private final List<Action> cellActions;
		private final boolean isLeftSide;

		LogLineCellRenderer(List<Action> cellActions, boolean isLeftSide) {
			this.cellActions = cellActions;
			this.isLeftSide = isLeftSide;
		}

		public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			final Component res = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

			Line logLine = (Line) value;

			if (!isSelected) {
				setForeground(COLOR_NORMAL);
				setFont(FONT_NORMAL);
			} else {
				setForeground(COLOR_SELECTED);
				setFont(FONT_SELECTED);
			}

			final Action action = cellActions.get(index);
			int l = action.leftLogLine == null ? -1 : action.leftLogLine.index();
			int r = action.rightLogLine == null ? -1 : action.rightLogLine.index();
			setToolTipText(String.format("%s %d - %d - %.2f %d", action.type, l, r, action.sim, logLine.hash()));
			final String textLine = logLine.index() + " " + logLine.value().replaceAll("\\t", "    ");
			if (action.type == Action.Type.ADDED) {
				if (isLeftSide) {
					setText(" ");
					setBackground(COLOR_ADDED_LEFT);
				} else {
					setText(textLine);
					setBackground(COLOR_ADDED_RIGHT);
				}
			} else if (action.type == Action.Type.DELETED) {
				if (isLeftSide) {
					setText(textLine);
					setBackground(COLOR_DELETED_LEFT);
				} else {
					setText(" ");
					setBackground(COLOR_DELETED_RIGHT);
				}
			} else if (action.type == Action.Type.UPDATED) {
				setText(toHtml(action, logLine));
			} else if (action.type == Action.Type.SKIPPED) setBackground(COLOR_SKIPPED);
			else if (action.type == Action.Type.MOVED_UNCHANGED) {
				setText(textLine);
				setBackground(COLOR_MOVED);
			} else if (action.type == Action.Type.MOVED_UPDATED) {
				setBackground(COLOR_MOVED);
				setText(toHtml(action, logLine));
			} else {
				setText(textLine);
				setBackground(COLOR_UNCHANGED);
			}

			if (action.debug == 1 && Options.getInstance().getSwingDisplayDebug())
				setBackground(COLOR_DEBUG_1);

			return res;
		}

		private String toHtml(Action action, Line line) {
			String otherText;
			try {
				otherText = this.cellActions == actions.left() ? action.rightLogLine.value() : action.leftLogLine.value();
			} catch (NullPointerException e) {
				System.out.println("tried " + action);
				otherText = "";
			}
			final String[] tokens = line.value().strip().split("\\s+");
			final String[] otherTokens = otherText.strip().split("\\s+");
			final StringBuilder b = new StringBuilder();
			b.append("<html>" + line.index() + "&nbsp;");
			if (!line.value().strip().equals(line.value())) {
				// if there was leading whitespaces, add them back to preserve the indentation
				b.append(line.value().split("\\w+")[0].replaceAll("\\t", "&nbsp;".repeat(4)));
			}
			for (int i = 0; i < Math.min(tokens.length, otherTokens.length); i++) {
				final String format = tokens[i].equals(otherTokens[i]) ? "%s&nbsp;" : "<span style='background-color:rgb(255, 173, 0);'>%s</span>&nbsp;";
				b.append(format.formatted(tokens[i]));
			}
			for (int i = Math.min(tokens.length, otherTokens.length); i < tokens.length; i++) {
				b.append(tokens[i]);
				b.append("&nbsp;");
			}
			b.append("</html>");
			return b.toString();
		}
	}

	private class LeftLinesSelectionListener implements MouseListener {
		@Override
		public void mouseClicked(MouseEvent e) {
			Action action = actions.left().get(leftLines.getSelectedIndex());
			rightLines.setSelectedValue(action.rightLogLine, true);
//			rightLines.setSelectedIndex(leftLines.getSelectedIndex());
//			rightLines.setSelectedValue(action.rightLogLine, true);
//			if (action.type == Action.Type.UNCHANGED || action.type == Action.Type.UPDATED || action.type == Action.Type.MOVED_UNCHANGED || action.type == Action.Type.MOVED_UPDATED)
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
			Action action = actions.right().get(rightLines.getSelectedIndex());
			leftLines.setSelectedValue(action.leftLogLine, true);
//			if (action.type == Action.Type.UNCHANGED || action.type == Action.Type.UPDATED || action.type == Action.Type.MOVED_UNCHANGED || action.type == Action.Type.MOVED_UPDATED)
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

	private class KeyboardShortcut implements KeyListener {

		@Override
		public void keyTyped(KeyEvent e) {

			if (e.getKeyChar() == 'g') {
				if (e.isAltDown() && lastGreen > 0 && actions.right().get(lastGreen).type == Action.Type.ADDED) {
					while (lastGreen < rightLines.getModel().getSize() && actions.right().get(lastGreen).type == Action.Type.ADDED) {
						lastGreen++;
					}
					lastGreen--;
					rightLines.setSelectedValue(rightLines.getModel().getElementAt(lastGreen), true);
				} else {
					int again = 0;
					while (again < 2) {
						for (int j = lastGreen + 1; j < rightLines.getModel().getSize(); j++) {
							if (actions.right().get(j).type == Action.Type.ADDED) {
								lastGreen = j;
								rightLines.setSelectedValue(rightLines.getModel().getElementAt(j), true);
								again = 2;
								break;
							}
						}
						again++;
						if (again == 1) {
							lastGreen = -1;
						}
					}
				}
			} else if (e.getKeyChar() == 'G') {
				if (e.isAltDown() && lastGreen < rightLines.getModel().getSize() && actions.right().get(lastGreen).type == Action.Type.ADDED) {
					while (lastGreen > 0 && actions.right().get(lastGreen).type == Action.Type.ADDED) {
						lastGreen--;
					}
					lastGreen++;
					rightLines.setSelectedValue(rightLines.getModel().getElementAt(lastGreen), true);
				} else {
					int again = 0;
					while (again < 2) {
						for (int j = lastGreen - 1; j > 0; j--) {
							if (actions.right().get(j).type == Action.Type.ADDED) {
								lastGreen = j;
								rightLines.setSelectedValue(rightLines.getModel().getElementAt(j), true);
								again = 2;
								break;
							}
						}
						again++;
						if (again == 1) {
							lastGreen = rightLines.getModel().getSize();
						}
					}
				}
			}
		}

		@Override
		public void keyPressed(KeyEvent e) {

		}

		@Override
		public void keyReleased(KeyEvent e) {

		}
	}
}
