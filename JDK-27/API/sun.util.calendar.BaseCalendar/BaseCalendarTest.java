/*
 * @test
 * @summary sun.util.calendar.BaseCalendar conformance: internal package reflection.
 * @modules java.base/sun.util.calendar:+open
 * @run main BaseCalendarTest
 */
public class BaseCalendarTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.util.calendar.BaseCalendar", false, BaseCalendarTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("sun.util.calendar.AbstractCalendar"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "getCalendarDateFromFixedDate"), "method getCalendarDateFromFixedDate");
        check(hasMethod(c, "getDayOfWeek"), "method getDayOfWeek");
        check(hasMethod(c, "getDayOfWeekFromFixedDate"), "method getDayOfWeekFromFixedDate");
        check(hasMethod(c, "getDayOfYear"), "method getDayOfYear");
        check(hasMethod(c, "getFixedDate"), "method getFixedDate");
        check(hasMethod(c, "getMonthLength"), "method getMonthLength");
        check(hasMethod(c, "getYearFromFixedDate"), "method getYearFromFixedDate");
        check(hasMethod(c, "getYearLength"), "method getYearLength");
        check(hasMethod(c, "isLeapYear"), "method isLeapYear");
        check(hasMethod(c, "normalize"), "method normalize");
        check(hasMethod(c, "validate"), "method validate");
        check(hasField(c, "$assertionsDisabled"), "field $assertionsDisabled");
        check(hasField(c, "ACCUMULATED_DAYS_IN_MONTH"), "field ACCUMULATED_DAYS_IN_MONTH");
        check(hasField(c, "ACCUMULATED_DAYS_IN_MONTH_LEAP"), "field ACCUMULATED_DAYS_IN_MONTH_LEAP");
        check(hasField(c, "APRIL"), "field APRIL");
        check(hasField(c, "AUGUST"), "field AUGUST");
        check(hasField(c, "BASE_YEAR"), "field BASE_YEAR");
        check(hasField(c, "DAYS_IN_MONTH"), "field DAYS_IN_MONTH");
        check(hasField(c, "DECEMBER"), "field DECEMBER");
        check(hasField(c, "FEBRUARY"), "field FEBRUARY");
        check(hasField(c, "FIXED_DATES"), "field FIXED_DATES");
        check(hasField(c, "FRIDAY"), "field FRIDAY");
        check(hasField(c, "JANUARY"), "field JANUARY");
        check(hasField(c, "JULY"), "field JULY");
        check(hasField(c, "JUNE"), "field JUNE");
        check(hasField(c, "MARCH"), "field MARCH");
        check(hasField(c, "MAY"), "field MAY");
        check(hasField(c, "MONDAY"), "field MONDAY");
        check(hasField(c, "NOVEMBER"), "field NOVEMBER");
        check(hasField(c, "OCTOBER"), "field OCTOBER");
        check(hasField(c, "SATURDAY"), "field SATURDAY");
        check(hasField(c, "SEPTEMBER"), "field SEPTEMBER");
        check(hasField(c, "SUNDAY"), "field SUNDAY");
        check(hasField(c, "THURSDAY"), "field THURSDAY");
        check(hasField(c, "TUESDAY"), "field TUESDAY");
        check(hasField(c, "WEDNESDAY"), "field WEDNESDAY");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
