package de.olipar.ssmct.comparison;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class TheComparator {
	public static int[][] compare(Comparator<Comparable<?>[]> comparator, Comparable<?>[][] segments) {
		List<Integer> temp = new ArrayList<Integer>();
		List<int[]> retList = new ArrayList<int[]>();
		for(int i=0;i<segments.length;i++) {
			for(int j=0;j<segments.length;j++) {
				temp.add(comparator.compare(segments[i], segments[j]));
			}
			retList.add(integerListToIntArray(temp));
			temp.clear();
		}
		return retList.toArray(new int[retList.size()][]);
	}
	
	private static int[] integerListToIntArray(List<Integer> list) {
		int[] ret = new int[list.size()];
		for(int i=0;i<ret.length;i++) {
			ret[i]=list.get(i).intValue();
		}
		return ret;
	}
}
