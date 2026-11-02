/*
 * @test
 * @summary sun.util.calendar.CalendarUtils conformance: class and members.
 * @run main CalendarUtilsTest
 */

public class CalendarUtilsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.util.calendar.CalendarUtils", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.util.calendar.CalendarUtils"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("amod"), "declares method amod");
        check(mnames.contains("floorDivide"), "declares method floorDivide");
        check(mnames.contains("isGregorianLeapYear"), "declares method isGregorianLeapYear");
        check(mnames.contains("isJulianLeapYear"), "declares method isJulianLeapYear");
        check(mnames.contains("mod"), "declares method mod");
        check(mnames.contains("sprintf0d"), "declares method sprintf0d");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
