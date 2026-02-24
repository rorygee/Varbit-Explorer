package com.varbitexplorer.export;

import com.varbitexplorer.logic.IdType;
import com.varbitexplorer.model.EventRecord;
import com.varbitexplorer.model.ScopeState;
import com.varbitexplorer.model.Snapshot;
import org.junit.Test;

import java.util.SortedMap;
import java.util.TreeMap;

import static org.junit.Assert.*;

public class ExportFormatterTest
{
	@Test
	public void formatsEventWithStableFieldOrder()
	{
		EventRecord e = EventRecord.varChanged(123L, 7, IdType.VARBIT, 4774, 2, 103, null, ScopeState.inactive());
		String json = ExportFormatter.formatEvent(e);
		assertEquals(
			"{\"timestampEpochMs\":123,\"tick\":7,\"type\":\"VARBIT_CHANGED\",\"idType\":\"VARBIT\",\"id\":4774,\"old\":2,\"new\":103,\"label\":null,\"diffA\":null,\"diffB\":null,\"changes\":[]," +
			"\"playerX\":null,\"playerY\":null,\"playerPlane\":null,\"playerRegionId\":null,\"playerChunkX\":null,\"playerChunkY\":null," +
			"\"scopeActive\":false,\"scopeRadius\":null,\"scopeAnchorX\":null,\"scopeAnchorY\":null,\"scopePlane\":null,\"scopeRegionId\":null,\"scopeChunkX\":null,\"scopeChunkY\":null}",
			json
		);
	}

	@Test
	public void formatsSnapshotWithStableVarOrdering()
	{
		SortedMap<Integer, Integer> vb = new TreeMap<>();
		vb.put(4774, 2);
		SortedMap<Integer, Integer> vp = new TreeMap<>();
		Snapshot s = new Snapshot(123L, 7, "empty", null, ScopeState.inactive(), vb, vp);
		String json = ExportFormatter.formatSnapshot(s);
		assertEquals(
			"{\"timestampEpochMs\":123,\"tick\":7,\"label\":\"empty\"," +
			"\"playerX\":null,\"playerY\":null,\"playerPlane\":null,\"playerRegionId\":null,\"playerChunkX\":null,\"playerChunkY\":null," +
			"\"scopeActive\":false,\"scopeRadius\":null,\"scopeAnchorX\":null,\"scopeAnchorY\":null,\"scopePlane\":null,\"scopeRegionId\":null,\"scopeChunkX\":null,\"scopeChunkY\":null," +
			"\"varbits\":[[4774,2]],\"varps\":[]}",
			json
		);
	}
}
