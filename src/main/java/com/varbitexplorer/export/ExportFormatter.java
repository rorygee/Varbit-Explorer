package com.varbitexplorer.export;

import com.varbitexplorer.model.DiffChange;
import com.varbitexplorer.model.EventRecord;
import com.varbitexplorer.model.ScopeState;
import com.varbitexplorer.model.Snapshot;
import com.varbitexplorer.model.WorldPointInfo;

import java.util.Map;

public final class ExportFormatter
{
	private ExportFormatter() {}

	public static String formatEvent(EventRecord e)
	{
		StringBuilder sb = new StringBuilder(256);
		sb.append('{');

		appendLong(sb, "timestampEpochMs", e.timestampEpochMs);
		sb.append(',');
		appendInt(sb, "tick", e.tick);
		sb.append(',');
		appendString(sb, "type", e.type);
		sb.append(',');
		appendString(sb, "idType", e.idType == null ? null : e.idType.name());
		sb.append(',');
		appendNullableInt(sb, "id", e.id);
		sb.append(',');
		appendNullableInt(sb, "old", e.oldValue);
		sb.append(',');
		appendNullableInt(sb, "new", e.newValue);
		sb.append(',');
		appendString(sb, "label", e.label);
		sb.append(',');
		appendString(sb, "diffA", e.diffA);
		sb.append(',');
		appendString(sb, "diffB", e.diffB);
		sb.append(',');
		appendDiffChanges(sb, "changes", e.diffChanges);
		sb.append(',');
		appendContext(sb, e.player, e.scope);

		sb.append('}');
		return sb.toString();
	}

	public static String formatSnapshot(Snapshot s)
	{
		StringBuilder sb = new StringBuilder(1024);
		sb.append('{');

		appendLong(sb, "timestampEpochMs", s.timestampEpochMs);
		sb.append(',');
		appendInt(sb, "tick", s.tick);
		sb.append(',');
		appendString(sb, "label", s.label);
		sb.append(',');
		appendContext(sb, s.player, s.scope);
		sb.append(',');
		appendPairsArray(sb, "varbits", s.varbits);
		sb.append(',');
		appendPairsArray(sb, "varps", s.varps);

		sb.append('}');
		return sb.toString();
	}

	public static String formatDiffCsvHeader()
	{
		return "idType,id,old,new,delta";
	}

	public static String formatDiffCsvRow(DiffChange c)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(c.idType == null ? "" : c.idType.name());
		sb.append(',').append(c.id);
		sb.append(',').append(c.oldValue == null ? "" : c.oldValue);
		sb.append(',').append(c.newValue == null ? "" : c.newValue);
		sb.append(',').append(c.delta == null ? "" : c.delta);
		return sb.toString();
	}

	private static void appendContext(StringBuilder sb, WorldPointInfo player, ScopeState scope)
	{
		appendNullableInt(sb, "playerX", player == null ? null : player.x);
		sb.append(',');
		appendNullableInt(sb, "playerY", player == null ? null : player.y);
		sb.append(',');
		appendNullableInt(sb, "playerPlane", player == null ? null : player.plane);
		sb.append(',');
		appendNullableInt(sb, "playerRegionId", player == null ? null : player.regionId);
		sb.append(',');
		appendNullableInt(sb, "playerChunkX", player == null ? null : player.chunkX);
		sb.append(',');
		appendNullableInt(sb, "playerChunkY", player == null ? null : player.chunkY);
		sb.append(',');

		boolean active = scope != null && scope.active;
		appendBoolean(sb, "scopeActive", active);
		sb.append(',');
		appendNullableInt(sb, "scopeRadius", active ? scope.radius : null);
		sb.append(',');
		appendNullableInt(sb, "scopeAnchorX", active && scope.anchor != null ? scope.anchor.x : null);
		sb.append(',');
		appendNullableInt(sb, "scopeAnchorY", active && scope.anchor != null ? scope.anchor.y : null);
		sb.append(',');
		appendNullableInt(sb, "scopePlane", active && scope.anchor != null ? scope.anchor.plane : null);
		sb.append(',');
		appendNullableInt(sb, "scopeRegionId", active && scope.anchor != null ? scope.anchor.regionId : null);
		sb.append(',');
		appendNullableInt(sb, "scopeChunkX", active && scope.anchor != null ? scope.anchor.chunkX : null);
		sb.append(',');
		appendNullableInt(sb, "scopeChunkY", active && scope.anchor != null ? scope.anchor.chunkY : null);
	}

	private static void appendPairsArray(StringBuilder sb, String key, Map<Integer, Integer> map)
	{
		appendKey(sb, key);
		sb.append('[');
		boolean first = true;
		for (Map.Entry<Integer, Integer> e : map.entrySet())
		{
			if (!first)
			{
				sb.append(',');
			}
			first = false;
			sb.append('[').append(e.getKey()).append(',').append(e.getValue()).append(']');
		}
		sb.append(']');
	}

	private static void appendDiffChanges(StringBuilder sb, String key, java.util.List<DiffChange> changes)
	{
		appendKey(sb, key);
		sb.append('[');
		boolean first = true;
		if (changes != null)
		{
			for (DiffChange c : changes)
			{
				if (!first)
				{
					sb.append(',');
				}
				first = false;
				sb.append('{');
				appendString(sb, "idType", c.idType == null ? null : c.idType.name());
				sb.append(',');
				appendInt(sb, "id", c.id);
				sb.append(',');
				appendNullableInt(sb, "old", c.oldValue);
				sb.append(',');
				appendNullableInt(sb, "new", c.newValue);
				sb.append(',');
				appendNullableInt(sb, "delta", c.delta);
				sb.append('}');
			}
		}
		sb.append(']');
	}

	private static void appendKey(StringBuilder sb, String key)
	{
		sb.append('"').append(key).append('"').append(':');
	}

	private static void appendString(StringBuilder sb, String key, String value)
	{
		appendKey(sb, key);
		if (value == null)
		{
			sb.append("null");
		}
		else
		{
			sb.append('"').append(JsonEscaper.escape(value)).append('"');
		}
	}

	private static void appendLong(StringBuilder sb, String key, long value)
	{
		appendKey(sb, key);
		sb.append(value);
	}

	private static void appendInt(StringBuilder sb, String key, int value)
	{
		appendKey(sb, key);
		sb.append(value);
	}

	private static void appendNullableInt(StringBuilder sb, String key, Integer value)
	{
		appendKey(sb, key);
		if (value == null)
		{
			sb.append("null");
		}
		else
		{
			sb.append(value);
		}
	}

	private static void appendBoolean(StringBuilder sb, String key, boolean value)
	{
		appendKey(sb, key);
		sb.append(value ? "true" : "false");
	}
}
