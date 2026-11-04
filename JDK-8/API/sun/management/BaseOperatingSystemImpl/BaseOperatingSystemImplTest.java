/*
 * @test
 * @summary sun.management.BaseOperatingSystemImpl conformance: class and members.
 * @run main BaseOperatingSystemImplTest
 */

public class BaseOperatingSystemImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.management.BaseOperatingSystemImpl", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.management.BaseOperatingSystemImpl"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        check(ifaces.contains("java.lang.management.OperatingSystemMXBean"), "interface java.lang.management.OperatingSystemMXBean");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("getArch"), "declares method getArch");
        check(mnames.contains("getAvailableProcessors"), "declares method getAvailableProcessors");
        check(mnames.contains("getName"), "declares method getName");
        check(mnames.contains("getObjectName"), "declares method getObjectName");
        check(mnames.contains("getSystemLoadAverage"), "declares method getSystemLoadAverage");
        check(mnames.contains("getVersion"), "declares method getVersion");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
