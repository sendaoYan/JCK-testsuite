/*
 * @test
 * @summary sun.misc.SoftCache conformance: class and members.
 * @run main SoftCacheTest
 */

public class SoftCacheTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.misc.SoftCache", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.misc.SoftCache"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.util.AbstractMap"), "superclass java.util.AbstractMap");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        check(ifaces.contains("java.util.Map"), "interface java.util.Map");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("clear"), "declares method clear");
        check(mnames.contains("containsKey"), "declares method containsKey");
        check(mnames.contains("entrySet"), "declares method entrySet");
        check(mnames.contains("fill"), "declares method fill");
        check(mnames.contains("get"), "declares method get");
        check(mnames.contains("isEmpty"), "declares method isEmpty");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
