/*
 * @test
 * @summary sun.util.locale.provider.TimeZoneNameUtility conformance: internal package reflection.
 * @modules java.base/sun.util.locale.provider:+open
 * @run main TimeZoneNameUtilityTest
 */
public class TimeZoneNameUtilityTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.util.locale.provider.TimeZoneNameUtility", false, TimeZoneNameUtilityTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "canonicalTZID"), "method canonicalTZID");
        check(hasMethod(c, "convertLDMLShortID"), "method convertLDMLShortID");
        check(hasMethod(c, "explicitDstOffset"), "method explicitDstOffset");
        check(hasMethod(c, "getZoneStrings"), "method getZoneStrings");
        check(hasMethod(c, "retrieveDisplayName"), "method retrieveDisplayName");
        check(hasMethod(c, "retrieveDisplayNames"), "method retrieveDisplayNames");
        check(hasMethod(c, "retrieveGenericDisplayName"), "method retrieveGenericDisplayName");
        check(hasField(c, "cachedDisplayNames"), "field cachedDisplayNames");
        check(hasField(c, "cachedZoneData"), "field cachedZoneData");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
