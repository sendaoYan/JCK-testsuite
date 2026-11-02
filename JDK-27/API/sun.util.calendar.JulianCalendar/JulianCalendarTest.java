/*
 * @test
 * @summary sun.util.calendar.JulianCalendar conformance: internal package reflection.
 * @modules java.base/sun.util.calendar:+open
 * @run main JulianCalendarTest
 */
public class JulianCalendarTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.util.calendar.JulianCalendar", false, JulianCalendarTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("sun.util.calendar.BaseCalendar"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "getCalendarDate"), "method getCalendarDate");
        check(hasMethod(c, "getCalendarDateFromFixedDate"), "method getCalendarDateFromFixedDate");
        check(hasMethod(c, "getDayOfWeek"), "method getDayOfWeek");
        check(hasMethod(c, "getFixedDate"), "method getFixedDate");
        check(hasMethod(c, "getName"), "method getName");
        check(hasMethod(c, "getYearFromFixedDate"), "method getYearFromFixedDate");
        check(hasMethod(c, "newCalendarDate"), "method newCalendarDate");
        check(hasField(c, "$assertionsDisabled"), "field $assertionsDisabled");
        check(hasField(c, "BCE"), "field BCE");
        check(hasField(c, "CE"), "field CE");
        check(hasField(c, "JULIAN_EPOCH"), "field JULIAN_EPOCH");
        check(hasField(c, "eras"), "field eras");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
