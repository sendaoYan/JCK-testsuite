/*
 * @test
 * @summary com.sun.tools.javac.util.JCDiagnostic.Note conformance: internal class reflection.
 * @modules jdk.compiler/com.sun.tools.javac.util:+open
 * @run main NoteTest
 */
public class NoteTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.tools.javac.util.JCDiagnostic$Note", false, NoteTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("com.sun.tools.javac.util.JCDiagnostic$Note"), "name");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.tools.javac.util.JCDiagnostic$DiagnosticInfo"), "superclass");
        check(c.getInterfaces().length == 0, "interfaces count");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
