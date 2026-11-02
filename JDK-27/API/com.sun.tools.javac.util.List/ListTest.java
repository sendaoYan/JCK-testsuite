/*
 * @test
 * @summary com.sun.tools.javac.util.List conformance: internal class reflection.
 * @modules jdk.compiler/com.sun.tools.javac.util:+open
 * @run main ListTest
 */
public class ListTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> x : c.getInterfaces()) if (x.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.tools.javac.util.List", false, ListTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("com.sun.tools.javac.util.List"), "name");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.util.AbstractCollection"), "superclass");
        check(c.getInterfaces().length == 1, "interfaces count");
        check(hasInterface(c, "java.util.List"), "interface java.util.List");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
