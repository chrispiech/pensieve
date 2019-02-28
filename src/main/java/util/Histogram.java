package util;

import java.util.*;

public class Histogram {

	private double minBound;
	private double maxBound;
	private double binSize;
	
	List<Integer> counts;
	
	public Histogram(double minBound, double maxBound, double size) {
		
		this.binSize = size;
		this.maxBound = maxBound;
		this.minBound = minBound;
		this.counts = new ArrayList<Integer>();
		
		double diff = maxBound - minBound;
		int numBuckets = (int) (diff / size) + 1;
		for(int i = 0; i < numBuckets; i++) {
			counts.add(0);
		}
		
		
	}
	
	public void addPoint(double x) {
		int binIndex = getBinIndex(x);
		int newCount = counts.get(binIndex) + 1;
		counts.set(binIndex, newCount);
	}
	
	private int getBinIndex(double x) {
		double pos = (x - minBound) / binSize;
		return (int) pos;
	}
	
	public String toString() {
		String str = "";
		for(int i = 0; i < counts.size(); i++) {
			double lowerBound = minBound + i * binSize;
			int count = counts.get(i);
			String lowerStr = String.format("%.2f", lowerBound);
			String boundStr = lowerStr;
			System.out.println(boundStr + "\t" + count);
		}
		return str;
	}

	public static void main(String[] args) {
		Histogram h = new Histogram(0, 2.5, 0.1);
		h.addPoint(0.00);
		h.addPoint(0.05);
		h.addPoint(0.10);
		h.addPoint(0.12);
		
		h.addPoint(0.45);
		System.out.println(h);
	}
	
}
