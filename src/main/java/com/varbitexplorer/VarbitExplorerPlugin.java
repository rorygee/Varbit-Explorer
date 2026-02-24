package com.varbitexplorer;

import com.google.inject.Provides;
import com.varbitexplorer.export.ExportFileNamer;
import com.varbitexplorer.export.ExportPaths;
import com.varbitexplorer.export.JsonlExporter;
import com.varbitexplorer.export.SystemExportClock;
import com.varbitexplorer.logic.IdType;
import com.varbitexplorer.logic.MappingAggregator;
import com.varbitexplorer.logic.Scope;
import com.varbitexplorer.logic.SnapshotDiff;
import com.varbitexplorer.logic.SnapshotStore;
import com.varbitexplorer.logic.WatchSpec;
import com.varbitexplorer.logic.WatchSpecFormatter;
import com.varbitexplorer.logic.WatchSpecParseResult;
import com.varbitexplorer.logic.WatchSpecParser;
import com.varbitexplorer.model.DiffChange;
import com.varbitexplorer.model.DiffResult;
import com.varbitexplorer.model.EventRecord;
import com.varbitexplorer.model.LogEntry;
import com.varbitexplorer.model.ScopeState;
import com.varbitexplorer.model.Snapshot;
import com.varbitexplorer.model.WorldPointInfo;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Player;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.client.RuneLite;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;

import javax.inject.Inject;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ScheduledExecutorService;

@PluginDescriptor(
	name = "Varbit Explorer",
	description = "Inspect watched varbits/varps with scoped logging, snapshots, diffs, and export.",
	tags = {"varp", "varbit", "debug", "explorer"}
)
public class VarbitExplorerPlugin extends Plugin
{
	private static final int[] TRANSMIT_A_E = {4771, 4772, 4773, 4774, 4775};

	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private ScheduledExecutorService executor;

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private VarbitExplorerConfig config;

	private NavigationButton navButton;
	private VarbitExplorerPanel panel;

	private boolean capturing = false;
	private int tick = 0;

	private WatchSpec watchSpec = WatchSpec.empty();
	private Scope scope;

	private final Map<Integer, Integer> lastVarps = new HashMap<>();
	private final Map<Integer, Integer> lastVarbits = new HashMap<>();

	private final SnapshotStore snapshotStore = new SnapshotStore();
	private DiffResult lastDiff;

	private final List<EventRecord> events = new ArrayList<>();
	private int exportedEventCount = 0;

	private final ExportFileNamer fileNamer = new ExportFileNamer(new SystemExportClock());
	private String sessionTag;

