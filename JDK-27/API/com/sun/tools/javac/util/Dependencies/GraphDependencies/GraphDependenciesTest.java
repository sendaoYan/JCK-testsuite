/*
 * @test
 * @summary com.sun.tools.javac.util.Dependencies.GraphDependencies conformance: internal class reflection.
 * @modules jdk.compiler/com.sun.tools.javac.util:+open
 * @run main GraphDependenciesTest
 */
public class GraphDependenciesTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> x : c.getInterfaces()) if (x.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.tools.javac.util.Dependencies$GraphDependencies", false, GraphDependenciesTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("com.sun.tools.javac.util.Dependencies$GraphDependencies"), "name");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.tools.javac.util.Dependencies"), "superclass");
        check(c.getInterfaces().length == 2, "interfaces count");
        check(hasInterface(c, "java.io.Closeable"), "interface java.io.Closeable");
        check(hasInterface(c, "com.sun.tools.javac.code.Symbol$Completer"), "interface com.sun.tools.javac.code.Symbol$Completer");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
