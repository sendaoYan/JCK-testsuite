/*
 * @test
 * @summary java.time.Month enum conformance: getValue, of, length (leap/non-leap), and plus.
 * @run main MonthTest
 */

import java.time.Month;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.MonthDay;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalQueries;

public class MonthTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testMonth() {
        check(Month.JANUARY.getValue() == 1 && Month.DECEMBER.getValue() == 12, "getValue");
        check(Month.of(1) == Month.JANUARY && Month.of(12) == Month.DECEMBER, "of");
        check(Month.FEBRUARY.length(false) == 28 && Month.FEBRUARY.length(true) == 29, "length");
        check(Month.JANUARY.length(false) == 31, "JANUARY length");
        check(Month.JANUARY.plus(1) == Month.FEBRUARY, "plus");
    }

    public static void main(String[] args) {
        testMonth();
        testDeepMonth();
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


    static void testDeepMonth() {
        Month[] vals = Month.values();
        check(vals.length == 12 && vals[0] == Month.JANUARY && vals[11] == Month.DECEMBER, "values order");
        check(Month.valueOf("JANUARY") == Month.JANUARY, "valueOf");
        xact(() -> Month.valueOf("bogus"), IllegalArgumentException.class,
            "No enum constant java.time.Month.bogus", "valueOf bogus");
        check(Month.of(1) == Month.JANUARY && Month.of(12) == Month.DECEMBER, "of");
        xact(() -> Month.of(13), DateTimeException.class,
            "Invalid value for MonthOfYear: 13", "of 13");
        xact(() -> Month.of(0), DateTimeException.class,
            "Invalid value for MonthOfYear: 0", "of 0");
        int[] len = {31,29,31,30,31,30,31,31,30,31,30,31};
        for (int i = 0; i < 12; i++) {
            final Month m = vals[i];
            check(m.getValue() == i + 1, "getValue " + m);
            check(m.length(true) == len[i], "length leap " + m);
            check(m.length(false) == len[i] - (i == 1 ? 1 : 0), "length non-leap " + m);
            check(m.minLength() == (i == 1 ? 28 : len[i]), "minLength " + m);
            check(m.maxLength() == len[i], "maxLength " + m);
        }
        check(Month.JANUARY.firstDayOfYear(true) == 1, "Jan fdoy");
        check(Month.MARCH.firstDayOfYear(true) == 61, "Mar fdoy leap");
        check(Month.MARCH.firstDayOfYear(false) == 60, "Mar fdoy non-leap");
        check(Month.DECEMBER.firstDayOfYear(true) == 336, "Dec fdoy leap");
        check(Month.DECEMBER.firstDayOfYear(false) == 335, "Dec fdoy non-leap");
        check(Month.FEBRUARY.firstDayOfYear(false) == 32, "Feb fdoy non-leap");
        check(Month.JANUARY.plus(1) == Month.FEBRUARY, "plus 1");
        check(Month.JANUARY.plus(12) == Month.JANUARY, "plus 12 wraps");
        check(Month.JANUARY.plus(-1) == Month.DECEMBER, "plus -1 wraps");
        check(Month.JANUARY.minus(1) == Month.DECEMBER, "minus 1 wraps");
        check(Month.DECEMBER.minus(12) == Month.DECEMBER, "minus 12 wraps");
        check(Month.MARCH.minus(3) == Month.DECEMBER, "minus 3 wraps");
        check(Month.of(2).getDisplayName(TextStyle.FULL, java.util.Locale.FRENCH).equals("f\u00E9vrier"), "FR full");
        check(Month.of(2).getDisplayName(TextStyle.NARROW, java.util.Locale.ENGLISH).equals("F"), "narrow EN F");
        check(Month.JULY.getDisplayName(TextStyle.SHORT, java.util.Locale.ENGLISH).equals("Jul"), "short EN");
         check(Month.FEBRUARY.getDisplayName(TextStyle.SHORT_STANDALONE, java.util.Locale.ENGLISH).equals("2"), "SHORT_STANDALONE JDK8 numeric");
        check(Month.from(LocalDate.of(2024, 5, 4)) == Month.MAY, "from");
        check(Month.from(YearMonth.of(2024, 7)) == Month.JULY, "from YearMonth");
        xeq(MonthDay.of(Month.MARCH, 5).toString(), "--03-05", "MonthDay.of enum");
        check(Month.FEBRUARY.query(TemporalQueries.precision()) == ChronoUnit.MONTHS, "precision");
        xeq(Month.DECEMBER.adjustInto(LocalDate.of(2024, 2, 29)).toString(), "2024-12-29", "adjustInto keeps day");
        check(Month.FEBRUARY.compareTo(Month.MARCH) < 0, "compareTo");
        xeq(Month.FEBRUARY.getDisplayName(TextStyle.FULL, java.util.Locale.ENGLISH), "February", "EN full");
         xeq(Month.FEBRUARY.getDisplayName(TextStyle.FULL_STANDALONE, java.util.Locale.ENGLISH), "2", "EN full standalone JDK8 numeric");
    }

}
