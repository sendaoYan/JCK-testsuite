/*
 * @test
 * @summary sun.util.resources.LocaleNamesBundle conformance: abstract class and members.
 * @run main LocaleNamesBundleTest
 */

public class LocaleNamesBundleTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.util.resources.LocaleNamesBundle", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.util.resources.LocaleNamesBundle"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("sun.util.resources.OpenListResourceBundle"), "superclass sun.util.resources.OpenListResourceBundle");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
