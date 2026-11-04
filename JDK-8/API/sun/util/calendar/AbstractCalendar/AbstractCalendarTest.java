/*
 * @test
 * @summary sun.util.calendar.AbstractCalendar conformance: abstract class and members.
 * @run main AbstractCalendarTest
 */

public class AbstractCalendarTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.util.calendar.AbstractCalendar", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.util.calendar.AbstractCalendar"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("sun.util.calendar.CalendarSystem"), "superclass sun.util.calendar.CalendarSystem");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("getCalendarDate"), "declares method getCalendarDate");
        check(mnames.contains("getCalendarDateFromFixedDate"), "declares method getCalendarDateFromFixedDate");
        check(mnames.contains("getDayOfWeekDateAfter"), "declares method getDayOfWeekDateAfter");
        check(mnames.contains("getDayOfWeekDateBefore"), "declares method getDayOfWeekDateBefore");
        check(mnames.contains("getDayOfWeekDateOnOrBefore"), "declares method getDayOfWeekDateOnOrBefore");
        check(mnames.contains("getEra"), "declares method getEra");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
