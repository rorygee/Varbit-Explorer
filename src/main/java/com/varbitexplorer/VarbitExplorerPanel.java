package com.varbitexplorer;

import com.varbitexplorer.model.LogEntry;
import com.varbitexplorer.logic.SnapshotLabels;
import com.varbitexplorer.ui.LogTableModel;
import com.varbitexplorer.ui.LogWindow;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.function.Consumer;

public class VarbitExplorerPanel extends PluginPanel
{
	private static final String FILTER_PLACEHOLDER = "Filter…";

	private final LogTableModel tableModel = new LogTableModel();
	private final JTable sidebarTable = new JTable(tableModel);
	private final TableRowSorter<LogTableModel> sidebarSorter = new TableRowSorter<>(tableModel);
	private LogWindow logWindow;

	private final JTextField watchField = new JTextField();
	private final JLabel watchSummaryLabel = new JLabel("Watching: 0 varbits, 0 varps");
	private final JLabel scopeLabel = new JLabel("Scope: OFF");

	private final JTextField filterField = new JTextField(FILTER_PLACEHOLDER);
	private final JLabel statusLabel = new JLabel("Capturing: OFF");
	private final JTextArea helpText = new JTextArea();

	private Runnable startAction;
	private Runnable stopAction;
	private Runnable snapshotAction;
	private Runnable diffAction;
	private Runnable mapAction;
	private Runnable exportAction;
	private Runnable clearAction;
	private Runnable scopeHereAction;
	private Runnable scopeClearAction;
	private Consumer<String> watchApplyAction;
	private Runnable presetTransmitAction;
	private Runnable disposeAction;

	public VarbitExplorerPanel()
	{
		setLayout(new BorderLayout());
		setBackground(ColorScheme.DARK_GRAY_COLOR);

		add(buildTopSection(), BorderLayout.NORTH);
		add(buildTableSection(), BorderLayout.CENTER);

		append(new LogEntry(System.currentTimeMillis(), 0, "INFO", "", "", "", "Varbit Explorer ready"));
	}

	private JComponent buildTopSection()
	{
		JPanel container = new JPanel();
		container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
		container.setBackground(ColorScheme.DARK_GRAY_COLOR);

		container.add(buildToolbar());
		container.add(buildWatchRow());
		container.add(buildScopeRow());
		container.add(buildFilterRow());
		container.add(buildHelpText());
		container.add(buildDivider());

		return container;
	}

