/*
 * @test
 * @summary sun.management.counter.ByteArrayCounter conformance: interface and members.
 * @run main ByteArrayCounterTest
 */

public class ByteArrayCounterTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.management.counter.ByteArrayCounter", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.management.counter.ByteArrayCounter"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        check(ifaces.contains("sun.management.counter.Counter"), "interface sun.management.counter.Counter");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("byteArrayValue"), "declares method byteArrayValue");
        check(mnames.contains("byteAt"), "declares method byteAt");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
