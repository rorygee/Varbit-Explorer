package com.varbitexplorer.logic;

import com.varbitexplorer.model.ScopeState;
import com.varbitexplorer.model.Snapshot;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import static org.junit.Assert.*;

public class MappingAggregatorTest
{
	@Test
	public void aggregatesByIdAndLabelDeterministically()
	{
		Snapshot empty = snap("empty", map(4774, 2), map());
		Snapshot planted = snap("planted", map(4774, 103, 4775, 9), map());
		Snapshot planted2 = snap("planted", map(4774, 104), map());

		List<String> lines = MappingAggregator.aggregate(Arrays.asList(empty, planted, planted2));
		assertEquals(2, lines.size());
		assertEquals("VARBIT 4774: empty=2 planted=103|104", lines.get(0));
		assertEquals("VARBIT 4775: planted=9", lines.get(1));
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
