/*
 * @test
 * @summary sun.util.calendar.JulianCalendar conformance: class and members.
 * @run main JulianCalendarTest
 */

public class JulianCalendarTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.util.calendar.JulianCalendar", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.util.calendar.JulianCalendar"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("sun.util.calendar.BaseCalendar"), "superclass sun.util.calendar.BaseCalendar");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("getCalendarDate"), "declares method getCalendarDate");
        check(mnames.contains("getCalendarDateFromFixedDate"), "declares method getCalendarDateFromFixedDate");
        check(mnames.contains("getDayOfWeek"), "declares method getDayOfWeek");
        check(mnames.contains("getFixedDate"), "declares method getFixedDate");
        check(mnames.contains("getName"), "declares method getName");
        check(mnames.contains("getYearFromFixedDate"), "declares method getYearFromFixedDate");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
