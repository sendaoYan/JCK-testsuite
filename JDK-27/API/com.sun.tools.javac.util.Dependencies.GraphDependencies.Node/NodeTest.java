/*
 * @test
 * @summary com.sun.tools.javac.util.Dependencies.GraphDependencies.Node conformance: internal abstract class reflection.
 * @modules jdk.compiler/com.sun.tools.javac.util:+open
 * @run main NodeTest
 */
public class NodeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> x : c.getInterfaces()) if (x.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.tools.javac.util.Dependencies$GraphDependencies$Node", false, NodeTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("com.sun.tools.javac.util.Dependencies$GraphDependencies$Node"), "name");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.tools.javac.util.GraphUtils$AbstractNode"), "superclass");
        check(c.getInterfaces().length == 1, "interfaces count");
        check(hasInterface(c, "com.sun.tools.javac.util.GraphUtils$DottableNode"), "interface com.sun.tools.javac.util.GraphUtils$DottableNode");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
