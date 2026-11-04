/*
 * @test
 * @summary sun.util.calendar.Gregorian conformance: class and members.
 * @run main GregorianTest
 */

public class GregorianTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.util.calendar.Gregorian", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.util.calendar.Gregorian"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("sun.util.calendar.BaseCalendar"), "superclass sun.util.calendar.BaseCalendar");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("getCalendarDate"), "declares method getCalendarDate");
        check(mnames.contains("getName"), "declares method getName");
        check(mnames.contains("newCalendarDate"), "declares method newCalendarDate");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
