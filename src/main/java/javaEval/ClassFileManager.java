package javaEval;
import java.io.IOException;
import java.security.SecureClassLoader;
import java.util.*;

import javax.tools.*;
import javax.tools.JavaFileObject.Kind;

@SuppressWarnings("unchecked")
public class ClassFileManager extends
ForwardingJavaFileManager {
	/**
	 * Instance of JavaClassObject that will store the
	 * compiled bytecode of our class
	 */
	private Map<String, JavaClassObject> jclassMap = new HashMap<String,
			JavaClassObject>();

	/**
	 * Will initialize the manager with the specified
	 * standard java file manager
	 *
	 * @param standardManger
	 */
	public ClassFileManager(StandardJavaFileManager
			standardManager) {
		super(standardManager);
	}

	/**
	 * Will be used by us to get the class loader for our
	 * compiled class. It creates an anonymous class
	 * extending the SecureClassLoader which uses the
	 * byte code created by the compiler and stored in
	 * the JavaClassObject, and returns the Class for it
	 */
	@Override
	public ClassLoader getClassLoader(Location location) {
		return new SecureClassLoader() {
			@Override
			protected Class<?> findClass(String name)
					throws ClassNotFoundException {
				JavaClassObject jclassObject = jclassMap.get(name);
				byte[] b = jclassObject.getBytes();
				return super.defineClass(name, jclassObject
						.getBytes(), 0, b.length);
			}
		};
	}

	/**
	 * Gives the compiler an instance of the JavaClassObject
	 * so that the compiler can write the byte code into it.
	 */
	@Override
	public JavaFileObject getJavaFileForOutput(Location location,
			String className, Kind kind, FileObject sibling)
					throws IOException {
		JavaClassObject jclassObject = new JavaClassObject(className, kind);
		jclassMap.put(className, jclassObject);
		return jclassObject;
	}
}