/*
 * @test
 * @summary sun.util.locale.provider.CalendarDataUtility conformance: internal package reflection.
 * @modules java.base/sun.util.locale.provider:+open
 * @run main CalendarDataUtilityTest
 */
public class CalendarDataUtilityTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.util.locale.provider.CalendarDataUtility", false, CalendarDataUtilityTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "findRegionOverride"), "method findRegionOverride");
        check(hasMethod(c, "retrieveFieldValueName"), "method retrieveFieldValueName");
        check(hasMethod(c, "retrieveFieldValueNames"), "method retrieveFieldValueNames");
        check(hasMethod(c, "retrieveFirstDayOfWeek"), "method retrieveFirstDayOfWeek");
        check(hasMethod(c, "retrieveJavaTimeFieldValueName"), "method retrieveJavaTimeFieldValueName");
        check(hasMethod(c, "retrieveJavaTimeFieldValueNames"), "method retrieveJavaTimeFieldValueNames");
        check(hasMethod(c, "retrieveMinimalDaysInFirstWeek"), "method retrieveMinimalDaysInFirstWeek");
        check(hasField(c, "FIRST_DAY_OF_WEEK"), "field FIRST_DAY_OF_WEEK");
        check(hasField(c, "MINIMAL_DAYS_IN_FIRST_WEEK"), "field MINIMAL_DAYS_IN_FIRST_WEEK");
        check(hasField(c, "OVERRIDE_BUILDER"), "field OVERRIDE_BUILDER");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
