/*
 * @test
 * @summary sun.util.locale.provider.TimeZoneNameUtility conformance: class and members.
 * @run main TimeZoneNameUtilityTest
 */

public class TimeZoneNameUtilityTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.util.locale.provider.TimeZoneNameUtility", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.util.locale.provider.TimeZoneNameUtility"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("getZoneStrings"), "declares method getZoneStrings");
        check(mnames.contains("loadZoneStrings"), "declares method loadZoneStrings");
        check(mnames.contains("retrieveDisplayName"), "declares method retrieveDisplayName");
        check(mnames.contains("retrieveDisplayNames"), "declares method retrieveDisplayNames");
        check(mnames.contains("retrieveDisplayNamesImpl"), "declares method retrieveDisplayNamesImpl");
        check(mnames.contains("retrieveGenericDisplayName"), "declares method retrieveGenericDisplayName");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
