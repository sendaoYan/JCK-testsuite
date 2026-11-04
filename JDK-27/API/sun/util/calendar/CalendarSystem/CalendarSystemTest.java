/*
 * @test
 * @summary sun.util.calendar.CalendarSystem conformance: internal package reflection.
 * @modules java.base/sun.util.calendar:+open
 * @run main CalendarSystemTest
 */
public class CalendarSystemTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.util.calendar.CalendarSystem", false, CalendarSystemTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "forName"), "method forName");
        check(hasMethod(c, "getCalendarDate"), "method getCalendarDate");
        check(hasMethod(c, "getEra"), "method getEra");
        check(hasMethod(c, "getEras"), "method getEras");
        check(hasMethod(c, "getGregorianCalendar"), "method getGregorianCalendar");
        check(hasMethod(c, "getMonthLength"), "method getMonthLength");
        check(hasMethod(c, "getName"), "method getName");
        check(hasMethod(c, "getNthDayOfWeek"), "method getNthDayOfWeek");
        check(hasMethod(c, "getTime"), "method getTime");
        check(hasMethod(c, "getYearLength"), "method getYearLength");
        check(hasMethod(c, "newCalendarDate"), "method newCalendarDate");
        check(hasMethod(c, "normalize"), "method normalize");
        check(hasMethod(c, "setTimeOfDay"), "method setTimeOfDay");
        check(hasMethod(c, "validate"), "method validate");
        check(hasField(c, "PACKAGE_NAME"), "field PACKAGE_NAME");
        check(hasField(c, "calendars"), "field calendars");
        check(hasField(c, "initialized"), "field initialized");
        check(hasField(c, "namePairs"), "field namePairs");
        check(hasField(c, "names"), "field names");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
