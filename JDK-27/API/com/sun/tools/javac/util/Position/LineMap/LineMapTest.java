/*
 * @test
 * @summary com.sun.tools.javac.util.Position.LineMap conformance: internal interface reflection.
 * @modules jdk.compiler/com.sun.tools.javac.util:+open
 * @run main LineMapTest
 */
public class LineMapTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> x : c.getInterfaces()) if (x.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.tools.javac.util.Position$LineMap", false, LineMapTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("com.sun.tools.javac.util.Position$LineMap"), "name");
        check(c.isInterface(), "interface");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "abstract");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 1, "interfaces count");
        check(hasInterface(c, "com.sun.source.tree.LineMap"), "interface com.sun.source.tree.LineMap");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
