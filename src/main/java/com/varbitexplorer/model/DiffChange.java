package com.varbitexplorer.model;

import com.varbitexplorer.logic.IdType;

public final class DiffChange
{
	public final IdType idType;
	public final int id;
	public final Integer oldValue;
	public final Integer newValue;
	public final Integer delta;

	public DiffChange(IdType idType, int id, Integer oldValue, Integer newValue)
	{
		this.idType = idType;
		this.id = id;
		this.oldValue = oldValue;
		this.newValue = newValue;
		if (oldValue != null && newValue != null)
		{
			this.delta = newValue - oldValue;
		}
		else
		{
			this.delta = null;
		}
	}
}
