/*
 * @test
 * @summary sun.util.calendar.LocalGregorianCalendar.Date conformance: class and members.
 * @run main DateTest
 */

public class DateTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.util.calendar.LocalGregorianCalendar$Date", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.util.calendar.LocalGregorianCalendar$Date"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("sun.util.calendar.BaseCalendar$Date"), "superclass sun.util.calendar.BaseCalendar$Date");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("addYear"), "declares method addYear");
        check(mnames.contains("getNormalizedYear"), "declares method getNormalizedYear");
        check(mnames.contains("setEra"), "declares method setEra");
        check(mnames.contains("setLocalEra"), "declares method setLocalEra");
        check(mnames.contains("setLocalYear"), "declares method setLocalYear");
        check(mnames.contains("setNormalizedYear"), "declares method setNormalizedYear");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
