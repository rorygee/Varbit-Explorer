package com.varbitexplorer.ui;

import com.varbitexplorer.model.LogEntry;

import javax.swing.table.AbstractTableModel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class LogTableModel extends AbstractTableModel
{
	private static final String[] COLUMNS = {"Time", "Type", "ID", "Old", "New", "Notes"};

	private final List<LogEntry> entries = new ArrayList<>();
	private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss.SSS");

	@Override
	public int getRowCount()
	{
		return entries.size();
	}

	@Override
	public int getColumnCount()
	{
		return COLUMNS.length;
	}

	@Override
	public String getColumnName(int column)
	{
		return COLUMNS[column];
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex)
	{
		LogEntry e = entries.get(rowIndex);
		switch (columnIndex)
		{
			case 0:
				return timeFormat.format(new Date(e.timestampEpochMs));
			case 1:
				return e.type;
			case 2:
				return e.id;
			case 3:
				return e.oldValue;
			case 4:
				return e.newValue;
			case 5:
				return e.notes;
			default:
				return "";
		}
	}

	public void addEntry(LogEntry entry)
	{
		int row = entries.size();
		entries.add(entry);
		fireTableRowsInserted(row, row);
	}

	public void clear()
	{
		int size = entries.size();
		entries.clear();
		if (size > 0)
		{
			fireTableRowsDeleted(0, size - 1);
		}
	}
}
