/*
 * @test
 * @summary com.sun.tools.javac.util.ForwardingDiagnosticFormatter conformance: internal class reflection.
 * @modules jdk.compiler/com.sun.tools.javac.util:+open
 * @run main ForwardingDiagnosticFormatterTest
 */
public class ForwardingDiagnosticFormatterTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> x : c.getInterfaces()) if (x.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.tools.javac.util.ForwardingDiagnosticFormatter", false, ForwardingDiagnosticFormatterTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("com.sun.tools.javac.util.ForwardingDiagnosticFormatter"), "name");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 1, "interfaces count");
        check(hasInterface(c, "com.sun.tools.javac.api.DiagnosticFormatter"), "interface com.sun.tools.javac.api.DiagnosticFormatter");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
