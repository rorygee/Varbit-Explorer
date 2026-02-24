package com.varbitexplorer.export;

import com.varbitexplorer.model.DiffResult;
import com.varbitexplorer.model.EventRecord;
import com.varbitexplorer.model.Snapshot;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

public final class JsonlExporter
{
	private final ExportPaths paths;

	public JsonlExporter(ExportPaths paths)
	{
		this.paths = paths;
	}

	public ExportPaths getPaths()
	{
		return paths;
	}

	public void export(List<EventRecord> events, List<Snapshot> snapshots, DiffResult lastDiff) throws IOException
	{
		ensureParent(paths.eventsJsonl);
		ensureParent(paths.snapshotsJsonl);
		ensureParent(paths.diffCsv);

		appendEvents(events);
		overwriteSnapshots(snapshots);
		overwriteDiff(lastDiff);
	}

	private void appendEvents(List<EventRecord> events) throws IOException
	{
		if (events == null || events.isEmpty())
		{
			return;
		}

		try (BufferedWriter w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(paths.eventsJsonl, true), StandardCharsets.UTF_8)))
		{
			for (EventRecord e : events)
			{
				w.write(ExportFormatter.formatEvent(e));
				w.newLine();
			}
		}
	}

	private void overwriteSnapshots(List<Snapshot> snapshots) throws IOException
	{
		try (BufferedWriter w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(paths.snapshotsJsonl, false), StandardCharsets.UTF_8)))
		{
			if (snapshots != null)
			{
				for (Snapshot s : snapshots)
				{
					w.write(ExportFormatter.formatSnapshot(s));
					w.newLine();
				}
			}
		}
	}

	private void overwriteDiff(DiffResult diff) throws IOException
	{
		if (diff == null)
		{
			return;
		}

		try (BufferedWriter w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(paths.diffCsv, false), StandardCharsets.UTF_8)))
		{
			w.write(ExportFormatter.formatDiffCsvHeader());
			w.newLine();
			for (com.varbitexplorer.model.DiffChange c : diff.changes)
			{
				w.write(ExportFormatter.formatDiffCsvRow(c));
				w.newLine();
			}
		}
	}

	private static void ensureParent(File f) throws IOException
	{
		File parent = f.getParentFile();
		if (parent != null && !parent.exists())
		{
			if (!parent.mkdirs() && !parent.exists())
			{
				throw new IOException("Failed to create directory: " + parent.getAbsolutePath());
			}
		}
	}
}
