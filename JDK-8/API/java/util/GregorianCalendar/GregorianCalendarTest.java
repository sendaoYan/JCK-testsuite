/*
 * @test
 * @summary java.util.GregorianCalendar API conformance (Java 8 surface): complete contract
 *          coverage of all constructors, isLeapYear (century rules), epoch fields in UTC,
 *          getCalendarType, add/roll, getActualMaximum (leap years), setGregorianChange
 *          (null NPE + round-trip), min/max/actual-min/actual-max, equals/hashCode, clone
 *          independence, week-date (isWeekDateSupported/getWeekYear/setWeekDate/getWeeksInWeekYear).
 * @run main GregorianCalendarTest
 */

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

public class GregorianCalendarTest {
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

    static GregorianCalendar utc() {
        return new GregorianCalendar(TimeZone.getTimeZone("UTC"), Locale.US);
    }

    static void testLeapYear() {
        GregorianCalendar gc = new GregorianCalendar();
        check(gc.isLeapYear(2000), "2000 is leap");
        check(gc.isLeapYear(2004), "2004 is leap");
        check(!gc.isLeapYear(1900), "1900 is not leap (century)");
        check(!gc.isLeapYear(2023), "2023 is not leap");
        check(gc.isLeapYear(1600), "1600 is leap (century divisible by 400)");
    }

    static void testConstructors() {
        GregorianCalendar g = new GregorianCalendar(2024, Calendar.MARCH, 1);
        check(g.get(Calendar.YEAR) == 2024 && g.get(Calendar.MONTH) == Calendar.MARCH
              && g.get(Calendar.DAY_OF_MONTH) == 1, "(y,m,d) fields");

        GregorianCalendar g2 = new GregorianCalendar(2024, Calendar.MARCH, 1, 13, 45);
        check(g2.get(Calendar.HOUR_OF_DAY) == 13 && g2.get(Calendar.MINUTE) == 45,
              "(y,m,d,h,m) time fields");

        GregorianCalendar g3 = new GregorianCalendar(2024, Calendar.MARCH, 1, 13, 45, 30);
        check(g3.get(Calendar.SECOND) == 30, "(y,m,d,h,m,s) seconds");

        check(new GregorianCalendar().getTimeZone() != null, "no-arg has timezone");
        check(new GregorianCalendar(TimeZone.getTimeZone("UTC")).getTimeZone().getID().equals("UTC"),
              "(TimeZone) constructor");
        check(new GregorianCalendar(Locale.US).getClass() == GregorianCalendar.class,
              "(Locale) constructor");
        check(utc().getTimeZone().getID().equals("UTC") , "(TimeZone,Locale) constructor");
    }

    static void testEpoch() {
        GregorianCalendar g = utc();
        g.setTimeInMillis(0L);
        check(g.get(Calendar.YEAR) == 1970 && g.get(Calendar.MONTH) == Calendar.JANUARY
              && g.get(Calendar.DAY_OF_MONTH) == 1, "epoch fields in UTC");
        check(g.getTimeInMillis() == 0L, "epoch time-in-millis");
    }

    static void testCalendarType() {
        check("gregory".equals(utc().getCalendarType()), "getCalendarType == gregory");
    }

    static void testAddRoll() {
        GregorianCalendar g = utc();
        g.clear();
        g.set(2020, Calendar.DECEMBER, 15);
        g.add(Calendar.MONTH, 1);
        check(g.get(Calendar.YEAR) == 2021 && g.get(Calendar.MONTH) == Calendar.JANUARY,
              "add MONTH crosses year");

        GregorianCalendar r = utc();
        r.clear();
        r.set(2020, Calendar.DECEMBER, 15);
        r.roll(Calendar.MONTH, true);
        check(r.get(Calendar.YEAR) == 2020 && r.get(Calendar.MONTH) == Calendar.JANUARY,
              "roll MONTH stays same year");
    }

    static void testActualMaximum() {
        GregorianCalendar g = utc();
        g.clear();
        g.set(2000, Calendar.FEBRUARY, 1);
        check(g.getActualMaximum(Calendar.DAY_OF_MONTH) == 29, "Feb 2000 actual max 29");
        g.set(2001, Calendar.FEBRUARY, 1);
        check(g.getActualMaximum(Calendar.DAY_OF_MONTH) == 28, "Feb 2001 actual max 28");
        g.set(2021, Calendar.APRIL, 1);
        check(g.getActualMaximum(Calendar.DAY_OF_MONTH) == 30, "April actual max 30");
    }

    static void testGregorianChange() {
        GregorianCalendar g = utc();
        Date cutover = g.getGregorianChange();
        check(cutover != null, "default gregorian change non-null");
        Date newCut = new Date(1000000000000L);
        g.setGregorianChange(newCut);
        check(g.getGregorianChange().equals(newCut), "setGregorianChange/getGregorianChange roundtrip");
        expectThrows(NullPointerException.class, () -> g.setGregorianChange(null),
            "setGregorianChange(null) NPE");
    }

