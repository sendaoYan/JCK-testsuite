/*
 * @test
 * @summary sun.util.resources.TimeZoneNamesBundle conformance: abstract class and members.
 * @run main TimeZoneNamesBundleTest
 */

public class TimeZoneNamesBundleTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.util.resources.TimeZoneNamesBundle", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.util.resources.TimeZoneNamesBundle"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("sun.util.resources.OpenListResourceBundle"), "superclass sun.util.resources.OpenListResourceBundle");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("createMap"), "declares method createMap");
        check(mnames.contains("createSet"), "declares method createSet");
        check(mnames.contains("getContents"), "declares method getContents");
        check(mnames.contains("handleGetObject"), "declares method handleGetObject");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
