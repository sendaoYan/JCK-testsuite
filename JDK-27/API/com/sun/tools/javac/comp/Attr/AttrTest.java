/*
 * @test
 * @summary com.sun.tools.javac.comp.Attr conformance: internal class reflection.
 * @modules jdk.compiler/com.sun.tools.javac.comp:+open
 * @run main AttrTest
 */
public class AttrTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.tools.javac.comp.Attr", false, AttrTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("com.sun.tools.javac.comp.Attr"), "name");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.tools.javac.tree.JCTree$Visitor"), "superclass");
        check(c.getInterfaces().length == 0, "interfaces count");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
