/*
 * @test
 * @summary sun.util.calendar.CalendarDate conformance: abstract class and members.
 * @run main CalendarDateTest
 */

public class CalendarDateTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.util.calendar.CalendarDate", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.util.calendar.CalendarDate"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        check(ifaces.contains("java.lang.Cloneable"), "interface java.lang.Cloneable");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("addDate"), "declares method addDate");
        check(mnames.contains("addDayOfMonth"), "declares method addDayOfMonth");
        check(mnames.contains("addHours"), "declares method addHours");
        check(mnames.contains("addMillis"), "declares method addMillis");
        check(mnames.contains("addMinutes"), "declares method addMinutes");
        check(mnames.contains("addMonth"), "declares method addMonth");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
