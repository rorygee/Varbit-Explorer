package com.varbitexplorer.logic;

import com.varbitexplorer.model.Snapshot;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

public final class MappingAggregator
{
	private MappingAggregator() {}

	public static List<String> aggregate(List<Snapshot> snapshots)
	{
		if (snapshots == null || snapshots.isEmpty())
		{
			return new ArrayList<>();
		}

		Set<String> labelOrder = new LinkedHashSet<>();
		SortedMap<IdKey, SortedMap<String, SortedSet<Integer>>> map = new TreeMap<>();

		for (Snapshot s : snapshots)
		{
			if (s == null)
			{
				continue;
			}
			String label = (s.label == null) ? "" : s.label.trim();
			if (label.isEmpty())
			{
				continue;
			}
			labelOrder.add(label);

			for (Map.Entry<Integer, Integer> e : s.varbits.entrySet())
			{
				put(map, new IdKey(IdType.VARBIT, e.getKey()), label, e.getValue());
			}
			for (Map.Entry<Integer, Integer> e : s.varps.entrySet())
			{
				put(map, new IdKey(IdType.VARP, e.getKey()), label, e.getValue());
			}
		}

		List<String> lines = new ArrayList<>();
		for (Map.Entry<IdKey, SortedMap<String, SortedSet<Integer>>> entry : map.entrySet())
		{
			IdKey key = entry.getKey();
			SortedMap<String, SortedSet<Integer>> byLabel = entry.getValue();

			StringBuilder sb = new StringBuilder();
			sb.append(key.type.name()).append(' ').append(key.id).append(": ");

			boolean first = true;
			for (String label : labelOrder)
			{
				SortedSet<Integer> vals = byLabel.get(label);
				if (vals == null || vals.isEmpty())
				{
					continue;
				}
				if (!first)
				{
					sb.append(' ');
				}
				first = false;
				sb.append(label).append('=');
				appendValues(sb, vals);
			}

			lines.add(sb.toString());
		}

		return lines;
	}

	private static void put(SortedMap<IdKey, SortedMap<String, SortedSet<Integer>>> map, IdKey key, String label, Integer value)
	{
		SortedMap<String, SortedSet<Integer>> byLabel = map.computeIfAbsent(key, k -> new TreeMap<>());
		SortedSet<Integer> values = byLabel.computeIfAbsent(label, l -> new TreeSet<>());
		if (value != null)
		{
			values.add(value);
		}
	}

	private static void appendValues(StringBuilder sb, SortedSet<Integer> values)
	{
		boolean first = true;
		for (Integer v : values)
		{
			if (!first)
			{
				sb.append('|');
			}
			first = false;
			sb.append(v);
		}
	}

	private static final class IdKey implements Comparable<IdKey>
	{
		private final IdType type;
		private final int id;

		private IdKey(IdType type, int id)
		{
			this.type = type;
			this.id = id;
		}

		@Override
		public int compareTo(IdKey o)
		{
			if (o == null)
			{
				return 1;
			}
			int typeCmp = Integer.compare(typeOrder(type), typeOrder(o.type));
			if (typeCmp != 0)
			{
				return typeCmp;
			}
			return Integer.compare(id, o.id);
		}

		private static int typeOrder(IdType t)
		{
			return t == IdType.VARBIT ? 0 : 1;
		}

		@Override
		public boolean equals(Object o)
		{
			if (!(o instanceof IdKey))
			{
				return false;
			}
			IdKey other = (IdKey) o;
			return other.id == id && other.type == type;
		}

		@Override
		public int hashCode()
		{
			return 31 * type.hashCode() + id;
		}
	}
}
