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
import java.time.temporal.ChronoField;

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
        testDeepWeekFields();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ==== contract deepening harness (appended) ====
    interface DeepRun { void run() throws Throwable; }

    static void xact(DeepRun r, Class<?> type, String exactMsg, String tag) {
        try {
            r.run();
            throw new AssertionError(tag + ": expected " + type.getSimpleName() + " but nothing was thrown");
        } catch (AssertionError e) {
            throw e;
        } catch (Throwable t) {
            if (!type.isInstance(t))
                throw new AssertionError(tag + ": expected " + type.getSimpleName() + " but got "
                        + t.getClass().getName() + ": " + t.getMessage(), t);
            if (exactMsg != null && !exactMsg.equals(t.getMessage()))
                throw new AssertionError(tag + ": " + type.getSimpleName() + " message <" + t.getMessage()
                        + "> want <" + exactMsg + ">");
            passed++;
        }
    }

    static void xeq(Object got, Object want, String tag) {
        if (got == null ? want != null : !got.equals(want))
            throw new AssertionError(tag + ": got <" + got + "> want <" + want + ">");
        passed++;
    }


    static void testDeepWeekFields() {
        WeekFields us = WeekFields.of(java.util.Locale.US);
        check(us.getFirstDayOfWeek() == DayOfWeek.SUNDAY, "US SUNDAY");
        check(us.getMinimalDaysInFirstWeek() == 1, "US minimal 1");
        WeekFields iso = WeekFields.ISO;
        check(iso.getFirstDayOfWeek() == DayOfWeek.MONDAY, "ISO MONDAY");
        check(iso.getMinimalDaysInFirstWeek() == 4, "ISO minimal 4");
        check(WeekFields.SUNDAY_START.getFirstDayOfWeek() == DayOfWeek.SUNDAY, "SUNDAY_START");
        check(WeekFields.SUNDAY_START.getMinimalDaysInFirstWeek() == 1, "SUNDAY_START minimal 1");
        check(WeekFields.of(DayOfWeek.MONDAY, 4).equals(WeekFields.ISO), "of(MONDAY,4)==ISO");
        check(WeekFields.ISO.equals(WeekFields.of(java.util.Locale.FRANCE)), "FR locale == ISO-ish Monday rules");
        xeq(WeekFields.ISO.toString(), "WeekFields[MONDAY,4]", "ISO toString");
        LocalDate d = LocalDate.of(2024, 2, 29);
        check(d.get(WeekFields.ISO.dayOfWeek()) == 4, "ISO dow of Thursday=4");
        check(d.get(us.dayOfWeek()) == 5, "US dow of Thursday=5 (Sunday=1)");
        check(d.get(WeekFields.ISO.weekOfWeekBasedYear()) == 9, "ISO wWBY=9");
        check(d.get(us.weekOfWeekBasedYear()) == 9, "US wWBY=9");
        check(LocalDate.of(2025, 1, 1).get(WeekFields.ISO.weekOfWeekBasedYear()) == 1, "2025-01-01 ISO week 1");
        check(LocalDate.of(2024, 12, 30).get(WeekFields.ISO.weekOfWeekBasedYear()) == 1, "2024-12-30 ISO week 1 of 2025");
        check(LocalDate.of(2024, 12, 29).get(WeekFields.ISO.weekOfWeekBasedYear()) == 52, "2024-12-29 ISO week 52");
        check(d.get(us.weekOfMonth()) == 5, "weekOfMonth US");
        check(d.get(WeekFields.ISO.weekOfMonth()) == 5, "weekOfMonth ISO");
        check(d.get(WeekFields.ISO.weekBasedYear()) == 2024, "weekBasedYear field");
        check(LocalDate.of(2025, 1, 1).get(WeekFields.ISO.weekBasedYear()) == 2025, "wby 2025-01-01");
        check(d.get(WeekFields.ISO.dayOfWeek()) == d.get(ChronoField.DAY_OF_WEEK), "ISO dayOfWeek == DAY_OF_WEEK");
        check(LocalDate.of(2024, 2, 29).with(us.dayOfWeek(), 1).toString().equals("2024-02-25"), "with US dow 1 -> Sunday");
        xeq(LocalDate.of(2024, 2, 29).with(WeekFields.ISO.dayOfWeek(), 1).toString(), "2024-02-26", "with ISO dow 1 -> Monday");
        xact(() -> WeekFields.of(null, 1), NullPointerException.class, null, "of null dow");
        xact(() -> WeekFields.of(DayOfWeek.MONDAY, 0), IllegalArgumentException.class,
            "Minimal number of days is invalid", "minimal 0");
        xact(() -> WeekFields.of(DayOfWeek.MONDAY, 8), IllegalArgumentException.class,
            "Minimal number of days is invalid", "minimal 8");
        check(WeekFields.ISO.hashCode() == WeekFields.of(DayOfWeek.MONDAY, 4).hashCode(), "hashCode");
        check(WeekFields.ISO.getFirstDayOfWeek().plus(1) == DayOfWeek.TUESDAY, "sanity");
        check(LocalDate.of(2024, 2, 29).getLong(WeekFields.ISO.weekBasedYear()) == 2024L, "getLong wby");
    }

}
