package com.varbitexplorer.ui;

import net.runelite.client.ui.ColorScheme;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * A simple pop-out window for the full log table.
 *
 * The sidebar view stays compact; this window is where the full-width columns are usable.
 */
public class LogWindow extends JDialog
{
	private final JTable table;
	private final TableRowSorter<LogTableModel> sorter;
	private Runnable onClosed;

	public LogWindow(Window owner, LogTableModel model)
	{
		super(owner, "Varbit Explorer — Log", ModalityType.MODELESS);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setMinimumSize(new Dimension(920, 520));

		table = new JTable(model);
		sorter = new TableRowSorter<>(model);
		table.setRowSorter(sorter);
		table.setFillsViewportHeight(true);
		table.setBackground(ColorScheme.DARK_GRAY_COLOR);
		table.setForeground(Color.WHITE);
		table.setGridColor(ColorScheme.DARKER_GRAY_COLOR);
		table.setSelectionBackground(ColorScheme.DARKER_GRAY_COLOR);
		table.setSelectionForeground(Color.WHITE);

		// Reasonable defaults; user can still resize columns manually.
		trySetPreferredWidth(0, 110); // Time
		trySetPreferredWidth(1, 60);  // Tick
		trySetPreferredWidth(2, 130); // Type
		trySetPreferredWidth(3, 110); // ID
		trySetPreferredWidth(4, 90);  // Old
		trySetPreferredWidth(5, 90);  // New
		trySetPreferredWidth(6, 600); // Notes

		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.getViewport().setBackground(ColorScheme.DARK_GRAY_COLOR);
		scrollPane.setBorder(BorderFactory.createEmptyBorder());

		setLayout(new BorderLayout());
		add(scrollPane, BorderLayout.CENTER);

		addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosed(WindowEvent e)
			{
				if (onClosed != null)
				{
					onClosed.run();
				}
			}
		});

		// If the owner goes away (plugin disabled), try to close gracefully.
		getRootPane().addAncestorListener(new AncestorListener()
		{
			@Override public void ancestorAdded(AncestorEvent event) { }
			@Override public void ancestorMoved(AncestorEvent event) { }
			@Override public void ancestorRemoved(AncestorEvent event) { dispose(); }
		});

		setLocationRelativeTo(owner);
	}

	public void setRowFilter(RowFilter<? super LogTableModel, ? super Integer> filter)
	{
		sorter.setRowFilter(filter);
	}

	public void scrollToRow(int modelRow)
	{
		int viewRow = table.convertRowIndexToView(modelRow);
		if (viewRow < 0)
		{
			return;
		}
		table.scrollRectToVisible(table.getCellRect(viewRow, 0, true));
	}

	public void addWindowClosedListener(Runnable r)
	{
		this.onClosed = r;
	}

	private void trySetPreferredWidth(int viewColumn, int width)
	{
		if (table.getColumnModel().getColumnCount() > viewColumn)
		{
			table.getColumnModel().getColumn(viewColumn).setPreferredWidth(width);
		}
	}
}
