/*
 * @test
 * @summary com.sun.tools.javac.tree.JCTree.JCFieldAccess conformance: internal class reflection.
 * @modules jdk.compiler/com.sun.tools.javac.tree:+open
 * @run main JCFieldAccessTest
 */
public class JCFieldAccessTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> x : c.getInterfaces()) if (x.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.tools.javac.tree.JCTree$JCFieldAccess", false, JCFieldAccessTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("com.sun.tools.javac.tree.JCTree$JCFieldAccess"), "name");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.tools.javac.tree.JCTree$JCExpression"), "superclass");
        check(c.getInterfaces().length == 1, "interfaces count");
        check(hasInterface(c, "com.sun.source.tree.MemberSelectTree"), "interface com.sun.source.tree.MemberSelectTree");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