	private JComponent buildToolbar()
	{
		// Two-row toolbar so everything remains reachable on the narrow sidebar.
		JPanel wrapper = new JPanel();
		wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
		wrapper.setBackground(ColorScheme.DARK_GRAY_COLOR);

		JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 4));
		row1.setBackground(ColorScheme.DARK_GRAY_COLOR);
		row1.add(createButton("Start", e -> run(startAction)));
		row1.add(createButton("Stop", e -> run(stopAction)));
		row1.add(createButton("Snapshot", e -> run(snapshotAction)));
		row1.add(createButton("Diff", e -> run(diffAction)));

		JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
		row2.setBackground(ColorScheme.DARK_GRAY_COLOR);
		row2.add(createButton("Map", e -> run(mapAction)));
		row2.add(createButton("Export", e -> run(exportAction)));
		row2.add(createButton("Clear", e -> run(clearAction)));
		row2.add(createButton("Log", e -> openLogWindow()));

		JPanel statusRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
		statusRow.setBackground(ColorScheme.DARK_GRAY_COLOR);
		statusLabel.setForeground(Color.WHITE);
		statusRow.add(statusLabel);

		wrapper.add(row1);
		wrapper.add(row2);
		wrapper.add(statusRow);
		return wrapper;
	}

	private JComponent buildWatchRow()
	{
		// Two-line watch area so the summary label doesn't steal width from the input.
		JPanel wrapper = new JPanel();
		wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
		wrapper.setBackground(ColorScheme.DARK_GRAY_COLOR);
		wrapper.setBorder(BorderFactory.createEmptyBorder(2, 4, 0, 4));

		JPanel row1 = new JPanel(new BorderLayout(4, 0));
		row1.setBackground(ColorScheme.DARK_GRAY_COLOR);

		JLabel label = new JLabel("Watch:");
		label.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
		row1.add(label, BorderLayout.WEST);

		watchField.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		watchField.setForeground(Color.WHITE);
		watchField.setCaretColor(Color.WHITE);
		watchField.setToolTipText("e.g. varbit:4771-4775, varp:123,456-460 (bare numbers follow last type; default varbit)");
		watchField.addActionListener(e -> applyWatch());
		watchField.addFocusListener(new java.awt.event.FocusAdapter()
		{
			@Override
			public void focusLost(java.awt.event.FocusEvent e)
			{
				applyWatch();
			}
		});

		row1.add(watchField, BorderLayout.CENTER);

		JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
		right.setBackground(ColorScheme.DARK_GRAY_COLOR);
		JButton preset = createButton("A–E", e -> run(presetTransmitAction));
		preset.setToolTipText("Transmit A–E (4771–4775)");
		right.add(preset);
		row1.add(right, BorderLayout.EAST);

		JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
		row2.setBackground(ColorScheme.DARK_GRAY_COLOR);
		row2.setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
		watchSummaryLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
		row2.add(watchSummaryLabel);

		wrapper.add(row1);
		wrapper.add(row2);
		return wrapper;
	}

	private void applyWatch()
	{
		if (watchApplyAction != null)
		{
			watchApplyAction.accept(watchField.getText());
		}
	}

	private JComponent buildScopeRow()
	{
		JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 4));
		row.setBackground(ColorScheme.DARK_GRAY_COLOR);
		row.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));

		row.add(createButton("Scope: Here", e -> run(scopeHereAction)));
		row.add(createButton("Clear Scope", e -> run(scopeClearAction)));

		scopeLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
		row.add(Box.createHorizontalStrut(8));
		row.add(scopeLabel);

		return row;
	}

	private static void run(Runnable r)
	{
		if (r != null)
		{
			r.run();
		}
	}

	private JButton createButton(String label, java.awt.event.ActionListener listener)
	{
		JButton btn = new JButton(label);
		btn.setFocusable(false);
		btn.setMargin(new Insets(2, 8, 2, 8));
		btn.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		btn.addActionListener(listener);
		return btn;
	}

	private JComponent buildFilterRow()
	{
		JPanel row = new JPanel(new BorderLayout());
		row.setBackground(ColorScheme.DARK_GRAY_COLOR);
		row.setBorder(BorderFactory.createEmptyBorder(4, 4, 0, 4));

		filterField.setForeground(ColorScheme.MEDIUM_GRAY_COLOR);
		filterField.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		filterField.setCaretColor(Color.WHITE);

		filterField.addFocusListener(new java.awt.event.FocusAdapter()
		{
			@Override
			public void focusGained(java.awt.event.FocusEvent e)
			{
				if (FILTER_PLACEHOLDER.equals(filterField.getText()))
				{
					filterField.setText("");
					filterField.setForeground(Color.WHITE);
				}
			}

			@Override
			public void focusLost(java.awt.event.FocusEvent e)
			{
				if (filterField.getText().trim().isEmpty())
				{
					filterField.setText(FILTER_PLACEHOLDER);
					filterField.setForeground(ColorScheme.MEDIUM_GRAY_COLOR);
				}
			}
		});

		filterField.getDocument().addDocumentListener(new DocumentListener()
		{
			@Override public void insertUpdate(DocumentEvent e) { applyFilter(); }
			@Override public void removeUpdate(DocumentEvent e) { applyFilter(); }
			@Override public void changedUpdate(DocumentEvent e) { applyFilter(); }
		});

		row.add(filterField, BorderLayout.CENTER);
		return row;
	}

	private JComponent buildHelpText()
	{
		helpText.setText(
			"Workflow:\n" +
			"1) Scope: Here near the patch.\n" +
			"2) Watch: enter varbit/varp IDs (ranges ok).\n" +
			"3) Snapshot → label empty / planted / diseased …\n" +
			"4) Diff → shows what changed. Export → JSONL/CSV.\n\n" +
			"Watch syntax examples:\n" +
			"- varbit:4771-4775\n" +
			"- varp:123,456-460\n" +
			"- 4774 (bare tokens follow last type; default varbit)\n"
		);
		helpText.setWrapStyleWord(true);
		helpText.setLineWrap(true);
		helpText.setEditable(false);
		helpText.setFocusable(false);
		helpText.setOpaque(true);
		helpText.setBackground(ColorScheme.DARK_GRAY_COLOR);
		helpText.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
		helpText.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		return helpText;
	}

	private JComponent buildDivider()
	{
		JPanel divider = new JPanel();
		divider.setPreferredSize(new Dimension(1, 1));
		divider.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
		divider.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		return divider;
	}

	private JComponent buildTableSection()
	{
		// Sidebar log is compact: tick / type / notes only.
		sidebarTable.setRowSorter(sidebarSorter);
		sidebarTable.setFillsViewportHeight(true);
		sidebarTable.setBackground(ColorScheme.DARK_GRAY_COLOR);
		sidebarTable.setForeground(Color.WHITE);
		sidebarTable.setGridColor(ColorScheme.DARKER_GRAY_COLOR);
		sidebarTable.setSelectionBackground(ColorScheme.DARKER_GRAY_COLOR);
		sidebarTable.setSelectionForeground(Color.WHITE);

		hideSidebarColumns();

		JScrollPane scrollPane = new JScrollPane(sidebarTable);
		scrollPane.setBorder(null);
		scrollPane.getViewport().setBackground(ColorScheme.DARK_GRAY_COLOR);
		return scrollPane;
	}

	private void hideSidebarColumns()
	{
		// Model columns: Time, Tick, Type, ID, Old, New, Notes
		// Keep: Tick, Type, Notes. Remove others from the view.
		// Remove from highest index downward.
		sidebarTable.removeColumn(sidebarTable.getColumnModel().getColumn(5)); // New
		sidebarTable.removeColumn(sidebarTable.getColumnModel().getColumn(4)); // Old
		sidebarTable.removeColumn(sidebarTable.getColumnModel().getColumn(3)); // ID
		sidebarTable.removeColumn(sidebarTable.getColumnModel().getColumn(0)); // Time

		// Now view columns are: Tick (model 1), Type (model 2), Notes (model 6)
		// Nudge widths so notes gets the space.
		if (sidebarTable.getColumnModel().getColumnCount() >= 3)
		{
			sidebarTable.getColumnModel().getColumn(0).setPreferredWidth(42);
			sidebarTable.getColumnModel().getColumn(1).setPreferredWidth(92);
			sidebarTable.getColumnModel().getColumn(2).setPreferredWidth(600);
		}
	}

	private void applyFilter()
	{
		String text = filterField.getText();
		if (FILTER_PLACEHOLDER.equals(text) || text.trim().isEmpty())
		{
			sidebarSorter.setRowFilter(null);
			if (logWindow != null)
			{
				logWindow.setRowFilter(null);
			}
			return;
		}

		final String needle = text.trim().toLowerCase();
		RowFilter<LogTableModel, Integer> rf = new RowFilter<LogTableModel, Integer>()
		{
			@Override
			public boolean include(Entry<? extends LogTableModel, ? extends Integer> entry)
			{
				for (int i = 0; i < entry.getValueCount(); i++)
				{
					Object v = entry.getValue(i);
					if (v != null && v.toString().toLowerCase().contains(needle))
					{
						return true;
					}
				}
				return false;
			}
		};

		sidebarSorter.setRowFilter(rf);
		if (logWindow != null)
		{
			logWindow.setRowFilter(rf);
		}
	}

	public void append(LogEntry entry)
	{
		SwingUtilities.invokeLater(() ->
		{
			tableModel.addEntry(entry);
			int lastRow = tableModel.getRowCount() - 1;
			if (lastRow >= 0)
			{
				int viewRow = sidebarTable.convertRowIndexToView(lastRow);
				if (viewRow >= 0)
				{
					sidebarTable.scrollRectToVisible(sidebarTable.getCellRect(viewRow, 0, true));
				}
				if (logWindow != null)
				{
					logWindow.scrollToRow(lastRow);
				}
			}
		});
	}

	public void clear()
	{
		SwingUtilities.invokeLater(tableModel::clear);
	}

	public void disposeWindows()
	{
		SwingUtilities.invokeLater(() ->
		{
			if (logWindow != null)
			{
				logWindow.dispose();
				logWindow = null;
			}
			if (disposeAction != null)
			{
				disposeAction.run();
			}
		});
	}

	public void setCapturing(boolean capturing)
	{
		SwingUtilities.invokeLater(() -> statusLabel.setText("Capturing: " + (capturing ? "ON" : "OFF")));
	}

	public void setWatchSummary(int varbits, int varps)
	{
		SwingUtilities.invokeLater(() -> watchSummaryLabel.setText("Watching: " + varbits + " varbits, " + varps + " varps"));
	}

	public void setScopeSummary(String text)
	{
		SwingUtilities.invokeLater(() -> scopeLabel.setText(text));
	}

	public String promptForSnapshotLabel(String defaultLabel)
	{
		String label = (String) JOptionPane.showInputDialog(
			this,
			"Snapshot label:",
			"Snapshot",
			JOptionPane.PLAIN_MESSAGE,
			null,
			null,
			defaultLabel
		);
		return SnapshotLabels.normalize(label, defaultLabel);
	}

	public void setWatchText(String text)
	{
		SwingUtilities.invokeLater(() -> watchField.setText(text));
	}

	public String getWatchText()
	{
		return watchField.getText();
	}

	public void setStartAction(Runnable startAction)
	{
		this.startAction = startAction;
	}

	public void setStopAction(Runnable stopAction)
	{
		this.stopAction = stopAction;
	}

	public void setSnapshotAction(Runnable snapshotAction)
	{
		this.snapshotAction = snapshotAction;
	}

	public void setDiffAction(Runnable diffAction)
	{
		this.diffAction = diffAction;
	}

	public void setMapAction(Runnable mapAction)
	{
		this.mapAction = mapAction;
	}

	public void setExportAction(Runnable exportAction)
	{
		this.exportAction = exportAction;
	}

	public void setClearAction(Runnable clearAction)
	{
		this.clearAction = clearAction;
	}

	public void setScopeHereAction(Runnable scopeHereAction)
	{
		this.scopeHereAction = scopeHereAction;
	}

	public void setScopeClearAction(Runnable scopeClearAction)
	{
		this.scopeClearAction = scopeClearAction;
	}

	public void setWatchApplyAction(Consumer<String> watchApplyAction)
	{
		this.watchApplyAction = watchApplyAction;
	}

	public void setPresetTransmitAction(Runnable presetTransmitAction)
	{
		this.presetTransmitAction = presetTransmitAction;
	}

	public void setDisposeAction(Runnable disposeAction)
	{
		this.disposeAction = disposeAction;
	}

	private void openLogWindow()
	{
		SwingUtilities.invokeLater(() ->
		{
			if (logWindow != null)
			{
				logWindow.toFront();
				return;
			}

			Window owner = SwingUtilities.getWindowAncestor(this);
			logWindow = new LogWindow(owner, tableModel);
			// Apply current filter so both views match.
			applyFilter();
			logWindow.addWindowClosedListener(() -> logWindow = null);
			logWindow.setVisible(true);
		});
	}
}
