package com.varbitexplorer.model;

import net.runelite.api.coords.WorldPoint;

public final class WorldPointInfo
{
	public final int x;
	public final int y;
	public final int plane;
	public final int regionId;
	public final int chunkX;
	public final int chunkY;

	private WorldPointInfo(int x, int y, int plane, int regionId, int chunkX, int chunkY)
	{
		this.x = x;
		this.y = y;
		this.plane = plane;
		this.regionId = regionId;
		this.chunkX = chunkX;
		this.chunkY = chunkY;
	}

	public static WorldPointInfo from(WorldPoint wp)
	{
		if (wp == null)
		{
			return null;
		}

		int x = wp.getX();
		int y = wp.getY();
		int plane = wp.getPlane();
		int regionX = wp.getRegionX();
		int regionY = wp.getRegionY();
		int regionId = (regionX << 8) | regionY;
		int chunkX = x >> 3;
		int chunkY = y >> 3;
		return new WorldPointInfo(x, y, plane, regionId, chunkX, chunkY);
	}
}
