package com.varbitexplorer;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class VarbitExplorerPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(VarbitExplorerPlugin.class);
		RuneLite.main(args);
	}
}
