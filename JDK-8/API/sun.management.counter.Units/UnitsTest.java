/*
 * @test
 * @summary sun.management.counter.Units conformance: class and members.
 * @run main UnitsTest
 */

public class UnitsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.management.counter.Units", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.management.counter.Units"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        check(ifaces.contains("java.io.Serializable"), "interface java.io.Serializable");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("intValue"), "declares method intValue");
        check(mnames.contains("toString"), "declares method toString");
        check(mnames.contains("toUnits"), "declares method toUnits");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
