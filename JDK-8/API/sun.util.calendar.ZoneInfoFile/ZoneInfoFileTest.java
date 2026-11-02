/*
 * @test
 * @summary sun.util.calendar.ZoneInfoFile conformance: class and members.
 * @run main ZoneInfoFileTest
 */

public class ZoneInfoFileTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.util.calendar.ZoneInfoFile", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.util.calendar.ZoneInfoFile"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("addOldMapping"), "declares method addOldMapping");
        check(mnames.contains("addTrans"), "declares method addTrans");
        check(mnames.contains("getAliasMap"), "declares method getAliasMap");
        check(mnames.contains("getCustomTimeZone"), "declares method getCustomTimeZone");
        check(mnames.contains("getStandardOffset"), "declares method getStandardOffset");
        check(mnames.contains("getVersion"), "declares method getVersion");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
