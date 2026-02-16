package com.varbitexplorer.model;

public class LogEntry
{
	public final long timestampEpochMs;
	public final String type;
	public final String id;
	public final String oldValue;
	public final String newValue;
	public final String notes;

	public LogEntry(long timestampEpochMs, String type, String id, String oldValue, String newValue, String notes)
	{
		this.timestampEpochMs = timestampEpochMs;
		this.type = type;
		this.id = id;
		this.oldValue = oldValue;
		this.newValue = newValue;
		this.notes = notes;
	}
}
