package de.olipar.ssmct.comparison;

import java.util.Comparator;

public class ExactlyTheSameComparator implements Comparator<Comparable<?>[]> {

	public ExactlyTheSameComparator() {
	}

	@Override
	public int compare(Comparable<?>[] o1, Comparable<?>[] o2) {
		if (o1.equals(o2))
			return 1;
		return 0;
	}

}
