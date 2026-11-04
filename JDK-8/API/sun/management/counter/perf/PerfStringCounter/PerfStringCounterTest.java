/*
 * @test
 * @summary sun.management.counter.perf.PerfStringCounter conformance: class and members.
 * @run main PerfStringCounterTest
 */

public class PerfStringCounterTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.management.counter.perf.PerfStringCounter", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.management.counter.perf.PerfStringCounter"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("sun.management.counter.perf.PerfByteArrayCounter"), "superclass sun.management.counter.perf.PerfByteArrayCounter");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        check(ifaces.contains("sun.management.counter.StringCounter"), "interface sun.management.counter.StringCounter");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("getValue"), "declares method getValue");
        check(mnames.contains("getVectorLength"), "declares method getVectorLength");
        check(mnames.contains("isVector"), "declares method isVector");
        check(mnames.contains("stringValue"), "declares method stringValue");
        check(mnames.contains("writeReplace"), "declares method writeReplace");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
