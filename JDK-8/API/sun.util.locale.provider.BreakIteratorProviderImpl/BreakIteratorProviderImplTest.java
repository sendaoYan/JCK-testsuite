/*
 * @test
 * @summary sun.util.locale.provider.BreakIteratorProviderImpl conformance: class and members.
 * @run main BreakIteratorProviderImplTest
 */

public class BreakIteratorProviderImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.util.locale.provider.BreakIteratorProviderImpl", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.util.locale.provider.BreakIteratorProviderImpl"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.text.spi.BreakIteratorProvider"), "superclass java.text.spi.BreakIteratorProvider");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        check(ifaces.contains("sun.util.locale.provider.AvailableLanguageTags"), "interface sun.util.locale.provider.AvailableLanguageTags");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("getAvailableLanguageTags"), "declares method getAvailableLanguageTags");
        check(mnames.contains("getAvailableLocales"), "declares method getAvailableLocales");
        check(mnames.contains("getBreakInstance"), "declares method getBreakInstance");
        check(mnames.contains("getCharacterInstance"), "declares method getCharacterInstance");
        check(mnames.contains("getLineInstance"), "declares method getLineInstance");
        check(mnames.contains("getSentenceInstance"), "declares method getSentenceInstance");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
