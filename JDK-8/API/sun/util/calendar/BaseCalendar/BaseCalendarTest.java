/*
 * @test
 * @summary sun.util.calendar.BaseCalendar conformance: abstract class and members.
 * @run main BaseCalendarTest
 */

public class BaseCalendarTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.util.calendar.BaseCalendar", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.util.calendar.BaseCalendar"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("sun.util.calendar.AbstractCalendar"), "superclass sun.util.calendar.AbstractCalendar");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("getCalendarDateFromFixedDate"), "declares method getCalendarDateFromFixedDate");
        check(mnames.contains("getDayOfWeek"), "declares method getDayOfWeek");
        check(mnames.contains("getDayOfWeekFromFixedDate"), "declares method getDayOfWeekFromFixedDate");
        check(mnames.contains("getDayOfYear"), "declares method getDayOfYear");
        check(mnames.contains("getFixedDate"), "declares method getFixedDate");
        check(mnames.contains("getGregorianYearFromFixedDate"), "declares method getGregorianYearFromFixedDate");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
