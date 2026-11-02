/*
 * @test
 * @summary sun.management.MonitorInfoCompositeData conformance: class and members.
 * @run main MonitorInfoCompositeDataTest
 */

public class MonitorInfoCompositeDataTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.management.MonitorInfoCompositeData", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.management.MonitorInfoCompositeData"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("sun.management.LazyCompositeData"), "superclass sun.management.LazyCompositeData");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("getClassName"), "declares method getClassName");
        check(mnames.contains("getCompositeData"), "declares method getCompositeData");
        check(mnames.contains("getIdentityHashCode"), "declares method getIdentityHashCode");
        check(mnames.contains("getLockedStackDepth"), "declares method getLockedStackDepth");
        check(mnames.contains("getLockedStackFrame"), "declares method getLockedStackFrame");
        check(mnames.contains("getMonitorInfo"), "declares method getMonitorInfo");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
