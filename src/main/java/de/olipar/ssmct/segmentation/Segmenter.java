package de.olipar.ssmct.segmentation;

public interface Segmenter<T extends Comparable<T>> {
public Comparable<T>[][] getSegments(byte[] input);
}
