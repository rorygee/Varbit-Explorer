package com.varbitexplorer.logic;

import com.varbitexplorer.model.ScopeState;
import com.varbitexplorer.model.Snapshot;
import org.junit.Test;

import java.util.SortedMap;
import java.util.TreeMap;

import static org.junit.Assert.*;

public class SnapshotStoreTest
{
	@Test
	public void retainsUpToMax()
	{
		SnapshotStore store = new SnapshotStore();
		for (int i = 1; i <= 5; i++)
		{
			store.add(snapshot("s" + i, i), 3);
		}

		assertEquals(3, store.size());
		assertEquals("s3", store.list().get(0).label);
		assertEquals("s5", store.getLatest().label);
	}

	private static Snapshot snapshot(String label, int tick)
	{
		SortedMap<Integer, Integer> vb = new TreeMap<>();
		SortedMap<Integer, Integer> vp = new TreeMap<>();
		vb.put(1, tick);
		return new Snapshot(1000L + tick, tick, label, null, ScopeState.inactive(), vb, vp);
	}
}
