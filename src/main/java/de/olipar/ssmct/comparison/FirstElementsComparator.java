package de.olipar.ssmct.comparison;

import java.util.Comparator;

import de.olipar.ssmct.annotation.Param;
import de.olipar.ssmct.annotation.ParameterDisplayType;
import de.olipar.ssmct.annotation.ParameterType;

public class FirstElementsComparator implements Comparator<Comparable<?>[]> {
	private final int numberOfElementsToCompare;

	public FirstElementsComparator(
			@Param(displayType = ParameterDisplayType.NUMBER, name = "Number of elements to compare", type = ParameterType.INT, min = 0, max = Integer.MAX_VALUE) int numberOfElementsToCompare) {
		if (numberOfElementsToCompare < 0)
			throw new IllegalArgumentException("Number of elements to compare must not be negative!");
		this.numberOfElementsToCompare = numberOfElementsToCompare;
	}

	@Override
	public int compare(Comparable<?>[] o1, Comparable<?>[] o2) {
		int ret = 0;
		for (int i = 0; i < Math.min(o1.length, o2.length); i++) {
			if (i == numberOfElementsToCompare)
				break;
			if (o1[i].equals(o2[i]))
				ret++;
		}
		return ret;
	}

}
