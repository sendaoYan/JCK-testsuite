/*
 * @test
 * @summary sun.util.locale.BaseLocale conformance: class and members.
 * @run main BaseLocaleTest
 */

public class BaseLocaleTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.util.locale.BaseLocale", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.util.locale.BaseLocale"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("createInstance"), "declares method createInstance");
        check(mnames.contains("equals"), "declares method equals");
        check(mnames.contains("getInstance"), "declares method getInstance");
        check(mnames.contains("getLanguage"), "declares method getLanguage");
        check(mnames.contains("getRegion"), "declares method getRegion");
        check(mnames.contains("getScript"), "declares method getScript");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
