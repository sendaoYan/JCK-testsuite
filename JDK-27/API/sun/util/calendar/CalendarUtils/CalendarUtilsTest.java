/*
 * @test
 * @summary sun.util.calendar.CalendarUtils conformance: internal package reflection.
 * @modules java.base/sun.util.calendar:+open
 * @run main CalendarUtilsTest
 */
public class CalendarUtilsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.util.calendar.CalendarUtils", false, CalendarUtilsTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "amod"), "method amod");
        check(hasMethod(c, "floorDivide"), "method floorDivide");
        check(hasMethod(c, "isGregorianLeapYear"), "method isGregorianLeapYear");
        check(hasMethod(c, "isJulianLeapYear"), "method isJulianLeapYear");
        check(hasMethod(c, "mod"), "method mod");
        check(hasMethod(c, "sprintf0d"), "method sprintf0d");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
