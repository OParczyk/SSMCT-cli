package de.olipar.ssmct.segmentation;

import java.util.ArrayList;
import java.util.List;

import de.olipar.ssmct.annotation.Param;
import de.olipar.ssmct.annotation.ParameterDisplayType;
import de.olipar.ssmct.annotation.ParameterType;

public class ByteSegmenter implements Segmenter<Byte> {

	private final int segmentSize;
	private final int start, end;

	public ByteSegmenter(
			@Param(displayType = ParameterDisplayType.NUMBER, type = ParameterType.INT, min = 1, max = Integer.MAX_VALUE, name = "segment size") Integer segmentSize,
			@Param(displayType = ParameterDisplayType.NUMBER, name = "Begin at byte", type = ParameterType.INT, min = 0, max = Integer.MAX_VALUE) Integer start,
			@Param(displayType = ParameterDisplayType.NUMBER, name = "Stop at byte", type = ParameterType.INT, min = 0, max = Integer.MAX_VALUE) Integer end) {
		if (segmentSize < 1)
			throw new IllegalArgumentException("segment size must be at least 1");
		if (start < 0)
			throw new IllegalArgumentException("start byte must be at least 0");
		if (end < 0)
			throw new IllegalArgumentException("end byte must be at least 0");

		this.segmentSize = segmentSize;
		this.start = start;
		this.end = end;
	}

	@Override
	public Comparable<Byte>[][] getSegments(byte[] input) {
		if (start > end)
			return new Byte[0][0];
		if (start >= input.length)
			return new Byte[0][0];
		List<Byte> temp = new ArrayList<Byte>();
		List<Byte[]> ret = new ArrayList<Byte[]>();
		int stop = Math.min(input.length, end);
		for (int i = start; i < stop; i++) {
			byte currentByte = input[i];
			temp.add(Byte.valueOf(currentByte));
			if (temp.size() >= segmentSize) {
				ret.add(temp.toArray(new Byte[segmentSize]));
				temp.clear();
			}
		}
		if (temp.size() > 0)
			ret.add(temp.toArray(new Byte[temp.size()]));

		return ret.toArray(new Byte[ret.size()][segmentSize]);
	}

}
