/*
 * @test
 * @summary sun.management.counter.Counter conformance: interface and members.
 * @run main CounterTest
 */

public class CounterTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.management.counter.Counter", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.management.counter.Counter"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        check(ifaces.contains("java.io.Serializable"), "interface java.io.Serializable");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("getFlags"), "declares method getFlags");
        check(mnames.contains("getName"), "declares method getName");
        check(mnames.contains("getUnits"), "declares method getUnits");
        check(mnames.contains("getValue"), "declares method getValue");
        check(mnames.contains("getVariability"), "declares method getVariability");
        check(mnames.contains("getVectorLength"), "declares method getVectorLength");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
