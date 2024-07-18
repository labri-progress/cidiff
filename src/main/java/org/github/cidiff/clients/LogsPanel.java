package org.github.cidiff.clients;

import org.github.cidiff.Action;
import org.github.cidiff.Line;
import org.github.cidiff.Options;
import org.github.cidiff.Pair;
import org.github.cidiff.Utils;

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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Label;
import java.awt.Rectangle;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.util.List;

public class LogsPanel extends JPanel {
	static final Color COLOR_ADDED = new Color(20, 127, 20, 134);
	static final Color COLOR_ADDED_DIMMED = new Color(20, 127, 20, 45);
	static final Color COLOR_DELETED = new Color(255, 0, 0, 129);
	static final Color COLOR_DELETED_DIMMED = new Color(255, 0, 0, 45);
	static final Color COLOR_UPDATED = new Color(255, 173, 0, 184);
	static final Color COLOR_UNCHANGED = new Color(255, 255, 255);
	static final Color COLOR_MOVED = new Color(168, 22, 168, 136);
	static final Color COLOR_MOVED_DIMMED = new Color(168, 22, 168, 45);
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
	private static boolean shouldParallelScroll = true;

	public LogsPanel(Pair<List<Line>> lines, Pair<List<Action>> actions, Options options) {
		super(new GridBagLayout());  // 1, Options.getSwingColumns().isEmpty() ? 2 : 1)
		Label label = new Label("Parser: " + options.parser() + " - Differ: " + options.algorithm());
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridwidth = 2;
		constraints.weightx = 1;
		this.add(label, constraints);
		Pair.Free<Pair<List<Line>>,Pair<List<Action>>> alligned = Utils.allignLines(lines, actions);
		actions = alligned.right();
		lines = alligned.left();
		this.actions = actions;
		Line[] leftData = new Line[lines.left().size()];
		lines.left().toArray(leftData);
		leftLines = new JList<>(leftData);
		leftLines.setCellRenderer(new LogLineCellRenderer(actions.left(), true));
		leftLines.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		leftLines.addMouseListener(new LeftLinesSelectionListener());
		final JScrollPane panLeftLines = new JScrollPane(leftLines);
		leftBar.setUI(makeScrollBarUi(leftLines, actions.left(), true));
		leftBar.setUnitIncrement(10);
		panLeftLines.setVerticalScrollBar(leftBar);
		if (options.swingColumns().isEmpty() || options.swingColumns().equalsIgnoreCase("left")) {
			GridBagConstraints c = new GridBagConstraints();
			c.gridx = 0;
			c.gridy = 1;
			c.fill = GridBagConstraints.BOTH;
			c.weightx = 1;
			c.weighty = 1;
			this.add(panLeftLines, c);
		}

		Line[] rightData = new Line[lines.right().size()];
		lines.right().toArray(rightData);
		rightLines = new JList<>(rightData);
		rightLines.setCellRenderer(new LogLineCellRenderer(actions.right(), false));
		rightLines.addMouseListener(new RightLinesSelectionListener());
		rightLines.addKeyListener(new KeyboardShortcut());
		rightLines.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		final JScrollPane panRightLines = new JScrollPane(rightLines);
		rightBar.setUI(makeScrollBarUi(rightLines, actions.right(), false));
		panRightLines.setVerticalScrollBar(rightBar);
		rightBar.setUnitIncrement(10);
		if (options.swingColumns().isEmpty() || options.swingColumns().equalsIgnoreCase("right")) {
			GridBagConstraints c = new GridBagConstraints();
			c.gridx = 1;
			c.gridy = 1;
			c.fill = GridBagConstraints.BOTH;
			c.weightx = 1;
			c.weighty = 1;
			this.add(panRightLines, c);
		}
		SyncScrollBar synchronizer = new SyncScrollBar(panLeftLines, panRightLines);
		panLeftLines.getVerticalScrollBar().addAdjustmentListener(synchronizer);
		panLeftLines.getHorizontalScrollBar().addAdjustmentListener(synchronizer);
		panRightLines.getVerticalScrollBar().addAdjustmentListener(synchronizer);
		panRightLines.getHorizontalScrollBar().addAdjustmentListener(synchronizer);

		this.setPreferredSize(new Dimension(1024, 768));
	}

