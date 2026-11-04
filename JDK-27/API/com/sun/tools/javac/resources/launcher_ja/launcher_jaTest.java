/*
 * @test
 * @summary com.sun.tools.javac.resources.launcher_ja conformance: internal class reflection.
 * @modules jdk.compiler/com.sun.tools.javac.resources:+open
 * @run main launcher_jaTest
 */
public class launcher_jaTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.tools.javac.resources.launcher_ja", false, launcher_jaTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("com.sun.tools.javac.resources.launcher_ja"), "name");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.util.ListResourceBundle"), "superclass");
        check(c.getInterfaces().length == 0, "interfaces count");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
