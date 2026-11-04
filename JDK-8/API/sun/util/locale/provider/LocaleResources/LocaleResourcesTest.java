/*
 * @test
 * @summary sun.util.locale.provider.LocaleResources conformance: class and members.
 * @run main LocaleResourcesTest
 */

public class LocaleResourcesTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.util.locale.provider.LocaleResources", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.util.locale.provider.LocaleResources"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("getBreakIteratorInfo"), "declares method getBreakIteratorInfo");
        check(mnames.contains("getCalendarData"), "declares method getCalendarData");
        check(mnames.contains("getCalendarNames"), "declares method getCalendarNames");
        check(mnames.contains("getCollationData"), "declares method getCollationData");
        check(mnames.contains("getCurrencyName"), "declares method getCurrencyName");
        check(mnames.contains("getDateTimePattern"), "declares method getDateTimePattern");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
