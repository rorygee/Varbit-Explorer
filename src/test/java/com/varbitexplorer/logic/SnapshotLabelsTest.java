package com.varbitexplorer.logic;

import org.junit.Test;

import static org.junit.Assert.*;

public class SnapshotLabelsTest
{
	@Test
	public void defaultLabelIsStable()
	{
		assertEquals("snapshot-1", SnapshotLabels.defaultLabel(1));
		assertEquals("snapshot-10", SnapshotLabels.defaultLabel(10));
	}

	@Test
	public void normalizeUsesDefaultOnEmpty()
	{
		assertNull(SnapshotLabels.normalize(null, "d"));
		assertEquals("d", SnapshotLabels.normalize("   ", "d"));
		assertEquals("planted", SnapshotLabels.normalize(" planted ", "d"));
	}
}
