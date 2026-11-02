/*
 * @test
 * @summary com.sun.tools.javac.tree.JCTree.JCPatternCaseLabel conformance: internal class reflection.
 * @modules jdk.compiler/com.sun.tools.javac.tree:+open
 * @run main JCPatternCaseLabelTest
 */
public class JCPatternCaseLabelTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> x : c.getInterfaces()) if (x.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.tools.javac.tree.JCTree$JCPatternCaseLabel", false, JCPatternCaseLabelTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("com.sun.tools.javac.tree.JCTree$JCPatternCaseLabel"), "name");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.tools.javac.tree.JCTree$JCCaseLabel"), "superclass");
        check(c.getInterfaces().length == 1, "interfaces count");
        check(hasInterface(c, "com.sun.source.tree.PatternCaseLabelTree"), "interface com.sun.source.tree.PatternCaseLabelTree");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
