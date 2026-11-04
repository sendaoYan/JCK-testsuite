/*
 * @test
 * @summary com.sun.tools.javac.comp.Lower conformance: internal class reflection.
 * @modules jdk.compiler/com.sun.tools.javac.comp:+open
 * @run main LowerTest
 */
public class LowerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.tools.javac.comp.Lower", false, LowerTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("com.sun.tools.javac.comp.Lower"), "name");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.tools.javac.tree.TreeTranslator"), "superclass");
        check(c.getInterfaces().length == 0, "interfaces count");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
