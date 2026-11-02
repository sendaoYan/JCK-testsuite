/*
 * @test
 * @summary com.sun.tools.javac.comp.CompileStates conformance: internal class reflection.
 * @modules jdk.compiler/com.sun.tools.javac.comp:+open
 * @run main CompileStatesTest
 */
public class CompileStatesTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.tools.javac.comp.CompileStates", false, CompileStatesTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("com.sun.tools.javac.comp.CompileStates"), "name");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.util.HashMap"), "superclass");
        check(c.getInterfaces().length == 0, "interfaces count");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
