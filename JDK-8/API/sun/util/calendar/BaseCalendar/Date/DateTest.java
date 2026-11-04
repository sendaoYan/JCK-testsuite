/*
 * @test
 * @summary sun.util.calendar.BaseCalendar.Date conformance: abstract class and members.
 * @run main DateTest
 */

public class DateTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.util.calendar.BaseCalendar$Date", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.util.calendar.BaseCalendar$Date"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("sun.util.calendar.CalendarDate"), "superclass sun.util.calendar.CalendarDate");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("getCachedJan1"), "declares method getCachedJan1");
        check(mnames.contains("getCachedYear"), "declares method getCachedYear");
        check(mnames.contains("getNormalizedYear"), "declares method getNormalizedYear");
        check(mnames.contains("hit"), "declares method hit");
        check(mnames.contains("setCache"), "declares method setCache");
        check(mnames.contains("setNormalizedDate"), "declares method setNormalizedDate");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
