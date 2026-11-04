/*
 * @test
 * @summary com.sun.tools.javac.util.ListBuffer conformance: internal class reflection.
 * @modules jdk.compiler/com.sun.tools.javac.util:+open
 * @run main ListBufferTest
 */
public class ListBufferTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.tools.javac.util.ListBuffer", false, ListBufferTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("com.sun.tools.javac.util.ListBuffer"), "name");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.util.AbstractQueue"), "superclass");
        check(c.getInterfaces().length == 0, "interfaces count");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
