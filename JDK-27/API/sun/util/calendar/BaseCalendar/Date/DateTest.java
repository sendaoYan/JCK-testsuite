/*
 * @test
 * @summary sun.util.calendar.BaseCalendar.Date conformance: internal package reflection.
 * @modules java.base/sun.util.calendar:+open
 * @run main DateTest
 */
public class DateTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.util.calendar.BaseCalendar$Date", false, DateTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("sun.util.calendar.CalendarDate"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "getCachedJan1"), "method getCachedJan1");
        check(hasMethod(c, "getCachedYear"), "method getCachedYear");
        check(hasMethod(c, "getNormalizedYear"), "method getNormalizedYear");
        check(hasMethod(c, "hit"), "method hit");
        check(hasMethod(c, "setCache"), "method setCache");
        check(hasMethod(c, "setNormalizedDate"), "method setNormalizedDate");
        check(hasMethod(c, "setNormalizedYear"), "method setNormalizedYear");
        check(hasField(c, "cachedFixedDateJan1"), "field cachedFixedDateJan1");
        check(hasField(c, "cachedFixedDateNextJan1"), "field cachedFixedDateNextJan1");
        check(hasField(c, "cachedYear"), "field cachedYear");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
