/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.impl.dv.xs.AbstractDateTimeDV conformance: abstract class hierarchy and members.
 * @run main AbstractDateTimeDVTest
 */
public class AbstractDateTimeDVTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.impl.dv.xs.AbstractDateTimeDV", false, AbstractDateTimeDVTest.class.getClassLoader());
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass().getName().equals("com.sun.org.apache.xerces.internal.impl.dv.xs.TypeValidator"), "superclass com.sun.org.apache.xerces.internal.impl.dv.xs.TypeValidator");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getAllowedFacets"), "method getAllowedFacets");
        check(mn.contains("isIdentical"), "method isIdentical");
        check(mn.contains("compare"), "method compare");
        check(mn.contains("compareDates"), "method compareDates");
        check(mn.contains("compareOrder"), "method compareOrder");
        check(mn.contains("getTime"), "method getTime");
        check(mn.contains("getDate"), "method getDate");
        check(mn.contains("getYearMonth"), "method getYearMonth");
        check(mn.contains("parseTimeZone"), "method parseTimeZone");
        check(mn.contains("getTimeZone"), "method getTimeZone");
        check(mn.contains("indexOf"), "method indexOf");
        check(mn.contains("validateDateTime"), "method validateDateTime");
        check(mn.contains("findUTCSign"), "method findUTCSign");
        check(mn.contains("isNextCharUTCSign"), "method isNextCharUTCSign");
        check(mn.contains("parseInt"), "method parseInt");
        check(mn.contains("parseIntYear"), "method parseIntYear");
        check(mn.contains("normalize"), "method normalize");
        check(mn.contains("saveUnnormalized"), "method saveUnnormalized");
        check(mn.contains("resetDateObj"), "method resetDateObj");
        check(mn.contains("maxDayInMonthFor"), "method maxDayInMonthFor");
        check(mn.contains("mod"), "method mod");
        check(mn.contains("fQuotient"), "method fQuotient");
        check(mn.contains("modulo"), "method modulo");
        check(mn.contains("dateToString"), "method dateToString");
        check(mn.contains("append"), "method append");
        check(mn.contains("append2"), "method append2");
        check(mn.contains("parseSecond"), "method parseSecond");
        check(mn.contains("getXMLGregorianCalendar"), "method getXMLGregorianCalendar");
        check(mn.contains("getDuration"), "method getDuration");
        check(mn.contains("getFractionalSecondsAsBigDecimal"), "method getFractionalSecondsAsBigDecimal");
        check(mn.contains("access$000"), "method access$000");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
