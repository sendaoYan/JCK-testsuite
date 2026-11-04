/*
 * @test
 * @summary sun.util.calendar.AbstractCalendar conformance: internal package reflection.
 * @modules java.base/sun.util.calendar:+open
 * @run main AbstractCalendarTest
 */
public class AbstractCalendarTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.util.calendar.AbstractCalendar", false, AbstractCalendarTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("sun.util.calendar.CalendarSystem"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "getCalendarDate"), "method getCalendarDate");
        check(hasMethod(c, "getCalendarDateFromFixedDate"), "method getCalendarDateFromFixedDate");
        check(hasMethod(c, "getDayOfWeekDateOnOrBefore"), "method getDayOfWeekDateOnOrBefore");
        check(hasMethod(c, "getEra"), "method getEra");
        check(hasMethod(c, "getEras"), "method getEras");
        check(hasMethod(c, "getFixedDate"), "method getFixedDate");
        check(hasMethod(c, "getNthDayOfWeek"), "method getNthDayOfWeek");
        check(hasMethod(c, "getTime"), "method getTime");
        check(hasMethod(c, "getTimeOfDay"), "method getTimeOfDay");
        check(hasMethod(c, "getTimeOfDayValue"), "method getTimeOfDayValue");
        check(hasMethod(c, "isLeapYear"), "method isLeapYear");
        check(hasMethod(c, "setEras"), "method setEras");
        check(hasMethod(c, "setTimeOfDay"), "method setTimeOfDay");
        check(hasMethod(c, "validateTime"), "method validateTime");
        check(hasField(c, "DAY_IN_MILLIS"), "field DAY_IN_MILLIS");
        check(hasField(c, "EPOCH_OFFSET"), "field EPOCH_OFFSET");
        check(hasField(c, "HOUR_IN_MILLIS"), "field HOUR_IN_MILLIS");
        check(hasField(c, "MINUTE_IN_MILLIS"), "field MINUTE_IN_MILLIS");
        check(hasField(c, "SECOND_IN_MILLIS"), "field SECOND_IN_MILLIS");
        check(hasField(c, "eras"), "field eras");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
