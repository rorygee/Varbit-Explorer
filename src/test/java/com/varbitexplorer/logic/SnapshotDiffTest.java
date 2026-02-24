package com.varbitexplorer.logic;

import com.varbitexplorer.model.DiffResult;
import com.varbitexplorer.model.ScopeState;
import com.varbitexplorer.model.Snapshot;
import org.junit.Test;

import java.util.SortedMap;
import java.util.TreeMap;

import static org.junit.Assert.*;

public class SnapshotDiffTest
{
	@Test
	public void diffsStableOrderingVarbitsThenVarps()
	{
		Snapshot a = snap("a",
			map(4771, 0, 4774, 2),
			map(10, 5));
		Snapshot b = snap("b",
			map(4771, 1, 4774, 2, 4775, 9),
			map(10, 7, 11, 1));

		DiffResult d = SnapshotDiff.diff(a, b);
		assertEquals(4, d.changes.size());

		// varbits: 4771 (0->1), 4775 (null->9)
		assertEquals(IdType.VARBIT, d.changes.get(0).idType);
		assertEquals(4771, d.changes.get(0).id);
		assertEquals(IdType.VARBIT, d.changes.get(1).idType);
		assertEquals(4775, d.changes.get(1).id);

		// varps: 10 (5->7), 11 (null->1)
		assertEquals(IdType.VARP, d.changes.get(2).idType);
		assertEquals(10, d.changes.get(2).id);
		assertEquals(IdType.VARP, d.changes.get(3).idType);
		assertEquals(11, d.changes.get(3).id);
	}

	@Test
	public void noChangeProducesEmptyList()
	{
		Snapshot a = snap("a", map(1, 2), map());
		Snapshot b = snap("b", map(1, 2), map());
		DiffResult d = SnapshotDiff.diff(a, b);
		assertTrue(d.changes.isEmpty());
	}

	private static Snapshot snap(String label, SortedMap<Integer, Integer> vb, SortedMap<Integer, Integer> vp)
	{
		return new Snapshot(1L, 1, label, null, ScopeState.inactive(), vb, vp);
	}

	private static SortedMap<Integer, Integer> map(Object... kv)
	{
		SortedMap<Integer, Integer> m = new TreeMap<>();
		for (int i = 0; i + 1 < kv.length; i += 2)
		{
			m.put((Integer) kv[i], (Integer) kv[i + 1]);
		}
		return m;
	}
}
