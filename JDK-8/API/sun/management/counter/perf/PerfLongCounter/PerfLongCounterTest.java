/*
 * @test
 * @summary sun.management.counter.perf.PerfLongCounter conformance: class and members.
 * @run main PerfLongCounterTest
 */

public class PerfLongCounterTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.management.counter.perf.PerfLongCounter", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.management.counter.perf.PerfLongCounter"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("sun.management.counter.AbstractCounter"), "superclass sun.management.counter.AbstractCounter");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        check(ifaces.contains("sun.management.counter.LongCounter"), "interface sun.management.counter.LongCounter");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("getValue"), "declares method getValue");
        check(mnames.contains("longValue"), "declares method longValue");
        check(mnames.contains("writeReplace"), "declares method writeReplace");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
