package com.varbitexplorer.model;

import com.varbitexplorer.logic.IdType;

import java.util.Collections;
import java.util.List;

public final class EventRecord
{
	public final long timestampEpochMs;
	public final int tick;
	public final String type;

	public final IdType idType; // for VARBIT_CHANGED / VARP_CHANGED
	public final Integer id;
	public final Integer oldValue;
	public final Integer newValue;

	public final String label; // for SNAPSHOT

	public final String diffA;
	public final String diffB;
	public final List<DiffChange> diffChanges;

	public final WorldPointInfo player;
	public final ScopeState scope;

	private EventRecord(
		long timestampEpochMs,
		int tick,
		String type,
		IdType idType,
		Integer id,
		Integer oldValue,
		Integer newValue,
		String label,
		String diffA,
		String diffB,
		List<DiffChange> diffChanges,
		WorldPointInfo player,
		ScopeState scope)
	{
		this.timestampEpochMs = timestampEpochMs;
		this.tick = tick;
		this.type = type;
		this.idType = idType;
		this.id = id;
		this.oldValue = oldValue;
		this.newValue = newValue;
		this.label = label;
		this.diffA = diffA;
		this.diffB = diffB;
		this.diffChanges = diffChanges == null ? Collections.emptyList() : Collections.unmodifiableList(diffChanges);
		this.player = player;
		this.scope = scope;
	}

	public static EventRecord varChanged(long ts, int tick, IdType idType, int id, int oldV, int newV, WorldPointInfo player, ScopeState scope)
	{
		String type = idType == IdType.VARBIT ? "VARBIT_CHANGED" : "VARP_CHANGED";
		return new EventRecord(ts, tick, type, idType, id, oldV, newV, null, null, null, null, player, scope);
	}

	public static EventRecord snapshot(long ts, int tick, String label, WorldPointInfo player, ScopeState scope)
	{
		return new EventRecord(ts, tick, "SNAPSHOT", null, null, null, null, label, null, null, null, player, scope);
	}

	public static EventRecord diff(long ts, int tick, String aLabel, String bLabel, List<DiffChange> changes, WorldPointInfo player, ScopeState scope)
	{
		return new EventRecord(ts, tick, "DIFF", null, null, null, null, null, aLabel, bLabel, changes, player, scope);
	}
}
