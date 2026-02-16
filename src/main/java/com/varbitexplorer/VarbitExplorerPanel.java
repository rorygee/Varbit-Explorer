package com.varbitexplorer;

import com.varbitexplorer.model.LogEntry;
import com.varbitexplorer.ui.LogTableModel;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableRowSorter;
import java.awt.*;

public class VarbitExplorerPanel extends PluginPanel
{
	private static final String FILTER_PLACEHOLDER = "Filter…";

	private final LogTableModel tableModel = new LogTableModel();
	private final JTable table = new JTable(tableModel);
	private final TableRowSorter<LogTableModel> sorter = new TableRowSorter<>(tableModel);

	private final JTextField filterField = new JTextField(FILTER_PLACEHOLDER);
	private final JLabel statusLabel = new JLabel("Capturing: OFF");
	private final JTextArea helpText = new JTextArea();

	private Runnable startAction;
	private Runnable stopAction;
	private Runnable snapshotAction;
	private Runnable diffAction;
	private Runnable exportAction;
	private Runnable clearAction;

	public VarbitExplorerPanel()
	{
		setLayout(new BorderLayout());
		setBackground(ColorScheme.DARK_GRAY_COLOR);

		add(buildTopSection(), BorderLayout.NORTH);
		add(buildTableSection(), BorderLayout.CENTER);

		// starter line so you can see the log immediately
		append(new LogEntry(System.currentTimeMillis(), "INFO", "", "", "", "Varbit Explorer ready"));
	}

	private JComponent buildTopSection()
	{
		JPanel container = new JPanel();
		container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
		container.setBackground(ColorScheme.DARK_GRAY_COLOR);

		container.add(buildToolbar());
		container.add(buildFilterRow());
		container.add(buildHelpText());
		container.add(buildDivider());

		return container;
	}

	private JComponent buildToolbar()
	{
		JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 4));
		toolbar.setBackground(ColorScheme.DARK_GRAY_COLOR);

		toolbar.add(createButton("Start", e -> run(startAction)));
		toolbar.add(createButton("Stop", e -> run(stopAction)));
		toolbar.add(createButton("Snapshot", e -> run(snapshotAction)));
		toolbar.add(createButton("Diff", e -> run(diffAction)));
		toolbar.add(createButton("Export", e -> run(exportAction)));
		toolbar.add(createButton("Clear", e -> run(clearAction)));

		toolbar.add(Box.createHorizontalStrut(8));
		statusLabel.setForeground(Color.WHITE);
		toolbar.add(statusLabel);

		return toolbar;
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
			"How to use:\n" +
			"1) Click Start, then perform an in-game action.\n" +
			"2) Varps will be logged while capturing.\n" +
			"3) Use Snapshot/Diff later to compare states.\n"
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
		table.setRowSorter(sorter);
		table.setFillsViewportHeight(true);
		table.setBackground(ColorScheme.DARK_GRAY_COLOR);
		table.setForeground(Color.WHITE);
		table.setGridColor(ColorScheme.DARKER_GRAY_COLOR);
		table.setSelectionBackground(ColorScheme.DARKER_GRAY_COLOR);
		table.setSelectionForeground(Color.WHITE);

		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setBorder(null);
		scrollPane.getViewport().setBackground(ColorScheme.DARK_GRAY_COLOR);
		return scrollPane;
	}

	private void applyFilter()
	{
		String text = filterField.getText();
		if (FILTER_PLACEHOLDER.equals(text) || text.trim().isEmpty())
		{
			sorter.setRowFilter(null);
			return;
		}

		final String needle = text.trim().toLowerCase();
		sorter.setRowFilter(new RowFilter<LogTableModel, Integer>()
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
		});
	}

	public void append(LogEntry entry)
	{
		SwingUtilities.invokeLater(() ->
		{
			tableModel.addEntry(entry);
			int lastRow = tableModel.getRowCount() - 1;
			if (lastRow >= 0)
			{
				table.scrollRectToVisible(table.getCellRect(lastRow, 0, true));
			}
		});
	}

	public void clear()
	{
		SwingUtilities.invokeLater(tableModel::clear);
	}

	public void setCapturing(boolean capturing)
	{
		SwingUtilities.invokeLater(() -> statusLabel.setText("Capturing: " + (capturing ? "ON" : "OFF")));
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

	public void setExportAction(Runnable exportAction)
	{
		this.exportAction = exportAction;
	}

	public void setClearAction(Runnable clearAction)
	{
		this.clearAction = clearAction;
	}
}