    static void testMinMax() {
        GregorianCalendar g = utc();
        check(g.getMinimum(Calendar.MONTH) == Calendar.JANUARY, "getMinimum(MONTH)");
        check(g.getMaximum(Calendar.MONTH) == Calendar.DECEMBER, "getMaximum(MONTH)");
        check(g.getGreatestMinimum(Calendar.DAY_OF_MONTH) >= 1, "getGreatestMinimum(DAY_OF_MONTH)");
        check(g.getLeastMaximum(Calendar.DAY_OF_MONTH) <= 31, "getLeastMaximum(DAY_OF_MONTH)");
        check(g.getActualMinimum(Calendar.MONTH) == Calendar.JANUARY, "getActualMinimum(MONTH)");
        check(g.getActualMaximum(Calendar.MONTH) == Calendar.DECEMBER, "getActualMaximum(MONTH)");
    }

    static void testEqualsClone() {
        GregorianCalendar a = utc();
        a.clear();
        a.set(2020, Calendar.JULY, 4);
        GregorianCalendar b = utc();
        b.clear();
        b.set(2020, Calendar.JULY, 4);
        check(a.equals(b) && a.hashCode() == b.hashCode(), "equals/hashCode");
        check(!a.equals(null) && !a.equals("x"), "equals non-null/non-GC false");

        GregorianCalendar clone = (GregorianCalendar) a.clone();
        check(clone != a && clone.equals(a), "clone distinct + equal");
        clone.add(Calendar.DAY_OF_MONTH, 1);
        check(!clone.equals(a) && a.get(Calendar.DAY_OF_MONTH) == 4, "clone mutation independent");
    }

    static void testWeekDate() {
        GregorianCalendar g = utc();
        check(g.isWeekDateSupported(), "isWeekDateSupported true");
        g.setFirstDayOfWeek(Calendar.MONDAY);
        g.setMinimalDaysInFirstWeek(4);
        g.setWeekDate(2020, 10, Calendar.MONDAY);
        check(g.getWeekYear() == 2020, "getWeekYear after setWeekDate");
        check(g.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY, "setWeekDate day of week");
        check(g.getWeeksInWeekYear() >= 52 && g.getWeeksInWeekYear() <= 53,
              "getWeeksInWeekYear 52..53");
    }


    // ===================== 契约深化：闰年矩阵/纪元/1582 换历/纯格里历归一化 =====================
    static void testGregorianContracts() {
        java.util.TimeZone gmt = java.util.TimeZone.getTimeZone("GMT");
        GregorianCalendar gc = new GregorianCalendar(gmt, java.util.Locale.US);
        check(gc.isLeapYear(2000) && gc.isLeapYear(2024), "leap years 2000/2024");
        check(!gc.isLeapYear(1900) && !gc.isLeapYear(2023), "non-leap 1900/2023");
        check(GregorianCalendar.AD == 1 && GregorianCalendar.BC == 0, "AD==1 BC==0 constants");
        check(new GregorianCalendar(gmt, java.util.Locale.US).get(Calendar.ERA) == GregorianCalendar.AD,
              "default era is AD");
        GregorianCalendar bc = new GregorianCalendar(gmt, java.util.Locale.US);
        bc.clear(); bc.set(Calendar.ERA, GregorianCalendar.BC); bc.set(1, Calendar.JANUARY, 1);
        check(bc.get(Calendar.YEAR) == 1 && bc.get(Calendar.ERA) == GregorianCalendar.BC,
              "BC era set round-trip");
        check("1582-10-15T00:00:00Z".equals(
              new GregorianCalendar().getGregorianChange().toInstant().toString()),
              "default Gregorian change Oct 15 1582 UTC");
        GregorianCalendar proleptic = new GregorianCalendar(gmt, java.util.Locale.US);
        proleptic.setGregorianChange(new java.util.Date(Long.MIN_VALUE));
        proleptic.clear(); proleptic.set(1500, Calendar.FEBRUARY, 29);
        check(proleptic.get(Calendar.MONTH) == Calendar.MARCH
              && proleptic.get(Calendar.DAY_OF_MONTH) == 1,
              "change=MIN gives proleptic Gregorian: Feb 29 1500 -> Mar 1");
        GregorianCalendar z = new GregorianCalendar(gmt, java.util.Locale.US);
        check(z.toZonedDateTime().toInstant().toEpochMilli() == z.getTimeInMillis(),
              "toZonedDateTime preserves instant");
        GregorianCalendar julian = new GregorianCalendar(gmt, java.util.Locale.US);
        julian.setGregorianChange(new java.util.Date(Long.MAX_VALUE));
        julian.clear(); julian.set(1500, Calendar.FEBRUARY, 29);
        check(julian.get(Calendar.DAY_OF_MONTH) == 29,
              "change=MAX pure Julian keeps Feb 29 1500");
    }

    public static void main(String[] args) {
        testLeapYear();
        testConstructors();
        testEpoch();
        testCalendarType();
        testAddRoll();
        testActualMaximum();
        testGregorianChange();
        testMinMax();
        testEqualsClone();
        testWeekDate();
        testGregorianContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
