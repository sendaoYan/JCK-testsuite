/*
 * @test
 * @summary sun.util.calendar.ZoneInfoFile conformance: internal package reflection.
 * @modules java.base/sun.util.calendar:+open
 * @run main ZoneInfoFileTest
 */
public class ZoneInfoFileTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.util.calendar.ZoneInfoFile", false, ZoneInfoFileTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "getAliasMap"), "method getAliasMap");
        check(hasMethod(c, "getCustomTimeZone"), "method getCustomTimeZone");
        check(hasMethod(c, "getVersion"), "method getVersion");
        check(hasMethod(c, "getZoneIds"), "method getZoneIds");
        check(hasMethod(c, "getZoneInfo"), "method getZoneInfo");
        check(hasMethod(c, "readOffset"), "method readOffset");
        check(hasMethod(c, "toCustomID"), "method toCustomID");
        check(hasMethod(c, "zoneIds"), "method zoneIds");
        check(hasField(c, "CURRT"), "field CURRT");
        check(hasField(c, "DAYS_0000_TO_1970"), "field DAYS_0000_TO_1970");
        check(hasField(c, "DAYS_PER_CYCLE"), "field DAYS_PER_CYCLE");
        check(hasField(c, "DST_MASK"), "field DST_MASK");
        check(hasField(c, "DST_NSHIFT"), "field DST_NSHIFT");
        check(hasField(c, "LASTYEAR"), "field LASTYEAR");
        check(hasField(c, "LDT2100"), "field LDT2100");
        check(hasField(c, "OFFSET_MASK"), "field OFFSET_MASK");
        check(hasField(c, "SECONDS_PER_DAY"), "field SECONDS_PER_DAY");
        check(hasField(c, "TRANSITION_NSHIFT"), "field TRANSITION_NSHIFT");
        check(hasField(c, "UTC1900"), "field UTC1900");
        check(hasField(c, "UTC2100"), "field UTC2100");
        check(hasField(c, "aliases"), "field aliases");
        check(hasField(c, "indices"), "field indices");
        check(hasField(c, "regions"), "field regions");
        check(hasField(c, "ruleArray"), "field ruleArray");
        check(hasField(c, "toCalendarDOW"), "field toCalendarDOW");
        check(hasField(c, "toSTZTime"), "field toSTZTime");
        check(hasField(c, "versionId"), "field versionId");
        check(hasField(c, "zones"), "field zones");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
