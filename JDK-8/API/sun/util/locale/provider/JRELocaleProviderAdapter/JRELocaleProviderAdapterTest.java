/*
 * @test
 * @summary sun.util.locale.provider.JRELocaleProviderAdapter conformance: class and members.
 * @run main JRELocaleProviderAdapterTest
 */

public class JRELocaleProviderAdapterTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.util.locale.provider.JRELocaleProviderAdapter", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.util.locale.provider.JRELocaleProviderAdapter"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("sun.util.locale.provider.LocaleProviderAdapter"), "superclass sun.util.locale.provider.LocaleProviderAdapter");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        check(ifaces.contains("sun.util.locale.provider.ResourceBundleBasedAdapter"), "interface sun.util.locale.provider.ResourceBundleBasedAdapter");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("createAvailableLocales"), "declares method createAvailableLocales");
        check(mnames.contains("createLanguageTagSet"), "declares method createLanguageTagSet");
        check(mnames.contains("getAdapterType"), "declares method getAdapterType");
        check(mnames.contains("getAvailableLocales"), "declares method getAvailableLocales");
        check(mnames.contains("getBreakIteratorProvider"), "declares method getBreakIteratorProvider");
        check(mnames.contains("getCalendarDataProvider"), "declares method getCalendarDataProvider");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
