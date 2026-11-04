/*
 * @test
 * @summary com.sun.tools.javac.tree.DCTree.DCDeprecated conformance: internal class reflection.
 * @modules jdk.compiler/com.sun.tools.javac.tree:+open
 * @run main DCDeprecatedTest
 */
public class DCDeprecatedTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> x : c.getInterfaces()) if (x.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.tools.javac.tree.DCTree$DCDeprecated", false, DCDeprecatedTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("com.sun.tools.javac.tree.DCTree$DCDeprecated"), "name");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.tools.javac.tree.DCTree$DCBlockTag"), "superclass");
        check(c.getInterfaces().length == 1, "interfaces count");
        check(hasInterface(c, "com.sun.source.doctree.DeprecatedTree"), "interface com.sun.source.doctree.DeprecatedTree");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
