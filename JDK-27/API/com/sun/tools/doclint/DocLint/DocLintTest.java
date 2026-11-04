/*
 * @test
 * @summary com.sun.tools.doclint.DocLint conformance: internal abstract class reflection.
 * @modules jdk.compiler/com.sun.tools.doclint:+open
 * @run main DocLintTest
 */
public class DocLintTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> x : c.getInterfaces()) if (x.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.tools.doclint.DocLint", false, DocLintTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("com.sun.tools.doclint.DocLint"), "name");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 1, "interfaces count");
        check(hasInterface(c, "com.sun.source.util.Plugin"), "interface com.sun.source.util.Plugin");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
