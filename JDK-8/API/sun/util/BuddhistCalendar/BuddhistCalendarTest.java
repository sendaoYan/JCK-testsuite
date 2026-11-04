/*
 * @test
 * @summary sun.util.BuddhistCalendar conformance: class and members.
 * @run main BuddhistCalendarTest
 */

public class BuddhistCalendarTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.util.BuddhistCalendar", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.util.BuddhistCalendar"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.util.GregorianCalendar"), "superclass java.util.GregorianCalendar");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("add"), "declares method add");
        check(mnames.contains("equals"), "declares method equals");
        check(mnames.contains("get"), "declares method get");
        check(mnames.contains("getActualMaximum"), "declares method getActualMaximum");
        check(mnames.contains("getCalendarType"), "declares method getCalendarType");
        check(mnames.contains("getDisplayName"), "declares method getDisplayName");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