	@Provides
	VarbitExplorerConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(VarbitExplorerConfig.class);
	}

	@Override
	protected void startUp()
	{
		sessionTag = fileNamer.newSessionTag();

		panel = new VarbitExplorerPanel();
		panel.setStartAction(this::startCapture);
		panel.setStopAction(this::stopCapture);
		panel.setClearAction(this::clearAll);
		panel.setSnapshotAction(this::snapshot);
		panel.setDiffAction(this::diffLatest);
		panel.setMapAction(this::mapSummary);
		panel.setExportAction(this::export);
		panel.setScopeHereAction(this::scopeHere);
		panel.setScopeClearAction(this::clearScope);
		panel.setWatchApplyAction(this::applyWatchText);
		panel.setPresetTransmitAction(this::applyTransmitPreset);

		panel.setWatchSummary(0, 0);
		panel.setScopeSummary("Scope: OFF");

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
		if (panel != null)
		{
			panel.disposeWindows();
		}
		clientToolbar.removeNavigation(navButton);
		capturing = false;
		lastVarps.clear();
		lastVarbits.clear();
		snapshotStore.clear();
		events.clear();
		exportedEventCount = 0;
		scope = null;
		watchSpec = WatchSpec.empty();
		lastDiff = null;
		panel = null;
	}

	@Subscribe
	public void onGameTick(GameTick t)
	{
		tick++;
		// Prefer tick-based scanning over relying on VarbitChanged.varbitId, which is often unset (-1)
		// even when underlying varp bitfields (varbits) have changed.
		scanWatchedValuesOnTick();
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged e)
	{
		if (e.getGameState() == GameState.LOGGED_IN)
		{
			// Scope is a local convenience, not global memory.
			scope = null;
			lastVarps.clear();
			lastVarbits.clear();
			tick = 0;
			if (panel != null)
			{
				panel.setScopeSummary("Scope: OFF");
				panel.append(new LogEntry(System.currentTimeMillis(), tick, "INFO", "", "", "", "Scope cleared (login)"));
			}
		}
	}

	private void startCapture()
	{
		if (capturing)
		{
			return;
		}

		capturing = true;
		panel.setCapturing(true);
		panel.append(new LogEntry(System.currentTimeMillis(), tick, "START", "", "", "", "Capture started"));

		lastVarps.clear();
		lastVarbits.clear();

		if (watchSpec.isEmpty())
		{
			panel.append(new LogEntry(System.currentTimeMillis(), tick, "INFO", "", "", "", "Watch is empty (no events will log)"));
		}
		if (scope == null || !scope.isActive())
		{
			panel.append(new LogEntry(System.currentTimeMillis(), tick, "INFO", "", "", "", "No scope set (may be noisy)"));
		}

		// fresh session file set for each capture start
		sessionTag = fileNamer.newSessionTag();
		events.clear();
		exportedEventCount = 0;
		lastDiff = null;
	}

	private void stopCapture()
	{
		if (!capturing)
		{
			return;
		}

		capturing = false;
		panel.setCapturing(false);
		panel.append(new LogEntry(System.currentTimeMillis(), tick, "STOP", "", "", "", "Capture stopped"));
	}

	private void clearAll()
	{
		lastVarps.clear();
		lastVarbits.clear();
		snapshotStore.clear();
		lastDiff = null;
		events.clear();
		exportedEventCount = 0;
		panel.clear();
		panel.append(new LogEntry(System.currentTimeMillis(), tick, "INFO", "", "", "", "Cleared"));
	}

	private void applyWatchText(String text)
	{
		WatchSpecParseResult parsed = WatchSpecParser.parse(text, IdType.VARBIT);
		applyWatchSpec(parsed);
	}

	private void applyWatchSpec(WatchSpecParseResult parsed)
	{
		for (String err : parsed.errors)
		{
			panel.append(new LogEntry(System.currentTimeMillis(), tick, "INFO", "", "", "", err));
		}

		WatchSpec previous = watchSpec;
		watchSpec = parsed.spec;

		// Remove stale baselines for ids no longer watched.
		lastVarbits.keySet().retainAll(watchSpec.getVarbits());
		lastVarps.keySet().retainAll(watchSpec.getVarps());

		panel.setWatchSummary(watchSpec.varbitCount(), watchSpec.varpCount());
		// Keep logs calm: only emit a WATCH row when the spec actually changes.
		if (!watchSpec.equals(previous))
		{
			panel.append(new LogEntry(System.currentTimeMillis(), tick, "WATCH", "", "", "", "Watching: " + watchSpec.varbitCount() + " varbits, " + watchSpec.varpCount() + " varps"));
		}
	}

	private void applyTransmitPreset()
	{
		WatchSpecParseResult parsed = WatchSpecParser.parse(panel.getWatchText(), IdType.VARBIT);
		WatchSpec merged = parsed.spec;
		for (int id : TRANSMIT_A_E)
		{
			merged = merged.withAddedVarbit(id);
		}

		applyWatchSpec(new WatchSpecParseResult(merged, parsed.errors));
		panel.setWatchText(WatchSpecFormatter.format(merged));
	}

	private void scopeHere()
	{
		clientThread.invokeLater(() ->
		{
			Player p = client.getLocalPlayer();
			if (p == null)
			{
				panel.append(new LogEntry(System.currentTimeMillis(), tick, "INFO", "", "", "", "No local player (cannot scope)"));
				return;
			}

			WorldPoint wp = p.getWorldLocation();
			scope = new Scope(wp, config.scopeRadius());
			WorldPointInfo info = WorldPointInfo.from(wp);
			panel.setScopeSummary("Scope: " + info.x + "," + info.y + " p" + info.plane + " r=" + config.scopeRadius());
			panel.append(new LogEntry(System.currentTimeMillis(), tick, "SCOPE", "", "", "", "Scoped to " + info.x + "," + info.y + " p" + info.plane + " r=" + config.scopeRadius()));

			// Baselines are patch-local; re-baseline when scope changes.
			lastVarps.clear();
			lastVarbits.clear();
		});
	}

	private void clearScope()
	{
		scope = null;
		lastVarps.clear();
		lastVarbits.clear();
		panel.setScopeSummary("Scope: OFF");
		panel.append(new LogEntry(System.currentTimeMillis(), tick, "SCOPE", "", "", "", "Scope cleared"));
	}

	private void scanWatchedValuesOnTick()
	{
		if (!capturing)
		{
			return;
		}
		if (watchSpec.isEmpty())
		{
			return;
		}
		Player p = client.getLocalPlayer();
		if (p == null)
		{
			return;
		}
		WorldPoint playerLoc = p.getWorldLocation();
		if (!isInScope(playerLoc))
		{
			return;
		}

		final long ts = System.currentTimeMillis();
		final WorldPointInfo playerInfo = WorldPointInfo.from(playerLoc);
		final ScopeState scopeState = scope == null ? ScopeState.inactive() : scope.toState();

		for (Integer varbitId : watchSpec.getVarbits())
		{
			int newValue = client.getVarbitValue(varbitId);
			Integer oldValue = lastVarbits.put(varbitId, newValue);
			// Baseline suppression: don't log first-seen values.
			if (oldValue == null || oldValue == newValue)
			{
				continue;
			}
			panel.append(new LogEntry(ts, tick, "VARBIT_CHANGED", String.valueOf(varbitId), String.valueOf(oldValue), String.valueOf(newValue), ""));
			events.add(EventRecord.varChanged(ts, tick, IdType.VARBIT, varbitId, oldValue, newValue, playerInfo, scopeState));
		}

		for (Integer varpId : watchSpec.getVarps())
		{
			int newValue = client.getVarpValue(varpId);
			Integer oldValue = lastVarps.put(varpId, newValue);
			if (oldValue == null || oldValue == newValue)
			{
				continue;
			}
			panel.append(new LogEntry(ts, tick, "VARP_CHANGED", String.valueOf(varpId), String.valueOf(oldValue), String.valueOf(newValue), ""));
			events.add(EventRecord.varChanged(ts, tick, IdType.VARP, varpId, oldValue, newValue, playerInfo, scopeState));
		}
	}

	private boolean isInScope(WorldPoint player)
	{
		if (scope == null || !scope.isActive())
		{
			return true;
		}
		return scope.contains(player);
	}

	private void snapshot()
	{
		if (watchSpec.isEmpty())
		{
			panel.append(new LogEntry(System.currentTimeMillis(), tick, "INFO", "", "", "", "Watch is empty"));
			return;
		}

		String defaultLabel = com.varbitexplorer.logic.SnapshotLabels.defaultLabel(snapshotStore.size() + 1);
		String label = panel.promptForSnapshotLabel(defaultLabel);
		if (label == null)
		{
			return;
		}

		clientThread.invokeLater(() ->
		{
			Player p = client.getLocalPlayer();
			if (p == null)
			{
				panel.append(new LogEntry(System.currentTimeMillis(), tick, "INFO", "", "", "", "No local player (cannot snapshot)"));
				return;
			}

			WorldPoint playerLoc = p.getWorldLocation();
			if (!isInScope(playerLoc))
			{
				panel.append(new LogEntry(System.currentTimeMillis(), tick, "INFO", "", "", "", "Out of scope (snapshot suppressed)"));
				return;
			}

			long ts = System.currentTimeMillis();
			WorldPointInfo playerInfo = WorldPointInfo.from(playerLoc);
			ScopeState scopeState = scope == null ? ScopeState.inactive() : scope.toState();

			SortedMap<Integer, Integer> varbits = new TreeMap<>();
			for (Integer id : watchSpec.getVarbits())
			{
				varbits.put(id, client.getVarbitValue(id));
			}

			SortedMap<Integer, Integer> varps = new TreeMap<>();
			for (Integer id : watchSpec.getVarps())
			{
				varps.put(id, client.getVarpValue(id));
			}

			Snapshot s = new Snapshot(ts, tick, label, playerInfo, scopeState, varbits, varps);
			snapshotStore.add(s, config.maxSnapshots());
			panel.append(new LogEntry(ts, s.tick, "SNAPSHOT", "", "", "", label + " (" + varbits.size() + " varbits, " + varps.size() + " varps)"));
			events.add(EventRecord.snapshot(ts, s.tick, label, playerInfo, scopeState));
		});
	}

	private void diffLatest()
	{
		clientThread.invokeLater(() ->
		{
			List<Snapshot> snaps = snapshotStore.list();
			if (snaps.size() < 2)
			{
				panel.append(new LogEntry(System.currentTimeMillis(), tick, "INFO", "", "", "", "Need at least 2 snapshots"));
				return;
			}

			Snapshot b = snaps.get(snaps.size() - 1);
			Snapshot a = chooseDiffA(snaps, b);
			if (a == null)
			{
				panel.append(new LogEntry(System.currentTimeMillis(), tick, "INFO", "", "", "", "No suitable baseline snapshot"));
				return;
			}

			DiffResult diff = SnapshotDiff.diff(a, b);
			lastDiff = diff;

			long ts = System.currentTimeMillis();
			int diffTick = b.tick;
			panel.append(new LogEntry(ts, diffTick, "DIFF", "", "", "", a.label + " → " + b.label + " (" + diff.changes.size() + " changes)"));

			if (diff.changes.isEmpty())
			{
				return;
			}

			for (DiffChange c : diff.changes)
			{
				String id = c.idType.name() + ":" + c.id;
				String oldV = c.oldValue == null ? "" : String.valueOf(c.oldValue);
				String newV = c.newValue == null ? "" : String.valueOf(c.newValue);
				String notes = c.delta == null ? "" : ("Δ=" + c.delta);
				panel.append(new LogEntry(ts, diffTick, "DIFF", id, oldV, newV, notes));
			}

			WorldPointInfo playerInfo = null;
			ScopeState scopeState = scope == null ? ScopeState.inactive() : scope.toState();
			Player p = client.getLocalPlayer();
			if (p != null)
			{
				playerInfo = WorldPointInfo.from(p.getWorldLocation());
			}
			events.add(EventRecord.diff(ts, diffTick, a.label, b.label, diff.changes, playerInfo, scopeState));
		});
	}

	private static Snapshot chooseDiffA(List<Snapshot> snaps, Snapshot latest)
	{
		Snapshot baseline = null;
		for (int i = 0; i < snaps.size(); i++)
		{
			Snapshot s = snaps.get(i);
			if (s == null || s.label == null)
			{
				continue;
			}
			String l = s.label.trim().toLowerCase();
			if ("baseline".equals(l) || "empty".equals(l))
			{
				baseline = s;
			}
		}

		if (baseline != null && baseline != latest)
		{
			return baseline;
		}

		// previous snapshot
		return snaps.get(snaps.size() - 2);
	}

	private void mapSummary()
	{
		clientThread.invokeLater(() ->
		{
			List<Snapshot> snaps = snapshotStore.list();
			if (snaps.isEmpty())
			{
				panel.append(new LogEntry(System.currentTimeMillis(), tick, "INFO", "", "", "", "No snapshots"));
				return;
			}

			List<String> lines = MappingAggregator.aggregate(snaps);
			if (lines.isEmpty())
			{
				panel.append(new LogEntry(System.currentTimeMillis(), tick, "MAP", "", "", "", "No labeled snapshots"));
				return;
			}

			long ts = System.currentTimeMillis();
			panel.append(new LogEntry(ts, tick, "MAP", "", "", "", "Mapping summary (" + lines.size() + " ids)"));
			for (String line : lines)
			{
				panel.append(new LogEntry(ts, tick, "MAP", "", "", "", line));
			}
		});
	}

	private void export()
	{
		clientThread.invokeLater(() ->
		{
			final long ts = System.currentTimeMillis();
			final int exportTick = tick;

			final File dir = resolveOutputDirectory();
			final ExportPaths paths = fileNamer.paths(dir, sessionTag);

			final List<EventRecord> toExport;
			final int exportFrom = exportedEventCount;
			if (events.size() > exportFrom)
			{
				toExport = new ArrayList<>(events.subList(exportFrom, events.size()));
			}
			else
			{
				toExport = new ArrayList<>();
			}
			final int exportTo = exportFrom + toExport.size();

			final List<Snapshot> snapshots = snapshotStore.list();
			final DiffResult diff = lastDiff;

			executor.execute(() ->
			{
				try
				{
					new JsonlExporter(paths).export(toExport, snapshots, diff);

					final String diffPart = diff == null ? "(no diff)" : paths.diffCsv.getName();
					clientThread.invokeLater(() ->
					{
						exportedEventCount = exportTo;
						panel.append(new LogEntry(ts, exportTick, "EXPORT", "", "", "", "Wrote: " +
							paths.eventsJsonl.getName() + ", " + paths.snapshotsJsonl.getName() + ", " + diffPart));
					});
				}
				catch (IOException ex)
				{
					clientThread.invokeLater(() ->
						panel.append(new LogEntry(ts, exportTick, "EXPORT", "", "", "", "Export failed: " + ex.getMessage())));
				}
			});
		});
	}

	private File resolveOutputDirectory()
	{
		String configured = config.outputDirectory();
		File base;
		if (configured != null && !configured.trim().isEmpty())
		{
			base = new File(configured.trim());
		}
		else
		{
			base = new File(RuneLite.RUNELITE_DIR, "varbit-explorer");
		}
		return base;
	}
}
