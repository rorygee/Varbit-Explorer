package com.varbitexplorer.logic;

import org.junit.Test;

import static org.junit.Assert.*;

public class WatchSpecParserTest
{
	@Test
	public void parsesSingleVarbit()
	{
		WatchSpecParseResult r = WatchSpecParser.parse("varbit:4774", IdType.VARBIT);
		assertTrue(r.errors.isEmpty());
		assertEquals(1, r.spec.varbitCount());
		assertTrue(r.spec.getVarbits().contains(4774));
		assertEquals(0, r.spec.varpCount());
	}

	@Test
	public void parsesRangesAndCommas()
	{
		WatchSpecParseResult r = WatchSpecParser.parse("varbit:4771-4775, 4774", IdType.VARBIT);
		assertTrue(r.errors.isEmpty());
		assertEquals(5, r.spec.varbitCount());
		assertTrue(r.spec.getVarbits().first() == 4771);
		assertTrue(r.spec.getVarbits().last() == 4775);
	}

	@Test
	public void acceptsUnicodeDashesInRanges()
	{
		// EN DASH between numbers is common when copy/pasting.
		WatchSpecParseResult r = WatchSpecParser.parse("varbit:4771–4775", IdType.VARBIT);
		assertTrue(r.errors.isEmpty());
		assertEquals(5, r.spec.varbitCount());
		assertTrue(r.spec.getVarbits().contains(4771));
		assertTrue(r.spec.getVarbits().contains(4775));
	}

	@Test
	public void parsesVarpsAndWhitespace()
	{
		WatchSpecParseResult r = WatchSpecParser.parse("varp:123 456-458", IdType.VARBIT);
		assertTrue(r.errors.isEmpty());
		assertEquals(0, r.spec.varbitCount());
		assertEquals(4, r.spec.varpCount());
		assertTrue(r.spec.getVarps().contains(123));
		assertTrue(r.spec.getVarps().contains(456));
		assertTrue(r.spec.getVarps().contains(458));
	}

	@Test
	public void bareTokensFollowLastType()
	{
		WatchSpecParseResult r = WatchSpecParser.parse("varp:10 11 varbit:1 2 3 varp:20 21", IdType.VARBIT);
		assertTrue(r.errors.isEmpty());
		assertEquals(3, r.spec.varbitCount());
		assertEquals(4, r.spec.varpCount());
		assertTrue(r.spec.getVarps().contains(10));
		assertTrue(r.spec.getVarps().contains(11));
		assertTrue(r.spec.getVarps().contains(20));
		assertTrue(r.spec.getVarps().contains(21));
	}

	@Test
	public void deDupesAndSorts()
	{
		WatchSpecParseResult r = WatchSpecParser.parse("varbit:2,1,2,1-3", IdType.VARBIT);
		assertTrue(r.errors.isEmpty());
		assertEquals(3, r.spec.varbitCount());
		assertArrayEquals(new Integer[]{1,2,3}, r.spec.getVarbits().toArray(new Integer[0]));
	}

	@Test
	public void invalidTokensFailSoft()
	{
		WatchSpecParseResult r = WatchSpecParser.parse("foo:123 varbit:abc varp:1- x", IdType.VARBIT);
		assertFalse(r.errors.isEmpty());
		assertTrue(r.spec.isEmpty());
	}
}
