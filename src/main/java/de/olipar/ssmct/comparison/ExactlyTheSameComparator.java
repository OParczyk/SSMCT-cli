package de.olipar.ssmct.comparison;

import java.util.Comparator;

public class ExactlyTheSameComparator implements Comparator<Comparable<?>[]> {

	public ExactlyTheSameComparator() {
	}

	@Override
	public int compare(Comparable<?>[] o1, Comparable<?>[] o2) {
		if (o1.length != o2.length)
			return 0;
		for (int i = 0; i < o1.length; i++) {
			if (!o1[i].equals(o2[i]))
				return 0;
		}
		return 1;
	}

}
