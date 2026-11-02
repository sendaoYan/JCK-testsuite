/*
 * @test
 * @summary com.sun.tools.javac.tree.JCTree conformance: internal abstract class reflection.
 * @modules jdk.compiler/com.sun.tools.javac.tree:+open
 * @run main JCTreeTest
 */
public class JCTreeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> x : c.getInterfaces()) if (x.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.tools.javac.tree.JCTree", false, JCTreeTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("com.sun.tools.javac.tree.JCTree"), "name");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 3, "interfaces count");
        check(hasInterface(c, "com.sun.source.tree.Tree"), "interface com.sun.source.tree.Tree");
        check(hasInterface(c, "java.lang.Cloneable"), "interface java.lang.Cloneable");
        check(hasInterface(c, "com.sun.tools.javac.util.JCDiagnostic$DiagnosticPosition"), "interface com.sun.tools.javac.util.JCDiagnostic$DiagnosticPosition");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
