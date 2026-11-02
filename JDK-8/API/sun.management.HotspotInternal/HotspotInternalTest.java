/*
 * @test
 * @summary sun.management.HotspotInternal conformance: class and members.
 * @run main HotspotInternalTest
 */

public class HotspotInternalTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.management.HotspotInternal", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.management.HotspotInternal"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        check(ifaces.contains("sun.management.HotspotInternalMBean"), "interface sun.management.HotspotInternalMBean");
        check(ifaces.contains("javax.management.MBeanRegistration"), "interface javax.management.MBeanRegistration");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("postDeregister"), "declares method postDeregister");
        check(mnames.contains("postRegister"), "declares method postRegister");
        check(mnames.contains("preDeregister"), "declares method preDeregister");
        check(mnames.contains("preRegister"), "declares method preRegister");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
