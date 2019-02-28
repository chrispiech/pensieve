package javaEval;
import java.util.*;
import javax.tools.*;

public class DynaCompTest {
    public static void main(String[] args) throws Exception {
        // Full name of the class that will be compiled.
        // If class should be in some package,
        // fullName should contain it too
        // (ex. "testpackage.DynaClass")
        String fullName = "DynaClass";

        // Here we specify the source code of the class to be compiled
        StringBuilder src = new StringBuilder();
        src.append("public class DynaClass {\n");
        src.append("    public String toString() {\n");
        src.append("        return \"Hello, I am \" + ");
        src.append("this.getClass().getSimpleName();\n");
        src.append("    }\n");
        src.append("}\n");

        System.out.println(src);

        // We get an instance of JavaCompiler. Then
        // we create a file manager
        // (our custom implementation of it)
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        JavaFileManager fileManager = new
            ClassFileManager(compiler
                .getStandardFileManager(null, null, null));

        // Dynamic compiling requires specifying
        // a list of "files" to compile. In our case
        // this is a list containing one "file" which is in our case
        // our own implementation (see details below)
        List<JavaFileObject> jfiles = new ArrayList<JavaFileObject>();
        jfiles.add(new CharSequenceJavaFileObject(fullName, src));

        // We specify a task to the compiler. Compiler should use our file
        // manager and our list of "files".
        // Then we run the compilation with call()
        compiler.getTask(null, fileManager, null, null,
            null, jfiles).call();

        // Creating an instance of our compiled class and
        // running its toString() method
        Object instance = fileManager.getClassLoader(null)
            .loadClass(fullName).newInstance();
        System.out.println(instance);
    }
}