/*
 * @test
 * @summary sun.misc.GC conformance: class and members.
 * @run main GCTest
 */

public class GCTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.misc.GC", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.misc.GC"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("currentLatencyTarget"), "declares method currentLatencyTarget");
        check(mnames.contains("maxObjectInspectionAge"), "declares method maxObjectInspectionAge");
        check(mnames.contains("requestLatency"), "declares method requestLatency");
        check(mnames.contains("setLatencyTarget"), "declares method setLatencyTarget");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
