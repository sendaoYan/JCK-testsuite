/*
 * @test
 * @summary java.time.DayOfWeek enum conformance: getValue, of, and plus (with wrap-around).
 * @run main DayOfWeekTest
 */

import java.time.DayOfWeek;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalQueries;

public class DayOfWeekTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testDayOfWeek() {
        check(DayOfWeek.MONDAY.getValue() == 1 && DayOfWeek.SUNDAY.getValue() == 7, "getValue");
        check(DayOfWeek.of(1) == DayOfWeek.MONDAY, "of");
        check(DayOfWeek.MONDAY.plus(1) == DayOfWeek.TUESDAY, "plus");
        check(DayOfWeek.MONDAY.plus(7) == DayOfWeek.MONDAY, "plus wraps");
    }

    public static void main(String[] args) {
        testDayOfWeek();
        testDeepDayOfWeek();
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


    static void testDeepDayOfWeek() {
        DayOfWeek[] vals = DayOfWeek.values();
        check(vals.length == 7 && vals[0] == DayOfWeek.MONDAY && vals[6] == DayOfWeek.SUNDAY, "values order ISO");
        check(DayOfWeek.valueOf("MONDAY") == DayOfWeek.MONDAY, "valueOf");
        xact(() -> DayOfWeek.valueOf("bogus"), IllegalArgumentException.class,
            "No enum constant java.time.DayOfWeek.bogus", "valueOf bogus");
        check(DayOfWeek.of(1) == DayOfWeek.MONDAY && DayOfWeek.of(7) == DayOfWeek.SUNDAY, "of ISO");
        xact(() -> DayOfWeek.of(0), DateTimeException.class,
            "Invalid value for DayOfWeek: 0", "of 0");
        xact(() -> DayOfWeek.of(8), DateTimeException.class,
            "Invalid value for DayOfWeek: 8", "of 8");
        for (int i = 0; i < 7; i++) {
            final DayOfWeek d = vals[i];
            check(d.getValue() == i + 1, "getValue " + d);
            check(d.plus(7) == d, "plus 7 identity " + d);
            check(d.minus(7) == d, "minus 7 identity " + d);
        }
        check(DayOfWeek.MONDAY.plus(1) == DayOfWeek.TUESDAY, "plus 1");
        check(DayOfWeek.SUNDAY.plus(1) == DayOfWeek.MONDAY, "plus 1 wraps");
        check(DayOfWeek.MONDAY.minus(1) == DayOfWeek.SUNDAY, "minus 1 wraps");
        check(DayOfWeek.FRIDAY.plus(4) == DayOfWeek.TUESDAY, "plus 4");
        check(DayOfWeek.THURSDAY.getDisplayName(TextStyle.FULL, java.util.Locale.FRENCH).equals("jeudi"), "FR full");
        check(DayOfWeek.THURSDAY.getDisplayName(TextStyle.NARROW, java.util.Locale.FRENCH).equals("J"), "FR narrow");
        check(DayOfWeek.THURSDAY.getDisplayName(TextStyle.SHORT, java.util.Locale.ENGLISH).equals("Thu"), "EN short");
        check(DayOfWeek.THURSDAY.getDisplayName(TextStyle.FULL, java.util.Locale.ENGLISH).equals("Thursday"), "EN full");
        check(DayOfWeek.from(LocalDate.of(2024, 2, 29)) == DayOfWeek.THURSDAY, "from Feb29-2024");
        check(DayOfWeek.from(LocalDate.of(2024, 3, 1)) == DayOfWeek.FRIDAY, "from Mar1-2024");
        check(DayOfWeek.THURSDAY.query(TemporalQueries.precision()) == ChronoUnit.DAYS, "precision");
        xeq(DayOfWeek.THURSDAY.adjustInto(LocalDate.of(2024, 2, 29)).toString(), "2024-02-29", "adjustInto same");
        xeq(DayOfWeek.MONDAY.adjustInto(LocalDate.of(2024, 2, 29)).toString(), "2024-02-26", "adjustInto back");
        check(DayOfWeek.THURSDAY.getLong(ChronoField.DAY_OF_WEEK) == 4, "getLong");
        check(DayOfWeek.MONDAY.compareTo(DayOfWeek.TUESDAY) < 0, "compareTo");
        xeq(LocalDate.of(2024, 2, 29).getDayOfWeek().getDisplayName(TextStyle.FULL, java.util.Locale.FRENCH),
            "jeudi", "localized dow of date");
    }

}
