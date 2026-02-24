package com.varbitexplorer.logic;

import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;

public final class WatchSpec
{
	private final SortedSet<Integer> varbits;
	private final SortedSet<Integer> varps;

	private WatchSpec(SortedSet<Integer> varbits, SortedSet<Integer> varps)
	{
		this.varbits = Collections.unmodifiableSortedSet(varbits);
		this.varps = Collections.unmodifiableSortedSet(varps);
	}

	public static WatchSpec empty()
	{
		return new WatchSpec(new TreeSet<>(), new TreeSet<>());
	}

	public SortedSet<Integer> getVarbits()
	{
		return varbits;
	}

	public SortedSet<Integer> getVarps()
	{
		return varps;
	}

	public int varbitCount()
	{
		return varbits.size();
	}

	public int varpCount()
	{
		return varps.size();
	}

	public boolean isEmpty()
	{
		return varbits.isEmpty() && varps.isEmpty();
	}

	public boolean watchesVarbit(int id)
	{
		return varbits.contains(id);
	}

	public boolean watchesVarp(int id)
	{
		return varps.contains(id);
	}

	public WatchSpec withAddedVarbits(Iterable<Integer> ids)
	{
		TreeSet<Integer> vb = new TreeSet<>(varbits);
		for (Integer id : ids)
		{
			if (id != null)
			{
				vb.add(id);
			}
		}
		return new WatchSpec(vb, new TreeSet<>(varps));
	}

	public WatchSpec withAddedVarpRange(int startInclusive, int endInclusive)
	{
		TreeSet<Integer> vp = new TreeSet<>(varps);
		int a = Math.min(startInclusive, endInclusive);
		int b = Math.max(startInclusive, endInclusive);
		for (int i = a; i <= b; i++)
		{
			vp.add(i);
		}
		return new WatchSpec(new TreeSet<>(varbits), vp);
	}

	public WatchSpec withAddedVarbitRange(int startInclusive, int endInclusive)
	{
		TreeSet<Integer> vb = new TreeSet<>(varbits);
		int a = Math.min(startInclusive, endInclusive);
		int b = Math.max(startInclusive, endInclusive);
		for (int i = a; i <= b; i++)
		{
			vb.add(i);
		}
		return new WatchSpec(vb, new TreeSet<>(varps));
	}

	public WatchSpec withAddedVarp(int id)
	{
		TreeSet<Integer> vp = new TreeSet<>(varps);
		vp.add(id);
		return new WatchSpec(new TreeSet<>(varbits), vp);
	}

	public WatchSpec withAddedVarbit(int id)
	{
		TreeSet<Integer> vb = new TreeSet<>(varbits);
		vb.add(id);
		return new WatchSpec(vb, new TreeSet<>(varps));
	}

	public WatchSpec union(WatchSpec other)
	{
		TreeSet<Integer> vb = new TreeSet<>(varbits);
		vb.addAll(other.varbits);
		TreeSet<Integer> vp = new TreeSet<>(varps);
		vp.addAll(other.varps);
		return new WatchSpec(vb, vp);
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
		{
			return true;
		}
		if (!(o instanceof WatchSpec))
		{
			return false;
		}
		WatchSpec other = (WatchSpec) o;
		return varbits.equals(other.varbits) && varps.equals(other.varps);
	}

	@Override
	public int hashCode()
	{
		int result = varbits.hashCode();
		result = 31 * result + varps.hashCode();
		return result;
	}
}
