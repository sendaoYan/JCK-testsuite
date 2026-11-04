/*
 * @test
 * @summary sun.util.locale.UnicodeLocaleExtension conformance: class and members.
 * @run main UnicodeLocaleExtensionTest
 */

public class UnicodeLocaleExtensionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.util.locale.UnicodeLocaleExtension", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.util.locale.UnicodeLocaleExtension"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("sun.util.locale.Extension"), "superclass sun.util.locale.Extension");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("getID"), "declares method getID");
        check(mnames.contains("getKey"), "declares method getKey");
        check(mnames.contains("getUnicodeLocaleAttributes"), "declares method getUnicodeLocaleAttributes");
        check(mnames.contains("getUnicodeLocaleKeys"), "declares method getUnicodeLocaleKeys");
        check(mnames.contains("getUnicodeLocaleType"), "declares method getUnicodeLocaleType");
        check(mnames.contains("getValue"), "declares method getValue");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
