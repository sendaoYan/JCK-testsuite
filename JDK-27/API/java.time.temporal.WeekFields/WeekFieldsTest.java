/*
 * @test
 * @summary java.time.temporal.WeekFields conformance: white-box structure + black-box behavior.
 * @run main WeekFieldsTest
 */

import java.io.Serializable;
import java.lang.reflect.Modifier;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalField;
import java.time.temporal.TemporalUnit;
import java.time.temporal.WeekFields;
import java.util.Locale;

public class WeekFieldsTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.time.temporal.WeekFields");
        check(Modifier.isFinal(c.getModifiers()), "is final");
        check(c.getConstructors().length == 0, "no public constructor");
        boolean serializable = false;
        for (Class<?> i : c.getInterfaces()) if (i == Serializable.class) serializable = true;
        check(serializable, "implements Serializable");
        check(WeekFields.ISO instanceof WeekFields, "ISO constant");
        check(WeekFields.SUNDAY_START instanceof WeekFields, "SUNDAY_START constant");
        check(WeekFields.WEEK_BASED_YEARS instanceof TemporalUnit, "WEEK_BASED_YEARS is TemporalUnit");
        check(WeekFields.ISO.dayOfWeek() instanceof TemporalField, "dayOfWeek() is TemporalField");
        check(WeekFields.ISO.weekOfMonth() instanceof TemporalField, "weekOfMonth() is TemporalField");
        check(WeekFields.ISO.weekOfYear() instanceof TemporalField, "weekOfYear() is TemporalField");
        check(WeekFields.ISO.weekOfWeekBasedYear() instanceof TemporalField, "weekOfWeekBasedYear() is TemporalField");
        check(WeekFields.ISO.weekBasedYear() instanceof TemporalField, "weekBasedYear() is TemporalField");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        // ISO configuration
        WeekFields iso = WeekFields.ISO;
        check(iso.getFirstDayOfWeek() == DayOfWeek.MONDAY, "ISO firstDayOfWeek MONDAY");
        check(iso.getMinimalDaysInFirstWeek() == 4, "ISO minimalDaysInFirstWeek 4");

        // SUNDAY_START configuration
        WeekFields sunday = WeekFields.of(DayOfWeek.SUNDAY, 1);
        check(sunday.getFirstDayOfWeek() == DayOfWeek.SUNDAY, "SUNDAY_START firstDayOfWeek SUNDAY");
        check(sunday.getMinimalDaysInFirstWeek() == 1, "SUNDAY_START minimalDaysInFirstWeek 1");
        check(sunday.equals(WeekFields.SUNDAY_START), "of(SUNDAY,1) equals SUNDAY_START");

        // locale-derived configuration (US: Sunday, 1 day)
        WeekFields us = WeekFields.of(Locale.US);
        check(us.getFirstDayOfWeek() == DayOfWeek.SUNDAY, "Locale.US firstDayOfWeek SUNDAY");
        check(us.getMinimalDaysInFirstWeek() == 1, "Locale.US minimalDaysInFirstWeek 1");

        // fields applied to 2024-01-01 (a Monday, ISO week 1 of week-based-year 2024)
        LocalDate jan1 = LocalDate.of(2024, 1, 1);
        check(jan1.getDayOfWeek() == DayOfWeek.MONDAY, "2024-01-01 is Monday");
        check(iso.dayOfWeek().getFrom(jan1) == 1, "ISO dayOfWeek 2024-01-01 = 1");
        check(iso.weekOfYear().getFrom(jan1) == 1, "ISO weekOfYear 2024-01-01 = 1");
        check(iso.weekOfMonth().getFrom(jan1) == 1, "ISO weekOfMonth 2024-01-01 = 1");
        check(iso.weekOfWeekBasedYear().getFrom(jan1) == 1, "ISO weekOfWeekBasedYear 2024-01-01 = 1");
        check(iso.weekBasedYear().getFrom(jan1) == 2024, "ISO weekBasedYear 2024-01-01 = 2024");

        // week-based-year rollover: 2024-12-30 belongs to week-based-year 2025
        LocalDate dec30 = LocalDate.of(2024, 12, 30);
        check(iso.weekBasedYear().getFrom(dec30) == 2025, "ISO weekBasedYear 2024-12-30 = 2025");
        check(iso.weekOfWeekBasedYear().getFrom(dec30) == 1, "ISO weekOfWeekBasedYear 2024-12-30 = 1");

        // sunday-start day-of-week mapping (2024-06-15 is Saturday -> 7, 2024-06-16 is Sunday -> 1)
        check(sunday.dayOfWeek().getFrom(LocalDate.of(2024, 6, 15)) == 7, "SUNDAY_START dayOfWeek Saturday = 7");
        check(sunday.dayOfWeek().getFrom(LocalDate.of(2024, 6, 16)) == 1, "SUNDAY_START dayOfWeek Sunday = 1");

        // equals / hashCode
        check(WeekFields.ISO.equals(WeekFields.ISO), "ISO equals itself");
        check(!WeekFields.ISO.equals(sunday), "ISO not equals SUNDAY_START");
        check(WeekFields.ISO.hashCode() == WeekFields.ISO.hashCode(), "ISO hashCode stable");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
