package javaEval;

public class JavaSource {

	private String src;
	
	private String className;
	
	public JavaSource(String className, String src) {
		this.className = className;
		this.src = src;
	}
	
	public String getSource() {
		return src;
	}
	
	public String getClassName() {
		return className;
	}
	
}
