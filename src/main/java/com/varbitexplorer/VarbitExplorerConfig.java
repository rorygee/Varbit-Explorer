package com.varbitexplorer;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("varbitexplorer")
public interface VarbitExplorerConfig extends Config
{
	@ConfigItem(
		keyName = "outputDirectory",
		name = "Output Directory",
		description = "Directory for JSONL/CSV export files"
	)
	default String outputDirectory()
	{
		return "";
	}

	@ConfigItem(
		keyName = "maxSnapshots",
		name = "Max Snapshots",
		description = "Maximum number of snapshots kept in memory"
	)
	default int maxSnapshots()
	{
		return 10;
	}

	@ConfigItem(
		keyName = "scopeRadius",
		name = "Scope Radius",
		description = "Tiles from the scoped anchor within which events/snapshots are recorded"
	)
	default int scopeRadius()
	{
		return 24;
	}
}
