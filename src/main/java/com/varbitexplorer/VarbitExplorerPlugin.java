package com.varbitexplorer;

import com.google.inject.Provides;
import com.varbitexplorer.model.LogEntry;
import net.runelite.api.Client;
import net.runelite.api.events.VarbitChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;

import javax.inject.Inject;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

@PluginDescriptor(
	name = "Varbit Explorer",
	description = "Inspect varps and watched varbits with logging and diff support.",
	tags = {"varp", "varbit", "debug", "explorer"}
)
public class VarbitExplorerPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private VarbitExplorerConfig config;

	private NavigationButton navButton;
	private VarbitExplorerPanel panel;

	private boolean capturing = false;
	private final Map<Integer, Integer> lastVarps = new HashMap<>();

	@Provides
	VarbitExplorerConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(VarbitExplorerConfig.class);
	}

	@Override
	protected void startUp()
	{
		panel = new VarbitExplorerPanel();
		panel.setStartAction(this::startCapture);
		panel.setStopAction(this::stopCapture);
		panel.setClearAction(panel::clear);

		// Placeholders for later phases
		panel.setSnapshotAction(() -> panel.append(new LogEntry(System.currentTimeMillis(), "SNAPSHOT", "", "", "", "Not implemented yet")));
		panel.setDiffAction(() -> panel.append(new LogEntry(System.currentTimeMillis(), "DIFF", "", "", "", "Not implemented yet")));
		panel.setExportAction(() -> panel.append(new LogEntry(System.currentTimeMillis(), "EXPORT", "", "", "", "Not implemented yet")));

		final BufferedImage icon = ImageUtil.loadImageResource(getClass(), "/icon.png");

		navButton = NavigationButton.builder()
			.tooltip("Varbit Explorer")
			.icon(icon)
			.priority(5)
			.panel(panel)
			.build();

		clientToolbar.addNavigation(navButton);
	}

	@Override
	protected void shutDown()
	{
		clientToolbar.removeNavigation(navButton);
		capturing = false;
		lastVarps.clear();
	}

	private void startCapture()
	{
		if (capturing)
		{
			return;
		}

		capturing = true;
		panel.setCapturing(true);
		panel.append(new LogEntry(System.currentTimeMillis(), "START", "", "", "", "Capture started"));

		// Clear any stale baseline. We baseline lazily on first varp change events.
		lastVarps.clear();
	}

	private void stopCapture()
	{
		if (!capturing)
		{
			return;
		}

		capturing = false;
		panel.setCapturing(false);
		panel.append(new LogEntry(System.currentTimeMillis(), "STOP", "", "", "", "Capture stopped"));
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged event)
	{
		if (!capturing)
		{
			return;
		}

		// Varps/VarPlayers come through with varbitId == -1
		if (event.getVarbitId() != -1)
		{
			return;
		}

		final int varpId = event.getVarpId();
		final int newValue = event.getValue();

		Integer oldValue = lastVarps.put(varpId, newValue);
		if (oldValue == null)
		{
			// baseline only; don't spam a log line the first time we see a varp change
			return;
		}

		if (oldValue == newValue)
		{
			return;
		}

		panel.append(new LogEntry(
			System.currentTimeMillis(),
			"VARP_CHANGED",
			String.valueOf(varpId),
			String.valueOf(oldValue),
			String.valueOf(newValue),
			""
		));
	}
}
