/*
 * @test
 * @summary java.util.SimpleTimeZone API conformance (Java 8 surface): complete contract
 *          coverage of constructors (fixed offset + full DST rule + start/end time mode),
 *          getRawOffset/setRawOffset, setDSTSavings/getDSTSavings, setStartYear, setStartRule
 *          /setEndRule (all overloads), useDaylightTime/observesDaylightTime/inDaylightTime,
 *          getOffset(long)/getOffset(6 ints), clone/equals/hashCode/hasSameRules/toString.
 * @run main SimpleTimeZoneTest
 */

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

public class SimpleTimeZoneTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void expectThrows(Class<? extends Throwable> type, Runnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (type.isInstance(t)) { passed++; return; }
            throw new AssertionError(msg + ": expected " + type.getSimpleName()
                + " but got " + t.getClass().getSimpleName(), t);
        }
        throw new AssertionError(msg + ": expected " + type.getSimpleName() + " but nothing thrown");
    }

    static long utcMillis(int year, int month, int day) {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.US);
        cal.clear();
        cal.set(year, month, day, 0, 0, 0);
        return cal.getTimeInMillis();
    }

    static SimpleTimeZone northernDst() {
        SimpleTimeZone dst = new SimpleTimeZone(0, "GMT");
        dst.setStartRule(Calendar.MARCH, 1, Calendar.SUNDAY, 3600000);
        dst.setEndRule(Calendar.OCTOBER, 1, Calendar.SUNDAY, 3600000);
        dst.setDSTSavings(3600000);
        return dst;
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
        SimpleTimeZone dst = northernDst();
        check(dst.useDaylightTime(), "useDaylightTime true after rules");
        check(dst.observesDaylightTime(), "observesDaylightTime true");
        check(dst.getDSTSavings() == 3600000, "getDSTSavings == 1 hour");

        // getOffset(6 ints): July 1 2020 (Wed) is DST; Jan 1 2020 (Wed) is standard.
        int july = dst.getOffset(1, 2020, Calendar.JULY, 1, Calendar.WEDNESDAY, 0);
        check(july == 3600000, "getOffset(6 ints) DST == raw + dstSavings");
        int jan = dst.getOffset(1, 2020, Calendar.JANUARY, 1, Calendar.WEDNESDAY, 0);
        check(jan == 0, "getOffset(6 ints) standard == raw offset");

        // getOffset(long) at fixed instants.
        check(dst.getOffset(utcMillis(2020, Calendar.JULY, 1)) == 3600000, "getOffset(long) DST");
        check(dst.getOffset(utcMillis(2020, Calendar.JANUARY, 1)) == 0, "getOffset(long) standard");

        // inDaylightTime.
        check(dst.inDaylightTime(new Date(utcMillis(2020, Calendar.JULY, 1))),
              "inDaylightTime true during DST");
        check(!dst.inDaylightTime(new Date(utcMillis(2020, Calendar.JANUARY, 1))),
              "inDaylightTime false during standard");
    }

    static void testConstructorsWithRules() {
        // full DST-rule constructor (start/end day-of-month based).
        SimpleTimeZone tz = new SimpleTimeZone(
            0, "GMT", Calendar.MARCH, 1, Calendar.SUNDAY, 3600000,
            Calendar.OCTOBER, 1, Calendar.SUNDAY, 3600000);
        check(tz.useDaylightTime(), "DST-rule constructor useDaylightTime");

        // constructor with explicit DST savings.
        SimpleTimeZone tz2 = new SimpleTimeZone(
            0, "GMT", Calendar.MARCH, 1, Calendar.SUNDAY, 3600000,
            Calendar.OCTOBER, 1, Calendar.SUNDAY, 3600000, 3600000);
        check(tz2.getDSTSavings() == 3600000, "DST-rule constructor with savings");

        // constructor with start time mode + end time mode.
        SimpleTimeZone tz3 = new SimpleTimeZone(
            0, "GMT", Calendar.MARCH, 1, Calendar.SUNDAY, 3600000,
            SimpleTimeZone.WALL_TIME,
            Calendar.OCTOBER, 1, Calendar.SUNDAY, 3600000,
            SimpleTimeZone.WALL_TIME, 3600000);
        check(tz3.useDaylightTime(), "DST-rule constructor with time modes");
    }

    static void testStartEndRuleOverloads() {
        SimpleTimeZone tz = new SimpleTimeZone(0, "GMT");
        tz.setStartYear(2020);
        tz.setStartRule(Calendar.MARCH, 1, Calendar.SUNDAY, 3600000);
        tz.setEndRule(Calendar.OCTOBER, 1, Calendar.SUNDAY, 3600000);
        check(tz.useDaylightTime(), "setStartRule(month,day,dayOfWeek,time)");

        SimpleTimeZone tz2 = new SimpleTimeZone(0, "GMT");
        tz2.setStartRule(Calendar.MARCH, 1, 3600000); // day-of-month form
        tz2.setEndRule(Calendar.OCTOBER, 1, 3600000);
        check(tz2.useDaylightTime(), "setStartRule(month,day,time)");

        SimpleTimeZone tz3 = new SimpleTimeZone(0, "GMT");
        tz3.setStartRule(Calendar.MARCH, 1, Calendar.SUNDAY, 3600000, true); // after
        tz3.setEndRule(Calendar.OCTOBER, 1, Calendar.SUNDAY, 3600000, true);
        check(tz3.useDaylightTime(), "setStartRule(...,after)");

        SimpleTimeZone tz4 = new SimpleTimeZone(0, "GMT");
        tz4.setStartRule(Calendar.MARCH, 1, Calendar.SUNDAY, 3600000);
        tz4.setEndRule(Calendar.OCTOBER, -1, Calendar.SUNDAY, 3600000); // last Sunday (dayOfMonth <= 0)
        check(tz4.useDaylightTime(), "setEndRule negative day (last)");
    }

    static void testCloneEqualsHashCode() {
        SimpleTimeZone a = northernDst();
        SimpleTimeZone b = (SimpleTimeZone) a.clone();
        check(b != a, "clone distinct object");
        check(b.equals(a), "clone equals original");
        check(b.hasSameRules(a), "clone hasSameRules original");
        check(a.hashCode() == b.hashCode(), "equal zones equal hashCode");

        SimpleTimeZone c = new SimpleTimeZone(0, "GMT");
        check(!a.equals(c), "different rules not equal");
        check(!a.equals(null) && !a.equals("x"), "equals non-null/non-STZ false");

        check(a.toString() != null && a.toString().length() > 0, "toString non-empty");
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

    public static void main(String[] args) {
        testRawOffset();
        testDstRules();
        testConstructorsWithRules();
        testStartEndRuleOverloads();
        testCloneEqualsHashCode();
        testSimpleTimeZoneContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
