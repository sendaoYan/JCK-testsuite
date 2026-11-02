/*
 * @test
 * @summary com.sun.tools.javac.tree.DCTree.DCEndPosTree conformance: internal abstract class reflection.
 * @modules jdk.compiler/com.sun.tools.javac.tree:+open
 * @run main DCEndPosTreeTest
 */
public class DCEndPosTreeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.tools.javac.tree.DCTree$DCEndPosTree", false, DCEndPosTreeTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("com.sun.tools.javac.tree.DCTree$DCEndPosTree"), "name");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.tools.javac.tree.DCTree"), "superclass");
        check(c.getInterfaces().length == 0, "interfaces count");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
