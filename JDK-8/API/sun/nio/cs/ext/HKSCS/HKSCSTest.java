/*
 * @test
 * @summary sun.nio.cs.ext.HKSCS conformance: class and members.
 * @run main HKSCSTest
 */

public class HKSCSTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.nio.cs.ext.HKSCS", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.nio.cs.ext.HKSCS"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
