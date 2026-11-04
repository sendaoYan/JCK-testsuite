/*
 * @test
 * @summary sun.misc.GC.LatencyRequest conformance: class and members.
 * @run main LatencyRequestTest
 */

public class LatencyRequestTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.misc.GC$LatencyRequest", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.misc.GC$LatencyRequest"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        check(ifaces.contains("java.lang.Comparable"), "interface java.lang.Comparable");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("adjustLatencyIfNeeded"), "declares method adjustLatencyIfNeeded");
        check(mnames.contains("cancel"), "declares method cancel");
        check(mnames.contains("compareTo"), "declares method compareTo");
        check(mnames.contains("toString"), "declares method toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
