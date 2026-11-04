/*
 * @test
 * @summary com.sun.tools.javac.util.AbstractDiagnosticFormatter.SimpleConfiguration conformance: internal class reflection.
 * @modules jdk.compiler/com.sun.tools.javac.util:+open
 * @run main SimpleConfigurationTest
 */
public class SimpleConfigurationTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> x : c.getInterfaces()) if (x.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.tools.javac.util.AbstractDiagnosticFormatter$SimpleConfiguration", false, SimpleConfigurationTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("com.sun.tools.javac.util.AbstractDiagnosticFormatter$SimpleConfiguration"), "name");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 1, "interfaces count");
        check(hasInterface(c, "com.sun.tools.javac.api.DiagnosticFormatter$Configuration"), "interface com.sun.tools.javac.api.DiagnosticFormatter$Configuration");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
