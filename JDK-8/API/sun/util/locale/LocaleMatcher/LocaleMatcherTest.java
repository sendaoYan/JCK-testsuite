/*
 * @test
 * @summary sun.util.locale.LocaleMatcher conformance: class and members.
 * @run main LocaleMatcherTest
 */

public class LocaleMatcherTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.util.locale.LocaleMatcher", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.util.locale.LocaleMatcher"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("filter"), "declares method filter");
        check(mnames.contains("filterBasic"), "declares method filterBasic");
        check(mnames.contains("filterExtended"), "declares method filterExtended");
        check(mnames.contains("filterTags"), "declares method filterTags");
        check(mnames.contains("getEquivalentForRegionAndVariant"), "declares method getEquivalentForRegionAndVariant");
        check(mnames.contains("getEquivalentsForLanguage"), "declares method getEquivalentsForLanguage");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
