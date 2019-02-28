package stanford.edu.gitviewer;

public class Util {

	public static String round (double value, int precision) {
	    int scale = (int) Math.pow(10, precision);
	    String str = "" +  (double) Math.round(value * scale) / scale;
	    return str;
	}
}
