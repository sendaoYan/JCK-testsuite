/*
 * @test
 * @summary sun.util.resources.en.TimeZoneNames_en_CA conformance: class and members.
 * @run main TimeZoneNames_en_CATest
 */

public class TimeZoneNames_en_CATest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.util.resources.en.TimeZoneNames_en_CA", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.util.resources.en.TimeZoneNames_en_CA"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("sun.util.resources.TimeZoneNamesBundle"), "superclass sun.util.resources.TimeZoneNamesBundle");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("getContents"), "declares method getContents");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
