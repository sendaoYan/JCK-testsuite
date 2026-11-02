/*
 * @test
 * @summary sun.util.locale.LocaleObjectCache conformance: abstract class and members.
 * @run main LocaleObjectCacheTest
 */

public class LocaleObjectCacheTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.util.locale.LocaleObjectCache", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.util.locale.LocaleObjectCache"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("cleanStaleEntries"), "declares method cleanStaleEntries");
        check(mnames.contains("createObject"), "declares method createObject");
        check(mnames.contains("get"), "declares method get");
        check(mnames.contains("normalizeKey"), "declares method normalizeKey");
        check(mnames.contains("put"), "declares method put");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
