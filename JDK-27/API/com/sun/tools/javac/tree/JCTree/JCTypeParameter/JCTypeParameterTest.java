/*
 * @test
 * @summary com.sun.tools.javac.tree.JCTree.JCTypeParameter conformance: internal class reflection.
 * @modules jdk.compiler/com.sun.tools.javac.tree:+open
 * @run main JCTypeParameterTest
 */
public class JCTypeParameterTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> x : c.getInterfaces()) if (x.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.tools.javac.tree.JCTree$JCTypeParameter", false, JCTypeParameterTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("com.sun.tools.javac.tree.JCTree$JCTypeParameter"), "name");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.tools.javac.tree.JCTree"), "superclass");
        check(c.getInterfaces().length == 1, "interfaces count");
        check(hasInterface(c, "com.sun.source.tree.TypeParameterTree"), "interface com.sun.source.tree.TypeParameterTree");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
