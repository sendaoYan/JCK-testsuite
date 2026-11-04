/*
 * @test
 * @summary sun.util.resources.LocaleData conformance: internal package reflection.
 * @modules java.base/sun.util.resources:+open
 * @run main LocaleDataTest
 */
public class LocaleDataTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.util.resources.LocaleData", false, LocaleDataTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "getBreakIteratorInfo"), "method getBreakIteratorInfo");
        check(hasMethod(c, "getBreakIteratorResources"), "method getBreakIteratorResources");
        check(hasMethod(c, "getBundle"), "method getBundle");
        check(hasMethod(c, "getCalendarData"), "method getCalendarData");
        check(hasMethod(c, "getCollationData"), "method getCollationData");
        check(hasMethod(c, "getCurrencyNames"), "method getCurrencyNames");
        check(hasMethod(c, "getDateFormatData"), "method getDateFormatData");
        check(hasMethod(c, "getLocaleNames"), "method getLocaleNames");
        check(hasMethod(c, "getNumberFormatData"), "method getNumberFormatData");
        check(hasMethod(c, "getTimeZoneNames"), "method getTimeZoneNames");
        check(hasField(c, "CANDIDATES_MAP"), "field CANDIDATES_MAP");
        check(hasField(c, "DOTCLDR"), "field DOTCLDR");
        check(hasField(c, "defaultControl"), "field defaultControl");
        check(hasField(c, "type"), "field type");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
