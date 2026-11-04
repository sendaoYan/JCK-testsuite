/*
 * @test
 * @summary sun.util.calendar.LocalGregorianCalendar conformance: class and members.
 * @run main LocalGregorianCalendarTest
 */

public class LocalGregorianCalendarTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.util.calendar.LocalGregorianCalendar", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.util.calendar.LocalGregorianCalendar"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("sun.util.calendar.BaseCalendar"), "superclass sun.util.calendar.BaseCalendar");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("adjustYear"), "declares method adjustYear");
        check(mnames.contains("getCalendarDate"), "declares method getCalendarDate");
        check(mnames.contains("getCalendarDateFromFixedDate"), "declares method getCalendarDateFromFixedDate");
        check(mnames.contains("getLocalGregorianCalendar"), "declares method getLocalGregorianCalendar");
        check(mnames.contains("getName"), "declares method getName");
        check(mnames.contains("isLeapYear"), "declares method isLeapYear");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
