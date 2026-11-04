/*
 * @test
 * @summary java.util.Calendar.Builder conformance: set/build date and time.
 * @run main CalendarBuilderTest
 */

import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public class CalendarBuilderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testBuilder() {
        Calendar cal = new Calendar.Builder()
            .set(Calendar.YEAR, 2020)
            .set(Calendar.MONTH, Calendar.JANUARY)
            .set(Calendar.DAY_OF_MONTH, 15)
            .build();
        check(cal.get(Calendar.YEAR) == 2020, "year");
        check(cal.get(Calendar.MONTH) == Calendar.JANUARY, "month");
        check(cal.get(Calendar.DAY_OF_MONTH) == 15, "day");

        Calendar c2 = new Calendar.Builder().setDate(2021, Calendar.FEBRUARY, 20).build();
        check(c2.get(Calendar.YEAR) == 2021 && c2.get(Calendar.DAY_OF_MONTH) == 20, "setDate");

        Calendar c3 = new Calendar.Builder().setTimeOfDay(10, 30, 45).build();
        check(c3.get(Calendar.HOUR_OF_DAY) == 10 && c3.get(Calendar.MINUTE) == 30
              && c3.get(Calendar.SECOND) == 45, "setTimeOfDay");

        TimeZone tz = TimeZone.getTimeZone("GMT");
        Calendar c4 = new Calendar.Builder().setTimeZone(tz).setLocale(Locale.US)
            .setLenient(false).build();
        check(c4.getTimeZone().equals(tz), "setTimeZone");
        check(!c4.isLenient(), "setLenient false");
    }


    // ===================== 契约深化：lenient 归一化/严格模式/iso8601/weekDate/复用 =====================
    static void testBuilderContracts() {
        Calendar full = new Calendar.Builder().setDate(2020, 9, 31).setTimeOfDay(12, 30, 15, 500).build();
        check(full.get(Calendar.YEAR) == 2020 && full.get(Calendar.MONTH) == 9
              && full.get(Calendar.DAY_OF_MONTH) == 31
              && full.get(Calendar.HOUR_OF_DAY) == 12 && full.get(Calendar.MINUTE) == 30
              && full.get(Calendar.SECOND) == 15 && full.get(Calendar.MILLISECOND) == 500,
              "setDate+setTimeOfDay full matrix");
        check(new Calendar.Builder().setInstant(0L).build().toInstant().toEpochMilli() == 0,
              "setInstant(long) epoch");
        check(new Calendar.Builder().setInstant(new java.util.Date(1000L)).build().getTimeInMillis() == 1000,
              "setInstant(Date)");
        Calendar norm = new Calendar.Builder().setDate(2020, 13, 1).build();
        check(norm.get(Calendar.YEAR) == 2021 && norm.get(Calendar.MONTH) == 1,
              "lenient month 13 normalizes to Feb 2021");
        try {
            new Calendar.Builder().setDate(2020, 13, 1).setLenient(false).build();
            check(false, "strict out-of-range month IAE");
        } catch (IllegalArgumentException iae) { check(true, "strict out-of-range month IAE"); }
        try {
            new Calendar.Builder().setCalendarType("bogus").build();
            check(false, "unknown calendar type IAE");
        } catch (IllegalArgumentException iae) { check(true, "unknown calendar type IAE"); }
        Calendar iso = new Calendar.Builder().setCalendarType("iso8601").setDate(2020, 0, 1).build();
        check("gregory".equals(iso.getCalendarType()) && iso.getFirstDayOfWeek() == 2
              && iso.getMinimalDaysInFirstWeek() == 4, "iso8601 week definition Mon/4");
        Calendar wk = new Calendar.Builder().setWeekDate(2020, 5, Calendar.MONDAY).build();
        check(wk.get(Calendar.WEEK_OF_YEAR) == 5 && wk.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY,
              "setWeekDate");
        Calendar fr = new Calendar.Builder().setLocale(Locale.FRANCE).setDate(2020, 0, 1).build();
        check(fr.getFirstDayOfWeek() == 2, "setLocale(FRANCE) week params");
        Calendar tzB = new Calendar.Builder().setTimeZone(TimeZone.getTimeZone("GMT"))
              .setDate(2020, 0, 1).build();
        check("GMT".equals(tzB.getTimeZone().getID()), "setTimeZone applied");
        Calendar fs = new Calendar.Builder().setFields(Calendar.YEAR, 2020, Calendar.MONTH, 5).build();
        check(fs.get(Calendar.YEAR) == 2020 && fs.get(Calendar.MONTH) == 5, "setFields varargs");
        Calendar.Builder reuse = new Calendar.Builder().setDate(2020, 0, 1);
        Calendar b1 = reuse.build();
        Calendar b2 = reuse.build();
        check(b1 != b2 && b1.equals(b2), "build() reusable yielding distinct equal instances");
        Calendar wdf = new Calendar.Builder().setWeekDefinition(Calendar.MONDAY, 4)
              .setDate(2020, 0, 1).build();
        check(wdf.getFirstDayOfWeek() == Calendar.MONDAY
              && wdf.getMinimalDaysInFirstWeek() == 4, "setWeekDefinition");
    }

    public static void main(String[] args) {
        testBuilder();
        testBuilderContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