	private BasicScrollBarUI makeScrollBarUi(JList<Line> lines, List<Action> scrollActions, boolean isLeftSide) {
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
					if (a.type() == Action.Type.UNCHANGED || a.type() == Action.Type.UPDATED) continue;

					Rectangle r = lines.getCellBounds(i, i);
					Rectangle s = at.createTransformedShape(r).getBounds();
					int h = 2; //Math.max(2, s.height-2);
					Color color = colorForAction(a, isLeftSide);
					g.setColor(color);
					g.fillRect(trackBounds.x + 2, trackBounds.y + 1 + s.y, trackBounds.width, h);
				}
			}
		};
	}

	private static Color colorForAction(Action action, boolean isDimmed) {
		return switch (action.type()) {
			case ADDED -> isDimmed ? COLOR_ADDED_DIMMED : COLOR_ADDED;
			case DELETED -> isDimmed ? COLOR_DELETED_DIMMED : COLOR_DELETED;
			case UNCHANGED, UPDATED -> null;
			case NONE -> COLOR_MOVED_DIMMED;
			case MOVED_UNCHANGED, MOVED_UPDATED -> COLOR_MOVED;
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
			if (!shouldParallelScroll) {
				return;
			}
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
			int l = action.left() == null ? -1 : action.left().index();
			int r = action.right() == null ? -1 : action.right().index();

			setToolTipText(String.format("%s %d - %d - %.2f %d", action.type(), l, r, action.sim(), logLine.hash()));

			Color color = LogsPanel.colorForAction(action, isLeftSide);
			String text = textForAction(action, logLine, isLeftSide);
			setBackground(color);
			setText(text);

			return res;
		}

		private String textForAction(Action action, Line line, boolean isLeftSide) {
			return switch (action.type()) {
				case ADDED -> line.displayValue();
				case DELETED -> line.displayValue();
				case UNCHANGED, MOVED_UNCHANGED -> line.displayValue();
				case UPDATED, MOVED_UPDATED -> toHtml(action, line);
				case NONE -> " ";
			};
		}

		private String toHtml(Action action, Line line) {
			String otherText;
			try {
				otherText = this.cellActions == actions.left() ? action.right().value() : action.left().value();
			} catch (NullPointerException e) {
				otherText = "";
			}
			final String[] tokens = line.value().strip().split("\\s+");
			final String[] otherTokens = otherText.strip().split("\\s+");
			final StringBuilder b = new StringBuilder();
			b.append("<html>" + (line.index() < 0 ? "" : line.index()) + "&nbsp;");
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
			shouldParallelScroll = action.type() != Action.Type.MOVED_UPDATED && action.type() != Action.Type.MOVED_UNCHANGED;
			rightLines.setSelectedValue(action.right(), true);
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
			shouldParallelScroll = action.type() != Action.Type.MOVED_UPDATED && action.type() != Action.Type.MOVED_UNCHANGED;
			leftLines.setSelectedValue(action.left(), true);
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
				if (e.isAltDown() && lastGreen > 0 && actions.right().get(lastGreen).type() == Action.Type.ADDED) {
					while (lastGreen < rightLines.getModel().getSize() && actions.right().get(lastGreen).type() == Action.Type.ADDED) {
						lastGreen++;
					}
					lastGreen--;
					rightLines.setSelectedValue(rightLines.getModel().getElementAt(lastGreen), true);
				} else {
					int again = 0;
					while (again < 2) {
						for (int j = lastGreen + 1; j < rightLines.getModel().getSize(); j++) {
							if (actions.right().get(j).type() == Action.Type.ADDED) {
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
				if (e.isAltDown() && lastGreen < rightLines.getModel().getSize() && actions.right().get(lastGreen).type() == Action.Type.ADDED) {
					while (lastGreen > 0 && actions.right().get(lastGreen).type() == Action.Type.ADDED) {
						lastGreen--;
					}
					lastGreen++;
					rightLines.setSelectedValue(rightLines.getModel().getElementAt(lastGreen), true);
				} else {
					int again = 0;
					while (again < 2) {
						for (int j = lastGreen - 1; j > 0; j--) {
							if (actions.right().get(j).type() == Action.Type.ADDED) {
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
