package de.olipar.ssmct.segmentation;

public interface Segmenter<T> {
public Comparable<T>[][] getSegments(byte[] input);
}
