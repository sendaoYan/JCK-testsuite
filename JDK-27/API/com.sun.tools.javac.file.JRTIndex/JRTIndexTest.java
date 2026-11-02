/*
 * @test
 * @summary com.sun.tools.javac.file.JRTIndex conformance: internal class reflection.
 * @modules jdk.compiler/com.sun.tools.javac.file:+open
 * @run main JRTIndexTest
 */
public class JRTIndexTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.tools.javac.file.JRTIndex", false, JRTIndexTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("com.sun.tools.javac.file.JRTIndex"), "name");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "interfaces count");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
