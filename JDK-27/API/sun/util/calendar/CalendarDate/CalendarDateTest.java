/*
 * @test
 * @summary sun.util.calendar.CalendarDate conformance: internal package reflection.
 * @modules java.base/sun.util.calendar:+open
 * @run main CalendarDateTest
 */
public class CalendarDateTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.util.calendar.CalendarDate", false, CalendarDateTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 1, "interface count");
        check(hasInterface(c, "java.lang.Cloneable"), "interface java.lang.Cloneable");
        check(hasMethod(c, "addHours"), "method addHours");
        check(hasMethod(c, "addMonth"), "method addMonth");
        check(hasMethod(c, "addYear"), "method addYear");
        check(hasMethod(c, "clone"), "method clone");
        check(hasMethod(c, "equals"), "method equals");
        check(hasMethod(c, "getDayOfMonth"), "method getDayOfMonth");
        check(hasMethod(c, "getDayOfWeek"), "method getDayOfWeek");
        check(hasMethod(c, "getDaylightSaving"), "method getDaylightSaving");
        check(hasMethod(c, "getEra"), "method getEra");
        check(hasMethod(c, "getHours"), "method getHours");
        check(hasMethod(c, "getMillis"), "method getMillis");
        check(hasMethod(c, "getMinutes"), "method getMinutes");
        check(hasMethod(c, "getMonth"), "method getMonth");
        check(hasMethod(c, "getSeconds"), "method getSeconds");
        check(hasMethod(c, "getTimeOfDay"), "method getTimeOfDay");
        check(hasMethod(c, "getYear"), "method getYear");
        check(hasMethod(c, "getZone"), "method getZone");
        check(hasMethod(c, "getZoneOffset"), "method getZoneOffset");
        check(hasMethod(c, "hashCode"), "method hashCode");
        check(hasMethod(c, "isDaylightTime"), "method isDaylightTime");
        check(hasMethod(c, "isLeapYear"), "method isLeapYear");
        check(hasMethod(c, "isNormalized"), "method isNormalized");
        check(hasMethod(c, "isSameDate"), "method isSameDate");
        check(hasMethod(c, "setDate"), "method setDate");
        check(hasMethod(c, "setDayOfMonth"), "method setDayOfMonth");
        check(hasMethod(c, "setDayOfWeek"), "method setDayOfWeek");
        check(hasMethod(c, "setDaylightSaving"), "method setDaylightSaving");
        check(hasMethod(c, "setEra"), "method setEra");
        check(hasMethod(c, "setHours"), "method setHours");
        check(hasMethod(c, "setMillis"), "method setMillis");
        check(hasMethod(c, "setMinutes"), "method setMinutes");
        check(hasMethod(c, "setMonth"), "method setMonth");
        check(hasMethod(c, "setNormalized"), "method setNormalized");
        check(hasMethod(c, "setSeconds"), "method setSeconds");
        check(hasMethod(c, "setTimeOfDay"), "method setTimeOfDay");
        check(hasMethod(c, "setYear"), "method setYear");
        check(hasMethod(c, "setZone"), "method setZone");
        check(hasMethod(c, "setZoneOffset"), "method setZoneOffset");
        check(hasMethod(c, "toString"), "method toString");
        check(hasField(c, "FIELD_UNDEFINED"), "field FIELD_UNDEFINED");
        check(hasField(c, "TIME_UNDEFINED"), "field TIME_UNDEFINED");
        check(hasField(c, "dayOfMonth"), "field dayOfMonth");
        check(hasField(c, "dayOfWeek"), "field dayOfWeek");
        check(hasField(c, "daylightSaving"), "field daylightSaving");
        check(hasField(c, "era"), "field era");
        check(hasField(c, "fraction"), "field fraction");
        check(hasField(c, "hours"), "field hours");
        check(hasField(c, "leapYear"), "field leapYear");
        check(hasField(c, "millis"), "field millis");
        check(hasField(c, "minutes"), "field minutes");
        check(hasField(c, "month"), "field month");
        check(hasField(c, "normalized"), "field normalized");
        check(hasField(c, "seconds"), "field seconds");
        check(hasField(c, "year"), "field year");
        check(hasField(c, "zoneOffset"), "field zoneOffset");
        check(hasField(c, "zoneinfo"), "field zoneinfo");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
