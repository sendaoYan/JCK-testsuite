/*
 * @test
 * @summary com.sun.tools.javac.jvm.PoolConstant.Dynamic.BsmKey conformance: internal class reflection.
 * @modules jdk.compiler/com.sun.tools.javac.jvm:+open
 * @run main BsmKeyTest
 */
public class BsmKeyTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.tools.javac.jvm.PoolConstant$Dynamic$BsmKey", false, BsmKeyTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("com.sun.tools.javac.jvm.PoolConstant$Dynamic$BsmKey"), "name");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "interfaces count");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
