package com.varbitexplorer.export;

import java.io.File;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public final class ExportFileNamer
{
	private static final DateTimeFormatter SESSION_FMT = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss").withZone(ZoneOffset.UTC);

	private final ExportClock clock;

	public ExportFileNamer(ExportClock clock)
	{
		this.clock = clock;
	}

	public String newSessionTag()
	{
		long now = clock.nowEpochMs();
		return SESSION_FMT.format(Instant.ofEpochMilli(now));
	}

	public ExportPaths paths(File directory, String sessionTag)
	{
		String base = "varbit-explorer-" + sessionTag;
		File events = new File(directory, base + "-events.jsonl");
		File snaps = new File(directory, base + "-snapshots.jsonl");
		File diff = new File(directory, base + "-diff.csv");
		return new ExportPaths(events, snaps, diff);
	}
}
