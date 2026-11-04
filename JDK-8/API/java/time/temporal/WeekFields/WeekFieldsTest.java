/*
 * @test
 * @summary java.time.temporal.WeekFields conformance: ISO week fields.
 * @run main WeekFieldsTest
 */

import java.time.DayOfWeek;
import java.time.temporal.WeekFields;
import java.time.LocalDate;
import java.time.temporal.ChronoField;

public class WeekFieldsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testWeekFields() {
        WeekFields wf = WeekFields.ISO;
        check(wf.getFirstDayOfWeek() == DayOfWeek.MONDAY, "firstDayOfWeek MONDAY");
        check(wf.getMinimalDaysInFirstWeek() == 4, "minimalDaysInFirstWeek 4");
        check(wf.dayOfWeek() != null, "dayOfWeek");
        check(wf.weekOfMonth() != null, "weekOfMonth");
        check(wf.weekOfYear() != null, "weekOfYear");
        check(wf.weekOfWeekBasedYear() != null, "weekOfWeekBasedYear");
        check(wf.weekBasedYear() != null, "weekBasedYear");

        WeekFields sunday = WeekFields.of(DayOfWeek.SUNDAY, 1);
        check(sunday.getFirstDayOfWeek() == DayOfWeek.SUNDAY, "of firstDayOfWeek SUNDAY");
        check(sunday.getMinimalDaysInFirstWeek() == 1, "of minimalDaysInFirstWeek 1");
    }

    public static void main(String[] args) {
        testWeekFields();
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
