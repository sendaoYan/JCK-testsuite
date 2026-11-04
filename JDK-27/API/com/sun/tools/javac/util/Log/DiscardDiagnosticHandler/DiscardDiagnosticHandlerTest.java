/*
 * @test
 * @summary com.sun.tools.javac.util.Log.DiscardDiagnosticHandler conformance: internal class reflection.
 * @modules jdk.compiler/com.sun.tools.javac.util:+open
 * @run main DiscardDiagnosticHandlerTest
 */
public class DiscardDiagnosticHandlerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.tools.javac.util.Log$DiscardDiagnosticHandler", false, DiscardDiagnosticHandlerTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("com.sun.tools.javac.util.Log$DiscardDiagnosticHandler"), "name");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.tools.javac.util.Log$DiagnosticHandler"), "superclass");
        check(c.getInterfaces().length == 0, "interfaces count");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
