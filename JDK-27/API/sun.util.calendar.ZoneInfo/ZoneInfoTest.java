/*
 * @test
 * @summary sun.util.calendar.ZoneInfo conformance: internal package reflection.
 * @modules java.base/sun.util.calendar:+open
 * @run main ZoneInfoTest
 */
public class ZoneInfoTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.util.calendar.ZoneInfo", false, ZoneInfoTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.util.TimeZone"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "availableIDs"), "method availableIDs");
        check(hasMethod(c, "clone"), "method clone");
        check(hasMethod(c, "equals"), "method equals");
        check(hasMethod(c, "getAliasTable"), "method getAliasTable");
        check(hasMethod(c, "getAvailableIDs"), "method getAvailableIDs");
        check(hasMethod(c, "getDSTSavings"), "method getDSTSavings");
        check(hasMethod(c, "getLastRuleInstance"), "method getLastRuleInstance");
        check(hasMethod(c, "getOffset"), "method getOffset");
        check(hasMethod(c, "getOffsets"), "method getOffsets");
        check(hasMethod(c, "getOffsetsByStandard"), "method getOffsetsByStandard");
        check(hasMethod(c, "getOffsetsByWall"), "method getOffsetsByWall");
        check(hasMethod(c, "getRawOffset"), "method getRawOffset");
        check(hasMethod(c, "getTimeZone"), "method getTimeZone");
        check(hasMethod(c, "hasSameRules"), "method hasSameRules");
        check(hasMethod(c, "hashCode"), "method hashCode");
        check(hasMethod(c, "inDaylightTime"), "method inDaylightTime");
        check(hasMethod(c, "isDirty"), "method isDirty");
        check(hasMethod(c, "observesDaylightTime"), "method observesDaylightTime");
        check(hasMethod(c, "setRawOffset"), "method setRawOffset");
        check(hasMethod(c, "toString"), "method toString");
        check(hasMethod(c, "useDaylightTime"), "method useDaylightTime");
        check(hasField(c, "DST_MASK"), "field DST_MASK");
        check(hasField(c, "DST_NSHIFT"), "field DST_NSHIFT");
        check(hasField(c, "OFFSET_MASK"), "field OFFSET_MASK");
        check(hasField(c, "STANDARD_TIME"), "field STANDARD_TIME");
        check(hasField(c, "TRANSITION_NSHIFT"), "field TRANSITION_NSHIFT");
        check(hasField(c, "UTC_TIME"), "field UTC_TIME");
        check(hasField(c, "WALL_TIME"), "field WALL_TIME");
        check(hasField(c, "checksum"), "field checksum");
        check(hasField(c, "dirty"), "field dirty");
        check(hasField(c, "dstSavings"), "field dstSavings");
        check(hasField(c, "lastRule"), "field lastRule");
        check(hasField(c, "offsets"), "field offsets");
        check(hasField(c, "rawOffset"), "field rawOffset");
        check(hasField(c, "rawOffsetDiff"), "field rawOffsetDiff");
        check(hasField(c, "serialVersionUID"), "field serialVersionUID");
        check(hasField(c, "simpleTimeZoneParams"), "field simpleTimeZoneParams");
        check(hasField(c, "transitions"), "field transitions");
        check(hasField(c, "willGMTOffsetChange"), "field willGMTOffsetChange");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
