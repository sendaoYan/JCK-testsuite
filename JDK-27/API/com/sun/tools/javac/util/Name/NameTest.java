/*
 * @test
 * @summary com.sun.tools.javac.util.Name conformance: internal abstract class reflection.
 * @modules jdk.compiler/com.sun.tools.javac.util:+open
 * @run main NameTest
 */
public class NameTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> x : c.getInterfaces()) if (x.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.tools.javac.util.Name", false, NameTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("com.sun.tools.javac.util.Name"), "name");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 3, "interfaces count");
        check(hasInterface(c, "javax.lang.model.element.Name"), "interface javax.lang.model.element.Name");
        check(hasInterface(c, "com.sun.tools.javac.jvm.PoolConstant"), "interface com.sun.tools.javac.jvm.PoolConstant");
        check(hasInterface(c, "java.lang.Comparable"), "interface java.lang.Comparable");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
