package com.varbitexplorer.export;

public final class SystemExportClock implements ExportClock
{
	@Override
	public long nowEpochMs()
	{
		return System.currentTimeMillis();
	}
}
