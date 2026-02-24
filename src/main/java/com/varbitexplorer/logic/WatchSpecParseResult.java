package com.varbitexplorer.logic;

import java.util.Collections;
import java.util.List;

public final class WatchSpecParseResult
{
	public final WatchSpec spec;
	public final List<String> errors;

	public WatchSpecParseResult(WatchSpec spec, List<String> errors)
	{
		this.spec = spec;
		this.errors = errors == null ? Collections.emptyList() : Collections.unmodifiableList(errors);
	}
}
