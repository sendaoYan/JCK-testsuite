/*
 * @test
 * @summary com.sun.tools.javac.util.RichDiagnosticFormatter.RichConfiguration conformance: internal class reflection.
 * @modules jdk.compiler/com.sun.tools.javac.util:+open
 * @run main RichConfigurationTest
 */
public class RichConfigurationTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.tools.javac.util.RichDiagnosticFormatter$RichConfiguration", false, RichConfigurationTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("com.sun.tools.javac.util.RichDiagnosticFormatter$RichConfiguration"), "name");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.tools.javac.util.ForwardingDiagnosticFormatter$ForwardingConfiguration"), "superclass");
        check(c.getInterfaces().length == 0, "interfaces count");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
