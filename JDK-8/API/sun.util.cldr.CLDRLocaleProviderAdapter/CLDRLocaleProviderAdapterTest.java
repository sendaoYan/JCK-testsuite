/*
 * @test
 * @summary sun.util.cldr.CLDRLocaleProviderAdapter conformance: class and members.
 * @run main CLDRLocaleProviderAdapterTest
 */

public class CLDRLocaleProviderAdapterTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.util.cldr.CLDRLocaleProviderAdapter", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.util.cldr.CLDRLocaleProviderAdapter"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("sun.util.locale.provider.JRELocaleProviderAdapter"), "superclass sun.util.locale.provider.JRELocaleProviderAdapter");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("createLanguageTagSet"), "declares method createLanguageTagSet");
        check(mnames.contains("getAdapterType"), "declares method getAdapterType");
        check(mnames.contains("getAvailableLocales"), "declares method getAvailableLocales");
        check(mnames.contains("getBreakIteratorProvider"), "declares method getBreakIteratorProvider");
        check(mnames.contains("getCollatorProvider"), "declares method getCollatorProvider");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
