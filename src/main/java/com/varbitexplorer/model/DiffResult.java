package com.varbitexplorer.model;

import java.util.Collections;
import java.util.List;

public final class DiffResult
{
	public final Snapshot a;
	public final Snapshot b;
	public final List<DiffChange> changes;

	public DiffResult(Snapshot a, Snapshot b, List<DiffChange> changes)
	{
		this.a = a;
		this.b = b;
		this.changes = changes == null ? Collections.emptyList() : Collections.unmodifiableList(changes);
	}
}
