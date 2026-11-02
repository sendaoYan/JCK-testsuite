/*
 * @test
 * @summary sun.util.locale.InternalLocaleBuilder conformance: class and members.
 * @run main InternalLocaleBuilderTest
 */

public class InternalLocaleBuilderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.util.locale.InternalLocaleBuilder", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.util.locale.InternalLocaleBuilder"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("addUnicodeLocaleAttribute"), "declares method addUnicodeLocaleAttribute");
        check(mnames.contains("checkVariants"), "declares method checkVariants");
        check(mnames.contains("clear"), "declares method clear");
        check(mnames.contains("clearExtensions"), "declares method clearExtensions");
        check(mnames.contains("getBaseLocale"), "declares method getBaseLocale");
        check(mnames.contains("getLocaleExtensions"), "declares method getLocaleExtensions");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
