/*
 * @test
 * @summary sun.misc.CompoundEnumeration conformance: class and members.
 * @run main CompoundEnumerationTest
 */

public class CompoundEnumerationTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.misc.CompoundEnumeration", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.misc.CompoundEnumeration"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        check(ifaces.contains("java.util.Enumeration"), "interface java.util.Enumeration");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("hasMoreElements"), "declares method hasMoreElements");
        check(mnames.contains("next"), "declares method next");
        check(mnames.contains("nextElement"), "declares method nextElement");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
