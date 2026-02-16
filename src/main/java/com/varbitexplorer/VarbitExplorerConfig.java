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
		description = "Directory for JSONL export files"
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
}
