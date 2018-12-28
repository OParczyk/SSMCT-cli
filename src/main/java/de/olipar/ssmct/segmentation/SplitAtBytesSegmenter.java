package de.olipar.ssmct.segmentation;

import java.util.ArrayList;
import java.util.List;

public class SplitAtBytesSegmenter implements Segmenter<Byte> {

	private final byte[] splitBytes;
	private final boolean includeSplitBytes;

	public SplitAtBytesSegmenter(@Param(name = "Characters to split at", type = ParameterType.STRING) String splitBytes,
			@Param(name = "Include bytes to split at", type = ParameterType.BOOL)boolean includeSplitBytes) {
		this.splitBytes = splitBytes.getBytes();
		this.includeSplitBytes = includeSplitBytes;
	}

	@Override
	public Comparable<Byte>[][] getSegments(byte[] input) {
		List<Byte> temp = new ArrayList<Byte>();
		List<Byte[]> ret = new ArrayList<Byte[]>();
		for (byte i : input) {
			if (contains(i, splitBytes)) {
				if (includeSplitBytes)
					temp.add(Byte.valueOf(i));
				ret.add(temp.toArray(new Byte[temp.size()]));
				temp.clear();
				continue;
			}
			temp.add(Byte.valueOf(i));
		}
		if (temp.size() != 0)
			ret.add(temp.toArray(new Byte[temp.size()]));

		return ret.toArray(new Byte[ret.size()][]);
	}

	private static boolean contains(byte comparator, byte[] container) {
		for (byte i : container) {
			if (comparator == i)
				return true;
		}
		return false;
	}

}
