package de.olipar.ssmct.segmentation;

import java.util.ArrayList;
import java.util.List;

public class ByteSegmenter implements Segmenter<Byte> {

	private final int segmentSize;

	public ByteSegmenter(int segmentSize) {
		if (segmentSize < 1)
			throw new IllegalArgumentException("segment size must be at least 1");
		this.segmentSize = segmentSize;
	}

	@Override
	public Comparable<Byte>[][] getSegments(byte[] input) {
		List<Byte> temp = new ArrayList<Byte>();
		List<Byte[]> ret = new ArrayList<Byte[]>();
		for (byte i : input) {
			temp.add(Byte.valueOf(i));
			if (temp.size() >= segmentSize) {
				ret.add(temp.toArray(new Byte[segmentSize]));
				temp.clear();
			}
		}
		return ret.toArray(new Byte[ret.size()][segmentSize]);
	}

}
