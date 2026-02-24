package com.varbitexplorer.logic;

import java.util.Iterator;
import java.util.SortedSet;

public final class WatchSpecFormatter
{
	private WatchSpecFormatter() {}

	public static String format(WatchSpec spec)
	{
		if (spec == null || spec.isEmpty())
		{
			return "";
		}

		StringBuilder sb = new StringBuilder();
		if (!spec.getVarbits().isEmpty())
		{
			sb.append("varbit:");
			appendCompressed(sb, spec.getVarbits());
		}
		if (!spec.getVarps().isEmpty())
		{
			if (sb.length() > 0)
			{
				sb.append(' ');
			}
			sb.append("varp:");
			appendCompressed(sb, spec.getVarps());
		}

		return sb.toString();
	}

	private static void appendCompressed(StringBuilder sb, SortedSet<Integer> ids)
	{
		Iterator<Integer> it = ids.iterator();
		if (!it.hasNext())
		{
			return;
		}

		int start = it.next();
		int prev = start;
		while (it.hasNext())
		{
			int cur = it.next();
			if (cur == prev + 1)
			{
				prev = cur;
				continue;
			}

			appendRange(sb, start, prev);
			sb.append(',');
			start = prev = cur;
		}
		appendRange(sb, start, prev);
	}

	private static void appendRange(StringBuilder sb, int start, int end)
	{
		if (start == end)
		{
			sb.append(start);
		}
		else
		{
			sb.append(start).append('-').append(end);
		}
	}
}
