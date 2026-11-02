/*
 * @test
 * @summary com.sun.tools.javac.tree.DCTree.DCUnknownInlineTag conformance: internal class reflection.
 * @modules jdk.compiler/com.sun.tools.javac.tree:+open
 * @run main DCUnknownInlineTagTest
 */
public class DCUnknownInlineTagTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> x : c.getInterfaces()) if (x.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.tools.javac.tree.DCTree$DCUnknownInlineTag", false, DCUnknownInlineTagTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("com.sun.tools.javac.tree.DCTree$DCUnknownInlineTag"), "name");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.tools.javac.tree.DCTree$DCInlineTag"), "superclass");
        check(c.getInterfaces().length == 1, "interfaces count");
        check(hasInterface(c, "com.sun.source.doctree.UnknownInlineTagTree"), "interface com.sun.source.doctree.UnknownInlineTagTree");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
