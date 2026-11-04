/*
 * @test
 * @summary sun.util.locale.provider.LocaleServiceProviderPool conformance: class and members.
 * @run main LocaleServiceProviderPoolTest
 */

public class LocaleServiceProviderPoolTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.util.locale.provider.LocaleServiceProviderPool", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.util.locale.provider.LocaleServiceProviderPool"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("config"), "declares method config");
        check(mnames.contains("findProviders"), "declares method findProviders");
        check(mnames.contains("getAllAvailableLocales"), "declares method getAllAvailableLocales");
        check(mnames.contains("getAvailableLocaleSet"), "declares method getAvailableLocaleSet");
        check(mnames.contains("getAvailableLocales"), "declares method getAvailableLocales");
        check(mnames.contains("getLocalizedObject"), "declares method getLocalizedObject");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
