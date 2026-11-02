/*
 * @test
 * @summary sun.management.MemoryUsageCompositeData conformance: class and members.
 * @run main MemoryUsageCompositeDataTest
 */

public class MemoryUsageCompositeDataTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.management.MemoryUsageCompositeData", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.management.MemoryUsageCompositeData"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("sun.management.LazyCompositeData"), "superclass sun.management.LazyCompositeData");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("getCommitted"), "declares method getCommitted");
        check(mnames.contains("getCompositeData"), "declares method getCompositeData");
        check(mnames.contains("getInit"), "declares method getInit");
        check(mnames.contains("getMax"), "declares method getMax");
        check(mnames.contains("getMemoryUsage"), "declares method getMemoryUsage");
        check(mnames.contains("getMemoryUsageCompositeType"), "declares method getMemoryUsageCompositeType");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
