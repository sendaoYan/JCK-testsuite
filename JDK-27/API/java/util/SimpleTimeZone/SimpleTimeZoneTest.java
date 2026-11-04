/*
 * @test
 * @summary java.util.SimpleTimeZone conformance: white-box structure plus black-box behavior
 *          of raw offset, DST start/end rules, getOffset for fixed standard/DST instants,
 *          getDSTSavings, clone/equals, and hasSameRules.
 * @run main SimpleTimeZoneTest
 */

import java.util.Calendar;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

public class SimpleTimeZoneTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.SimpleTimeZone");
        check(!c.isInterface(), "SimpleTimeZone is a class");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "SimpleTimeZone is concrete");
        check(c.getSuperclass() == TimeZone.class, "SimpleTimeZone extends TimeZone");
        c.getMethod("getRawOffset");
        c.getMethod("setRawOffset", int.class);
        c.getMethod("setDSTSavings", int.class);
        c.getMethod("getDSTSavings");
        c.getMethod("useDaylightTime");
        c.getMethod("setStartRule", int.class, int.class, int.class, int.class);
        c.getMethod("setEndRule", int.class, int.class, int.class, int.class);
        passed++;
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        testRawOffset();
        testDstRules();
        testCloneEquals();
        testConstructorAndRuleOverloads();
    }

    static void testConstructorAndRuleOverloads() {
        SimpleTimeZone tz = new SimpleTimeZone(
            0, "GMT", Calendar.MARCH, 1, Calendar.SUNDAY, 3600000,
            Calendar.OCTOBER, 1, Calendar.SUNDAY, 3600000);
        check(tz.useDaylightTime(), "DST-rule constructor useDaylightTime");

        SimpleTimeZone tz2 = new SimpleTimeZone(
            0, "GMT", Calendar.MARCH, 1, Calendar.SUNDAY, 3600000,
            Calendar.OCTOBER, 1, Calendar.SUNDAY, 3600000, 3600000);
        check(tz2.getDSTSavings() == 3600000, "DST-rule constructor with savings");

        SimpleTimeZone tz3 = new SimpleTimeZone(
            0, "GMT", Calendar.MARCH, 1, Calendar.SUNDAY, 3600000, SimpleTimeZone.WALL_TIME,
            Calendar.OCTOBER, 1, Calendar.SUNDAY, 3600000, SimpleTimeZone.WALL_TIME, 3600000);
        check(tz3.useDaylightTime(), "DST-rule constructor with time modes");

        SimpleTimeZone tz4 = new SimpleTimeZone(0, "GMT");
        tz4.setStartYear(2020);
        tz4.setStartRule(Calendar.MARCH, 1, 3600000);
        tz4.setEndRule(Calendar.OCTOBER, 1, 3600000);
        check(tz4.useDaylightTime(), "setStartRule(month,day,time)");

        SimpleTimeZone tz5 = new SimpleTimeZone(0, "GMT");
        tz5.setStartRule(Calendar.MARCH, 1, Calendar.SUNDAY, 3600000, true);
        tz5.setEndRule(Calendar.OCTOBER, -1, Calendar.SUNDAY, 3600000, true);
        check(tz5.useDaylightTime(), "setStartRule(...,after)/setEndRule(last)");

        SimpleTimeZone dst = new SimpleTimeZone(0, "GMT");
        dst.setStartRule(Calendar.MARCH, 1, Calendar.SUNDAY, 3600000);
        dst.setEndRule(Calendar.OCTOBER, 1, Calendar.SUNDAY, 3600000);
        dst.setDSTSavings(3600000);
        // fixed instants: July 2020 DST, Jan 2020 standard.
        java.util.Calendar cal = java.util.Calendar.getInstance(
            java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.US);
        cal.clear(); cal.set(2020, Calendar.JULY, 1, 0, 0, 0);
        check(dst.inDaylightTime(new java.util.Date(cal.getTimeInMillis())),
              "inDaylightTime true during DST");
        cal.clear(); cal.set(2020, Calendar.JANUARY, 1, 0, 0, 0);
        check(!dst.inDaylightTime(new java.util.Date(cal.getTimeInMillis())),
              "inDaylightTime false during standard");
    }

    static void testRawOffset() {
        SimpleTimeZone tz = new SimpleTimeZone(3600000, "GMT+01:00");
        check(tz.getRawOffset() == 3600000, "getRawOffset");
        check("GMT+01:00".equals(tz.getID()), "getID");
        check(!tz.useDaylightTime(), "useDaylightTime false without DST rules");

        tz.setRawOffset(7200000);
        check(tz.getRawOffset() == 7200000, "setRawOffset");
    }

    static void testDstRules() {
        SimpleTimeZone dst = new SimpleTimeZone(0, "GMT");
        dst.setStartRule(Calendar.MARCH, 1, Calendar.SUNDAY, 3600000);
        dst.setEndRule(Calendar.OCTOBER, 1, Calendar.SUNDAY, 3600000);
        dst.setDSTSavings(3600000);
        check(dst.useDaylightTime(), "useDaylightTime true after rules");
        check(dst.getDSTSavings() == 3600000, "getDSTSavings == 1 hour");

        // July 1 2020 is a Wednesday; northern-hemisphere DST is in effect -> raw + dstSavings.
        int julyOffset = dst.getOffset(1, 2020, Calendar.JULY, 1, Calendar.WEDNESDAY, 0);
        check(julyOffset == 3600000, "getOffset during DST == raw + dstSavings");

        // January 1 2020 is also a Wednesday; standard time -> raw offset only.
        int janOffset = dst.getOffset(1, 2020, Calendar.JANUARY, 1, Calendar.WEDNESDAY, 0);
        check(janOffset == 0, "getOffset during standard time == raw offset");
    }

    static void testCloneEquals() {
        SimpleTimeZone a = new SimpleTimeZone(3600000, "GMT+01:00");
        SimpleTimeZone b = (SimpleTimeZone) a.clone();
        check(b != a, "clone returns distinct object");
        check(b.equals(a), "clone equals original");
        check(b.hasSameRules(a), "clone hasSameRules original");
        check(a.hashCode() == b.hashCode(), "equal zones equal hashCode");
    }


    // ===================== 契约深化：规则等价性/US 规则 DST/非法参数矩阵 =====================
    static void testSimpleTimeZoneContracts() {
        SimpleTimeZone custom = new SimpleTimeZone(3600000, "X");
        check(custom.getRawOffset() == 3600000 && !custom.useDaylightTime(),
              "raw offset, no DST by default");
        check(custom.hasSameRules(new SimpleTimeZone(3600000, "Y")),
              "hasSameRules ignores zone id");
        check(!custom.hasSameRules(new SimpleTimeZone(7200000, "Y")),
              "hasSameRules detects offset difference");
        check(custom.equals(new SimpleTimeZone(3600000, "X"))
              && !custom.equals(new SimpleTimeZone(3600000, "W")),
              "equals includes zone id");
        check(custom.clone() instanceof SimpleTimeZone, "clone returns SimpleTimeZone");
        check(custom.getDSTSavings() == 0, "no-DST zone savings 0");
        SimpleTimeZone stz = new SimpleTimeZone(-5 * 3600000, "EST5",
              java.util.Calendar.MARCH, 8, -java.util.Calendar.SUNDAY, 2 * 3600000,
              java.util.Calendar.NOVEMBER, 1, -java.util.Calendar.SUNDAY, 2 * 3600000);
        @SuppressWarnings("deprecation")
        java.util.Date jul = new java.util.Date(2023 - 1900, java.util.Calendar.JULY, 1);
        @SuppressWarnings("deprecation")
        java.util.Date jan = new java.util.Date(2023 - 1900, java.util.Calendar.JANUARY, 1);
        check(stz.inDaylightTime(jul) && !stz.inDaylightTime(jan),
              "US-style rules: DST on in July, off in January");
        check(stz.useDaylightTime() && stz.getDSTSavings() == 3600000,
              "useDaylightTime + default savings 1h");
        try {
            new SimpleTimeZone(0, "Z", java.util.Calendar.MARCH, 8, -java.util.Calendar.SUNDAY,
                  2 * 3600000, java.util.Calendar.NOVEMBER, 1, -java.util.Calendar.SUNDAY,
                  2 * 3600000, 0);
            check(false, "zero dstSavings IAE");
        } catch (IllegalArgumentException iae) { check(true, "zero dstSavings IAE"); }
        try {
            SimpleTimeZone bad = new SimpleTimeZone(0, "Z");
            bad.setStartRule(13, 8, -1, 0);
            check(false, "setStartRule bad month IAE");
        } catch (IllegalArgumentException iae) { check(true, "setStartRule bad month IAE"); }
        SimpleTimeZone sy = new SimpleTimeZone(0, "Z");
        sy.setStartYear(1990);
        check(!sy.useDaylightTime(), "setStartYear alone does not enable DST");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testSimpleTimeZoneContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
