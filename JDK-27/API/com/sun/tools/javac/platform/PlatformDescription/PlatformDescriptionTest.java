/*
 * @test
 * @summary com.sun.tools.javac.platform.PlatformDescription conformance: internal interface reflection.
 * @modules jdk.compiler/com.sun.tools.javac.platform:+open
 * @run main PlatformDescriptionTest
 */
public class PlatformDescriptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> x : c.getInterfaces()) if (x.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.tools.javac.platform.PlatformDescription", false, PlatformDescriptionTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("com.sun.tools.javac.platform.PlatformDescription"), "name");
        check(c.isInterface(), "interface");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "abstract");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 1, "interfaces count");
        check(hasInterface(c, "java.io.Closeable"), "interface java.io.Closeable");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
