package com.varbitexplorer.logic;

import com.varbitexplorer.model.ScopeState;
import com.varbitexplorer.model.WorldPointInfo;
import net.runelite.api.coords.WorldPoint;

public final class Scope
{
	private final WorldPoint anchor;
	private final int radius;

	public Scope(WorldPoint anchor, int radius)
	{
		this.anchor = anchor;
		this.radius = radius;
	}

	public WorldPoint getAnchor()
	{
		return anchor;
	}

	public int getRadius()
	{
		return radius;
	}

	public boolean isActive()
	{
		return anchor != null;
	}

	public boolean contains(WorldPoint player)
	{
		if (anchor == null || player == null)
		{
			return false;
		}

		if (player.getPlane() != anchor.getPlane())
		{
			return false;
		}

		int dx = Math.abs(player.getX() - anchor.getX());
		int dy = Math.abs(player.getY() - anchor.getY());
		int dist = Math.max(dx, dy); // tile-based (Chebyshev)
		return dist <= radius;
	}

	public ScopeState toState()
	{
		if (anchor == null)
		{
			return ScopeState.inactive();
		}
		return ScopeState.active(WorldPointInfo.from(anchor), radius);
	}
}
