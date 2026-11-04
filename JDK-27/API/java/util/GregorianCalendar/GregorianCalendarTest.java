/*
 * @test
 * @summary java.util.GregorianCalendar conformance: white-box structure plus black-box behavior
 *          of isLeapYear, the (y,m,d) constructor, epoch fields in UTC, getCalendarType,
 *          add/roll, getActualMaximum, and ZonedDateTime round-trip.
 * @run main GregorianCalendarTest
 */

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

public class GregorianCalendarTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static GregorianCalendar utc() {
        return new GregorianCalendar(TimeZone.getTimeZone("UTC"), Locale.US);
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.GregorianCalendar");
        check(!c.isInterface(), "GregorianCalendar is a class");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "GregorianCalendar is concrete");
        check(c.getSuperclass() == Calendar.class, "GregorianCalendar extends Calendar");
        c.getMethod("isLeapYear", int.class);
        c.getMethod("getCalendarType");
        c.getMethod("add", int.class, int.class);
        c.getMethod("roll", int.class, int.class);
        c.getMethod("getActualMaximum", int.class);
        c.getMethod("toZonedDateTime");
        c.getMethod("from", ZonedDateTime.class);
        passed++;
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        testLeapYear();
        testConstructor();
        testEpoch();
        testCalendarType();
        testAddRoll();
        testActualMaximum();
        testZonedDateTimeRoundTrip();
        testGregorianChangeCloneWeekDate();
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

    static void testGregorianChangeCloneWeekDate() {
        GregorianCalendar g = utc();
        java.util.Date cut = new java.util.Date(1000000000000L);
        g.setGregorianChange(cut);
        check(g.getGregorianChange().equals(cut), "setGregorianChange roundtrip");
        expectThrows(NullPointerException.class, () -> g.setGregorianChange(null),
                "setGregorianChange(null) NPE");

        check(g.getMinimum(Calendar.MONTH) == Calendar.JANUARY, "getMinimum(MONTH)");
        check(g.getMaximum(Calendar.MONTH) == Calendar.DECEMBER, "getMaximum(MONTH)");
        check(g.getActualMinimum(Calendar.MONTH) == Calendar.JANUARY, "getActualMinimum(MONTH)");
        check(g.getActualMaximum(Calendar.MONTH) == Calendar.DECEMBER, "getActualMaximum(MONTH)");

        GregorianCalendar clone = (GregorianCalendar) g.clone();
        check(clone != g && clone.equals(g), "clone distinct + equal");
        clone.add(Calendar.DAY_OF_MONTH, 1);
        check(!clone.equals(g), "clone mutation independent");

        GregorianCalendar w = utc();
        check(w.isWeekDateSupported(), "isWeekDateSupported");
        w.setFirstDayOfWeek(Calendar.MONDAY);
        w.setMinimalDaysInFirstWeek(4);
        w.setWeekDate(2020, 10, Calendar.MONDAY);
        check(w.getWeekYear() == 2020, "getWeekYear after setWeekDate");
        check(w.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY, "setWeekDate day of week");
    }

    static void testLeapYear() {
        GregorianCalendar gc = new GregorianCalendar();
        check(gc.isLeapYear(2000), "2000 is leap");
        check(gc.isLeapYear(2004), "2004 is leap");
        check(!gc.isLeapYear(1900), "1900 is not leap (century)");
        check(!gc.isLeapYear(2023), "2023 is not leap");
    }

    static void testConstructor() {
        GregorianCalendar g = new GregorianCalendar(2024, Calendar.MARCH, 1);
        check(g.get(Calendar.YEAR) == 2024, "(y,m,d) YEAR");
        check(g.get(Calendar.MONTH) == Calendar.MARCH, "(y,m,d) MONTH");
        check(g.get(Calendar.DAY_OF_MONTH) == 1, "(y,m,d) DAY_OF_MONTH");

        GregorianCalendar g2 = new GregorianCalendar(2024, Calendar.MARCH, 1, 13, 45, 30);
        check(g2.get(Calendar.HOUR_OF_DAY) == 13 && g2.get(Calendar.MINUTE) == 45
              && g2.get(Calendar.SECOND) == 30, "(y,m,d,h,m,s) time fields");
    }

    static void testEpoch() {
        GregorianCalendar g = utc();
        g.setTimeInMillis(0L);
        check(g.get(Calendar.YEAR) == 1970 && g.get(Calendar.MONTH) == Calendar.JANUARY,
              "epoch fields in UTC");
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
    }

    static void testZonedDateTimeRoundTrip() {
        GregorianCalendar g = utc();
        g.setTimeInMillis(0L);
        ZonedDateTime zdt = g.toZonedDateTime();
        check(zdt.toInstant().getEpochSecond() == 0L, "toZonedDateTime preserves epoch instant");
        check(zdt.getZone().equals(ZoneId.of("UTC")), "toZonedDateTime preserves UTC zone");
        GregorianCalendar back = GregorianCalendar.from(zdt);
        check(back.getTimeInMillis() == 0L, "from(ZonedDateTime) round-trip");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
