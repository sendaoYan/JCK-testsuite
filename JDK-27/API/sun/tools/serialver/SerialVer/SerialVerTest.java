/*
 * @test
 * @summary sun.tools.serialver.SerialVer conformance: internal class reflection.
 * @modules jdk.compiler/sun.tools.serialver:+open
 * @run main SerialVerTest
 */
public class SerialVerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.tools.serialver.SerialVer", false, SerialVerTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("sun.tools.serialver.SerialVer"), "name");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "interfaces count");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
