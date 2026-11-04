/*
 * @test
 * @summary sun.management.HotspotInternalMBean conformance: interface and members.
 * @run main HotspotInternalMBeanTest
 */

public class HotspotInternalMBeanTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.management.HotspotInternalMBean", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.management.HotspotInternalMBean"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
