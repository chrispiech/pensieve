package util;

import java.util.*;

public class Warnings {

	private static Set<String> issued = new HashSet<String>();
	
	public static void check(boolean cond, String msg) {
		if(!cond) {
			throw new RuntimeException(msg);
		}
	}

	public static void error(String msg) {
		throw new RuntimeException(msg);
	}

	public static void msg(String msg) {
		if(!issued.contains(msg)) {
			System.out.println("WARNING: " + msg);
			issued.add(msg);
		}
	}
	
}
