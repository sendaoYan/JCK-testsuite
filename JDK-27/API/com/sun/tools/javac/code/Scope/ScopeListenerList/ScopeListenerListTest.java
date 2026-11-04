/*
 * @test
 * @summary com.sun.tools.javac.code.Scope.ScopeListenerList conformance: internal class reflection.
 * @modules jdk.compiler/com.sun.tools.javac.code:+open
 * @run main ScopeListenerListTest
 */
public class ScopeListenerListTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.tools.javac.code.Scope$ScopeListenerList", false, ScopeListenerListTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("com.sun.tools.javac.code.Scope$ScopeListenerList"), "name");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "interfaces count");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
