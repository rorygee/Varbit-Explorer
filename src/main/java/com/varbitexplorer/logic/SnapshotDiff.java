package com.varbitexplorer.logic;

import com.varbitexplorer.model.DiffChange;
import com.varbitexplorer.model.DiffResult;
import com.varbitexplorer.model.Snapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

public final class SnapshotDiff
{
	private SnapshotDiff() {}

	public static DiffResult diff(Snapshot a, Snapshot b)
	{
		List<DiffChange> changes = new ArrayList<>();
		if (a == null || b == null)
		{
			return new DiffResult(a, b, changes);
		}

		SortedSet<Integer> varbitKeys = new TreeSet<>();
		varbitKeys.addAll(a.varbits.keySet());
		varbitKeys.addAll(b.varbits.keySet());
		for (Integer id : varbitKeys)
		{
			Integer oldV = a.varbits.get(id);
			Integer newV = b.varbits.get(id);
			if (!equalsNullable(oldV, newV))
			{
				changes.add(new DiffChange(IdType.VARBIT, id, oldV, newV));
			}
		}

		SortedSet<Integer> varpKeys = new TreeSet<>();
		varpKeys.addAll(a.varps.keySet());
		varpKeys.addAll(b.varps.keySet());
		for (Integer id : varpKeys)
		{
			Integer oldV = a.varps.get(id);
			Integer newV = b.varps.get(id);
			if (!equalsNullable(oldV, newV))
			{
				changes.add(new DiffChange(IdType.VARP, id, oldV, newV));
			}
		}

		return new DiffResult(a, b, changes);
	}

	private static boolean equalsNullable(Integer a, Integer b)
	{
		if (a == null && b == null)
		{
			return true;
		}
		if (a == null || b == null)
		{
			return false;
		}
		return a.intValue() == b.intValue();
	}
}
