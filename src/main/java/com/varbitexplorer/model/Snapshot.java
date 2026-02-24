package com.varbitexplorer.model;

import java.util.Collections;
import java.util.SortedMap;
import java.util.TreeMap;

public final class Snapshot
{
	public final long timestampEpochMs;
	public final int tick;
	public final String label;
	public final WorldPointInfo player;
	public final ScopeState scope;
	public final SortedMap<Integer, Integer> varbits;
	public final SortedMap<Integer, Integer> varps;

	public Snapshot(long timestampEpochMs,
				int tick,
				String label,
				WorldPointInfo player,
				ScopeState scope,
				SortedMap<Integer, Integer> varbits,
				SortedMap<Integer, Integer> varps)
	{
		this.timestampEpochMs = timestampEpochMs;
		this.tick = tick;
		this.label = label;
		this.player = player;
		this.scope = scope;
		this.varbits = Collections.unmodifiableSortedMap(new TreeMap<>(varbits));
		this.varps = Collections.unmodifiableSortedMap(new TreeMap<>(varps));
	}
}
