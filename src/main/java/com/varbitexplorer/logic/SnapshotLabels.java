package com.varbitexplorer.logic;

public final class SnapshotLabels
{
	private SnapshotLabels() {}

	public static String defaultLabel(int index)
	{
		return "snapshot-" + index;
	}

	/**
	 * Normalize a user-provided label.
	 * @return null if cancelled (input == null), otherwise trimmed, with defaultLabel substituted if empty.
	 */
	public static String normalize(String input, String defaultLabel)
	{
		if (input == null)
		{
			return null;
		}
		String trimmed = input.trim();
		return trimmed.isEmpty() ? defaultLabel : trimmed;
	}
}
