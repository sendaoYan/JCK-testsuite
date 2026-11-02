/*
 * @test
 * @summary sun.util.locale.provider.AuxLocaleProviderAdapter conformance: abstract class and members.
 * @run main AuxLocaleProviderAdapterTest
 */

public class AuxLocaleProviderAdapterTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.util.locale.provider.AuxLocaleProviderAdapter", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.util.locale.provider.AuxLocaleProviderAdapter"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("sun.util.locale.provider.LocaleProviderAdapter"), "superclass sun.util.locale.provider.LocaleProviderAdapter");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("findInstalledProvider"), "declares method findInstalledProvider");
        check(mnames.contains("getAvailableLocales"), "declares method getAvailableLocales");
        check(mnames.contains("getBreakIteratorProvider"), "declares method getBreakIteratorProvider");
        check(mnames.contains("getCalendarDataProvider"), "declares method getCalendarDataProvider");
        check(mnames.contains("getCalendarNameProvider"), "declares method getCalendarNameProvider");
        check(mnames.contains("getCalendarProvider"), "declares method getCalendarProvider");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
