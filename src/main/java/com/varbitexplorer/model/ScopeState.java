package com.varbitexplorer.model;

public final class ScopeState
{
	public final boolean active;
	public final WorldPointInfo anchor;
	public final int radius;

	private ScopeState(boolean active, WorldPointInfo anchor, int radius)
	{
		this.active = active;
		this.anchor = anchor;
		this.radius = radius;
	}

	public static ScopeState inactive()
	{
		return new ScopeState(false, null, 0);
	}

	public static ScopeState active(WorldPointInfo anchor, int radius)
	{
		return new ScopeState(true, anchor, radius);
	}
}
