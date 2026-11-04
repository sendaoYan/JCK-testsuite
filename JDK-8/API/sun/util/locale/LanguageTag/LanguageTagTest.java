/*
 * @test
 * @summary sun.util.locale.LanguageTag conformance: class and members.
 * @run main LanguageTagTest
 */

public class LanguageTagTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.util.locale.LanguageTag", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.util.locale.LanguageTag"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("canonicalizeExtension"), "declares method canonicalizeExtension");
        check(mnames.contains("canonicalizeExtensionSingleton"), "declares method canonicalizeExtensionSingleton");
        check(mnames.contains("canonicalizeExtensionSubtag"), "declares method canonicalizeExtensionSubtag");
        check(mnames.contains("canonicalizeExtlang"), "declares method canonicalizeExtlang");
        check(mnames.contains("canonicalizeLanguage"), "declares method canonicalizeLanguage");
        check(mnames.contains("canonicalizePrivateuse"), "declares method canonicalizePrivateuse");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
