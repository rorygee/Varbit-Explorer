package com.varbitexplorer.logic;

import com.varbitexplorer.model.Snapshot;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

public final class SnapshotStore
{
	private final Deque<Snapshot> snapshots = new ArrayDeque<>();

	public void add(Snapshot snapshot, int maxSnapshots)
	{
		if (snapshot == null)
		{
			return;
		}

		snapshots.addLast(snapshot);

		int limit = Math.max(1, maxSnapshots);
		while (snapshots.size() > limit)
		{
			snapshots.removeFirst();
		}
	}

	public int size()
	{
		return snapshots.size();
	}

	public List<Snapshot> list()
	{
		return Collections.unmodifiableList(new ArrayList<>(snapshots));
	}

	public Snapshot getLatest()
	{
		return snapshots.peekLast();
	}

	public Snapshot getPrevious()
	{
		if (snapshots.size() < 2)
		{
			return null;
		}

		Snapshot last = snapshots.removeLast();
		Snapshot prev = snapshots.peekLast();
		snapshots.addLast(last);
		return prev;
	}

	public Snapshot findLatestBaselineLike()
	{
		Snapshot candidate = null;
		for (Snapshot s : snapshots)
		{
			if (s == null || s.label == null)
			{
				continue;
			}
			String l = s.label.trim().toLowerCase();
			if ("baseline".equals(l) || "empty".equals(l) || "weeds".equals(l))
			{
				candidate = s;
			}
		}
		return candidate;
	}

	public void clear()
	{
		snapshots.clear();
	}
}
