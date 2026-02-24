package com.varbitexplorer.export;

import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;

public class ExportFileNamerTest
{
	@Test
	public void sessionTagIsDeterministicWithFixedClock()
	{
		ExportFileNamer namer = new ExportFileNamer(() -> 0L);
		assertEquals("19700101-000000", namer.newSessionTag());
	}

	@Test
	public void pathsAreStable()
	{
		ExportFileNamer namer = new ExportFileNamer(() -> 0L);
		String tag = namer.newSessionTag();
		ExportPaths p = namer.paths(new File("/tmp"), tag);
		assertTrue(p.eventsJsonl.getName().contains("events.jsonl"));
		assertTrue(p.snapshotsJsonl.getName().contains("snapshots.jsonl"));
		assertTrue(p.diffCsv.getName().contains("diff.csv"));
	}
}
