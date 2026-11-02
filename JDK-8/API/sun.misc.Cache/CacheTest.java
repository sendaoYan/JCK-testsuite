/*
 * @test
 * @summary sun.misc.Cache conformance: class and members.
 * @run main CacheTest
 */

public class CacheTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.misc.Cache", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.misc.Cache"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.util.Dictionary"), "superclass java.util.Dictionary");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("elements"), "declares method elements");
        check(mnames.contains("get"), "declares method get");
        check(mnames.contains("init"), "declares method init");
        check(mnames.contains("isEmpty"), "declares method isEmpty");
        check(mnames.contains("keys"), "declares method keys");
        check(mnames.contains("put"), "declares method put");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
