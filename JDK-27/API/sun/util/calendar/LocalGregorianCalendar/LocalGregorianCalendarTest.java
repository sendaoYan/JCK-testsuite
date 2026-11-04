/*
 * @test
 * @summary sun.util.calendar.LocalGregorianCalendar conformance: internal package reflection.
 * @modules java.base/sun.util.calendar:+open
 * @run main LocalGregorianCalendarTest
 */
public class LocalGregorianCalendarTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.util.calendar.LocalGregorianCalendar", false, LocalGregorianCalendarTest.class.getClassLoader());
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
        check(hasMethod(c, "getName"), "method getName");
        check(hasMethod(c, "isLeapYear"), "method isLeapYear");
        check(hasMethod(c, "newCalendarDate"), "method newCalendarDate");
        check(hasMethod(c, "normalize"), "method normalize");
        check(hasMethod(c, "validate"), "method validate");
        check(hasField(c, "JAPANESE_ERAS"), "field JAPANESE_ERAS");
        check(hasField(c, "eras"), "field eras");
        check(hasField(c, "name"), "field name");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
