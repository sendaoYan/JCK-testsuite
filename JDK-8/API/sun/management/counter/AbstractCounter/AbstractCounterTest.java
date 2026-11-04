/*
 * @test
 * @summary sun.management.counter.AbstractCounter conformance: abstract class and members.
 * @run main AbstractCounterTest
 */

public class AbstractCounterTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.management.counter.AbstractCounter", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.management.counter.AbstractCounter"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        check(ifaces.contains("sun.management.counter.Counter"), "interface sun.management.counter.Counter");
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
