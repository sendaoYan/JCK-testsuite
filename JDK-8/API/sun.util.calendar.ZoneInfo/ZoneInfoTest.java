/*
 * @test
 * @summary sun.util.calendar.ZoneInfo conformance: class and members.
 * @run main ZoneInfoTest
 */

public class ZoneInfoTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.util.calendar.ZoneInfo", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.util.calendar.ZoneInfo"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.util.TimeZone"), "superclass java.util.TimeZone");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("clone"), "declares method clone");
        check(mnames.contains("equals"), "declares method equals");
        check(mnames.contains("getAliasTable"), "declares method getAliasTable");
        check(mnames.contains("getAvailableIDs"), "declares method getAvailableIDs");
        check(mnames.contains("getDSTSavings"), "declares method getDSTSavings");
        check(mnames.contains("getLastRawOffset"), "declares method getLastRawOffset");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
