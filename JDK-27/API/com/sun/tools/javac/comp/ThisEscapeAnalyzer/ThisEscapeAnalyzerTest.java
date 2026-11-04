/*
 * @test
 * @summary com.sun.tools.javac.comp.ThisEscapeAnalyzer conformance: internal class reflection.
 * @modules jdk.compiler/com.sun.tools.javac.comp:+open
 * @run main ThisEscapeAnalyzerTest
 */
public class ThisEscapeAnalyzerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.tools.javac.comp.ThisEscapeAnalyzer", false, ThisEscapeAnalyzerTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("com.sun.tools.javac.comp.ThisEscapeAnalyzer"), "name");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.tools.javac.tree.TreeScanner"), "superclass");
        check(c.getInterfaces().length == 0, "interfaces count");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
