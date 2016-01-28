package utils;

import java.util.Comparator;

import exas.ExasFeature;

public class FeatureAscendingOrder implements Comparator<ExasFeature>
{
	/**
	 * Sort in reverse natural order.
	 * Defines an alternate sort order for Pair.
	 * Compare two Pair Objects.
	 * Compares descending.
	 *
	 * @param p1 first String to compare
	 * @param p2 second String to compare
	 *
	 * @return +1 if p1<p2, 0 if p1==p2, -1 if p1>p2
	 */
	@Override
	public final int compare(ExasFeature f1, ExasFeature f2)
	{
		return f1.compareTo(f2);
	}
}
