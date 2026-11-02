/*
 * @test
 * @summary com.sun.tools.javac.tree.JCTree.JCEnhancedForLoop conformance: internal class reflection.
 * @modules jdk.compiler/com.sun.tools.javac.tree:+open
 * @run main JCEnhancedForLoopTest
 */
public class JCEnhancedForLoopTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> x : c.getInterfaces()) if (x.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.tools.javac.tree.JCTree$JCEnhancedForLoop", false, JCEnhancedForLoopTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("com.sun.tools.javac.tree.JCTree$JCEnhancedForLoop"), "name");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.tools.javac.tree.JCTree$JCStatement"), "superclass");
        check(c.getInterfaces().length == 1, "interfaces count");
        check(hasInterface(c, "com.sun.source.tree.EnhancedForLoopTree"), "interface com.sun.source.tree.EnhancedForLoopTree");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
