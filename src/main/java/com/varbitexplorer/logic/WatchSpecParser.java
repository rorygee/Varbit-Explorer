package com.varbitexplorer.logic;

import java.util.ArrayList;
import java.util.List;

public final class WatchSpecParser
{
	private WatchSpecParser() {}

	/**
	 * Parse a watch spec string.
	 *
	 * Supported tokens (comma/whitespace separated):
	 * - varbit:4774
	 * - varbit:4771-4775
	 * - varp:123
	 * - varp:456-460
	 * - bare numbers/ranges follow the last explicitly-set type; defaultType is used initially.
	 */
	public static WatchSpecParseResult parse(String input, IdType defaultType)
	{
		WatchSpec spec = WatchSpec.empty();
		List<String> errors = new ArrayList<>();

		if (input == null || input.trim().isEmpty())
		{
			return new WatchSpecParseResult(spec, errors);
		}

		IdType currentType = defaultType == null ? IdType.VARBIT : defaultType;
		// Normalize common unicode dash characters so copy/paste (or OS IME) doesn't break ranges.
		String normalized = input
			.replace(',', ' ')
			.replace('\u2013', '-') // EN DASH
			.replace('\u2014', '-') // EM DASH
			.replace('\u2212', '-') // MINUS SIGN
			.trim();
		String[] tokens = normalized.split("\\s+");

		for (String rawToken : tokens)
		{
			if (rawToken == null)
			{
				continue;
			}

			String token = rawToken.trim();
			if (token.isEmpty())
			{
				continue;
			}

			int colon = token.indexOf(':');
			if (colon > 0)
			{
				String prefix = token.substring(0, colon).trim().toLowerCase();
				String rest = token.substring(colon + 1).trim();
				IdType explicit = parsePrefix(prefix);
				if (explicit == null)
				{
					errors.add("Invalid token: " + rawToken);
					continue;
				}
				currentType = explicit;
				token = rest;
			}

			if (token.isEmpty())
			{
				errors.add("Invalid token: " + rawToken);
				continue;
			}

			try
			{
				int dash = token.indexOf('-');
				if (dash > 0)
				{
					int a = Integer.parseInt(token.substring(0, dash).trim());
					int b = Integer.parseInt(token.substring(dash + 1).trim());
					if (a < 0 || b < 0)
					{
						errors.add("Invalid token: " + rawToken);
						continue;
					}
					if (currentType == IdType.VARBIT)
					{
						spec = spec.withAddedVarbitRange(a, b);
					}
					else
					{
						spec = spec.withAddedVarpRange(a, b);
					}
				}
				else
				{
					int id = Integer.parseInt(token);
					if (id < 0)
					{
						errors.add("Invalid token: " + rawToken);
						continue;
					}
					if (currentType == IdType.VARBIT)
					{
						spec = spec.withAddedVarbit(id);
					}
					else
					{
						spec = spec.withAddedVarp(id);
					}
				}
			}
			catch (NumberFormatException ex)
			{
				errors.add("Invalid token: " + rawToken);
			}
		}

		return new WatchSpecParseResult(spec, errors);
	}

	private static IdType parsePrefix(String prefix)
	{
		if ("varbit".equals(prefix) || "vb".equals(prefix))
		{
			return IdType.VARBIT;
		}
		if ("varp".equals(prefix) || "vp".equals(prefix))
		{
			return IdType.VARP;
		}
		return null;
	}
}
