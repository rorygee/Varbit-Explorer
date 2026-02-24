package com.varbitexplorer.export;

import java.io.File;

public final class ExportPaths
{
	public final File eventsJsonl;
	public final File snapshotsJsonl;
	public final File diffCsv;

	public ExportPaths(File eventsJsonl, File snapshotsJsonl, File diffCsv)
	{
		this.eventsJsonl = eventsJsonl;
		this.snapshotsJsonl = snapshotsJsonl;
		this.diffCsv = diffCsv;
	}
}
