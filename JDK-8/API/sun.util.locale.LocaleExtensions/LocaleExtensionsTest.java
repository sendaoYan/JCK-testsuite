/*
 * @test
 * @summary sun.util.locale.LocaleExtensions conformance: class and members.
 * @run main LocaleExtensionsTest
 */

public class LocaleExtensionsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.util.locale.LocaleExtensions", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.util.locale.LocaleExtensions"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("equals"), "declares method equals");
        check(mnames.contains("getExtension"), "declares method getExtension");
        check(mnames.contains("getExtensionValue"), "declares method getExtensionValue");
        check(mnames.contains("getID"), "declares method getID");
        check(mnames.contains("getKeys"), "declares method getKeys");
        check(mnames.contains("getUnicodeLocaleAttributes"), "declares method getUnicodeLocaleAttributes");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
