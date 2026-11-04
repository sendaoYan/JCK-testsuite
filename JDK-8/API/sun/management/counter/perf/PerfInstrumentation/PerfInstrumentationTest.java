/*
 * @test
 * @summary sun.management.counter.perf.PerfInstrumentation conformance: class and members.
 * @run main PerfInstrumentationTest
 */

public class PerfInstrumentationTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.management.counter.perf.PerfInstrumentation", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.management.counter.perf.PerfInstrumentation"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("findByPattern"), "declares method findByPattern");
        check(mnames.contains("getAllCounters"), "declares method getAllCounters");
        check(mnames.contains("getMajorVersion"), "declares method getMajorVersion");
        check(mnames.contains("getMinorVersion"), "declares method getMinorVersion");
        check(mnames.contains("getModificationTimeStamp"), "declares method getModificationTimeStamp");
        check(mnames.contains("getNextCounter"), "declares method getNextCounter");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
